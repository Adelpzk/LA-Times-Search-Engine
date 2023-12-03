package UserSearch;

import GetDoc.GetDocHandler;

import java.util.Scanner;

import utilities.ConfigLoader;
import utilities.PorterStemmer;
import BM25.TREC;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserSearch {

    private static boolean stemmingEnabled;
    private static HashMap<Integer, List<Integer>> invIndex;
    private static HashMap<String, Integer> termToId;
    private static HashMap<Integer, String> idToDocNoMap;
    private static List<Integer> docLengths;
    private static GetDocHandler getDocHandler;
    private static List<TREC> writeToFile;

    public static void main(String[] args) {
        getDocHandler = new GetDocHandler();
        stemmingEnabled = ConfigLoader.getBooleanProperty("stemming.enabled", false);
        loadData("Data/latimes-index");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter your query (or Q to exit):");
            String userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("Q")) {
                System.out.println("Search engine terminated.");
                break; // Exit the loop if user enters 'Q'
            }

            long startTime = System.nanoTime();
            // Run the BM25 search with the user's input
            runBM25(userInput);
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            double seconds = duration / 1_000_000_000.0;
            System.out.println("Retrieval took " + seconds + " seconds");

            while (true) {
                // Prompt for next action
                System.out.println("To view a document enter the rank of the document, for a new query enter 'N', and to quit 'Q'");
                String nextAction = scanner.nextLine();

                if (nextAction.equalsIgnoreCase("N")) {
                    break; // Break the inner loop to enter a new query
                } else if (nextAction.equalsIgnoreCase("Q")) {
                    scanner.close();
                    System.out.println("Search engine terminated.");
                    return; // Exit the program
                } else {
                    if (Integer.parseInt(nextAction) > 10 && Integer.parseInt(nextAction) < 0) {
                        System.out.println("Invalid input. Only 10 Documents returned");
                    } else {
                        try {
                            int docRank = Integer.parseInt(nextAction);
                            BufferedReader document = getDocHandler.retrieveDocument(writeToFile.get(docRank - 1).getDocNO());
                            String line;
                            try {
                                while (((line = document.readLine()) != null)) {
                                    System.out.println(line);
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input. Please enter a number for the document rank, 'N' for a new query, or 'Q' to quit.");
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadData(String pathToFiles) {
        String invIndexFileName = stemmingEnabled ? "invIndex-stemmed" : "invIndex";
        try (BufferedInputStream fileInInvIndex = new BufferedInputStream(new FileInputStream(pathToFiles + "/" + invIndexFileName));
             BufferedInputStream fileInTermToId = new BufferedInputStream(new FileInputStream(pathToFiles + "/" + "termToId"));
             BufferedInputStream fileInDocNoToIdMapping = new BufferedInputStream(new FileInputStream(pathToFiles + "/" + "DocNoToIdMapping"));
             BufferedInputStream fileDocLengths = new BufferedInputStream(new FileInputStream(pathToFiles + "/" + "doc-lengths"));
             ObjectInputStream inTermToId = new ObjectInputStream(fileInTermToId);
             ObjectInputStream inInvIndex = new ObjectInputStream(fileInInvIndex);
             ObjectInputStream inDocLengths = new ObjectInputStream(fileDocLengths);
             ObjectInputStream inDocNoToIdMapping = new ObjectInputStream(fileInDocNoToIdMapping)) {
            invIndex = (HashMap<Integer, List<Integer>>) inInvIndex.readObject();
            termToId = (HashMap<String, Integer>) inTermToId.readObject();
            idToDocNoMap = (HashMap<Integer, String>) inDocNoToIdMapping.readObject();
            docLengths = (List<Integer>) inDocLengths.readObject();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    private static void runBM25(String query) {
        writeToFile = new ArrayList<>();
        String runTag = "apazokitBM25";
        int docCollectionSize = docLengths.size();
        double averageDocLength = getAverageDocLength(docLengths);
        Map<Integer, Double> bm25Scores = new HashMap<>();
        String[] queryTokens = tokenize(query.replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ").toLowerCase());
        for (String queryToken : queryTokens) {
            if (stemmingEnabled) {
                queryToken = PorterStemmer.stem(queryToken);
            }
            if (termToId.containsKey(queryToken)) {
                int termId = termToId.get(queryToken);
                List<Integer> postings = invIndex.get(termId);
                int n_i = postings.size() / 2;
                double idf = Math.log((docCollectionSize - n_i + 0.5) / (n_i + 0.5));
                for (int j = 0; j < postings.size(); j += 2) {
                    int docId = postings.get(j);
                    int f_i = postings.get(j + 1);
                    int d_l = docLengths.get(docId);
                    double bm25Score = runBM25(averageDocLength, d_l, idf, f_i);
                    bm25Scores.merge(docId, bm25Score, Double::sum);
                }
            }
        }


        List<Map.Entry<Integer, Double>> sortedBm25Scores = new ArrayList<>(bm25Scores.entrySet());
        sortedBm25Scores.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        int index = 1;
        int rank = 1;
        for (Map.Entry<Integer, Double> entry : sortedBm25Scores) {
            if (index > 10) break;
            TREC trec = new TREC("1", idToDocNoMap.get(entry.getKey()), rank, entry.getValue(), runTag);
            rank++;
            writeToFile.add(trec);
            index++;
        }

        int resultIndex = 1;
        for (TREC result : writeToFile) {
            PriorityQueue<Map.Entry<String, Double>> maxHeap = new PriorityQueue<>(
                    (a, b) -> b.getValue().compareTo(a.getValue())
            );
            BufferedReader document = getDocHandler.retrieveDocument(result.getDocNO());
            BufferedReader documentMetaData = getDocHandler.retrieveDocumentMetaData(result.getDocNO());


            String line;
            StringBuilder noTagsDocuments = new StringBuilder();
            boolean start = false;

            String date = null;
            String headline = null;

            try {
                while (((line = documentMetaData.readLine()) != null)) {
                    if (line.contains("date")) {
                        date = line.substring(line.indexOf(':') + 1).trim();
                    }
                    if (line.contains("headline")) {
                        headline = line.substring(line.indexOf(':') + 1).trim();
                    }

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                while (((line = document.readLine()) != null)) {
                    if (line.contains("<TEXT>")) {
                        start = true;
                    }
                    if (start) {
                        noTagsDocuments.append(line).append(System.lineSeparator());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String noTags = noTagsDocuments.toString().replaceAll("<[^>]*>", "");

            noTags = noTags.replaceAll("(?m)^[ \t]*\r?\n", "");

            List<String> sentences = splitIntoSentences(noTags);
            //List<String[]> tokenizedSentences = new ArrayList<>();
            int indexSentence = 1;
            for (String sentence : sentences) {
                //Skip sentences with less than 5 words.
                if (sentence.split("\\s+").length < 5) {
                    continue;
                }

                String[] tokens = sentence.split("\\s+");

                double score = scoreSentence(tokens, queryTokens, indexSentence);
                maxHeap.add(new AbstractMap.SimpleEntry<>(String.join(" ", tokens), score));

                indexSentence++;
            }
            int sentencesToRetrieve = 3; // Or however many you need
            List<String> summarySentences = new ArrayList<>();
            while (!maxHeap.isEmpty() && sentencesToRetrieve-- > 0) {
                summarySentences.add(maxHeap.poll().getKey());
            }

            // Output the summary
            System.out.println(resultIndex + ". " + headline + "(" + date + ")");
            resultIndex++;
            summarySentences.forEach(sentenceSet -> System.out.println(String.join(" ", sentenceSet)));
            System.out.print("(" + result.getDocNO() + ")" + "\n\n");
        }
    }

    private static double scoreSentence(String[] tokens, String[] queryTerms, int index) {
        double h = 0;
        double l = 0;
        double c = 0;
        if (index == 1) {
            l = 2;
        } else if (index == 2) {
            l = 1;
        }
        Set<String> distinctTerms = new HashSet<>();
        List<String> tokensAsList = Arrays.asList(queryTerms);
        for (String token : tokens) {
            if (tokensAsList.contains(token)) {
                c++;
                distinctTerms.add(token);
            }
        }

        double d = distinctTerms.size();

        return c + d + h + l;
    }

    private static double runBM25(double av_dl, int d_l, double idf, int f_i) {
        double k = 1.2 * ((1.0 - 0.75) + 0.75 * ((double) d_l / av_dl));
        //System.out.println("k = " + k);
        double fraction = (double) f_i / (k + f_i);
        //System.out.println("fraction " + fraction);
        return fraction * idf;
    }

    public static List<String> splitIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();
        Matcher matcher = Pattern.compile("([^.!?]+[.!?])").matcher(text);

        while (matcher.find()) {
            sentences.add(matcher.group(1).trim());
        }

        return sentences;
    }

    private static double getAverageDocLength(List<Integer> docLengths) {
        int totalCount = 0;
        for (int docLength : docLengths) {
            totalCount += docLength;
        }
        return (double) totalCount / docLengths.size();
    }

    private static String[] tokenize(String line) {
        return line.split("\\s+");
    }
}
