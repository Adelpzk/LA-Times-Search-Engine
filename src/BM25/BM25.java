package BM25;

import utilities.*;

import java.io.*;
import java.util.*;

public class BM25 {

    private static boolean stemmingEnabled;

    public static void main(String[] args) {
        stemmingEnabled = ConfigLoader.getBooleanProperty("stemming.enabled", false);
        runBM25(args[0], args[1], args[2]);
    }

    @SuppressWarnings("unchecked")
    private static void runBM25(String pathToFiles, String pathToQueries, String resultPath) {
        List<String> topicIds = new ArrayList<>();
        List<String> queries = new ArrayList<>();
        List<TREC> writeToFile = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(pathToQueries))) {
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                topicIds.add(line);
                System.out.println(topicIds.get(i));
                line = br.readLine().replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ").toLowerCase();
                queries.add(line);
                System.out.println(queries.get(i));
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String invIndexFileName = stemmingEnabled ? "invIndex-stemmed" : "invIndex";
        String runTag = stemmingEnabled ? "apazokitBM25Stem" : "apazokitBM25noStem";
        try (BufferedInputStream fileInInvIndex = new BufferedInputStream(new FileInputStream(pathToFiles + "/" + invIndexFileName));
             BufferedInputStream fileInTermToId = new BufferedInputStream(new FileInputStream(pathToFiles + "/" + "termToId"));
             BufferedInputStream fileInDocNoToIdMapping = new BufferedInputStream(new FileInputStream(pathToFiles + "/" + "DocNoToIdMapping"));
             BufferedInputStream fileDocLengths = new BufferedInputStream(new FileInputStream(pathToFiles + "/" + "doc-lengths"));
             ObjectInputStream inTermToId = new ObjectInputStream(fileInTermToId);
             ObjectInputStream inInvIndex = new ObjectInputStream(fileInInvIndex);
             ObjectInputStream inDocLengths = new ObjectInputStream(fileDocLengths);
             ObjectInputStream inDocNoToIdMapping = new ObjectInputStream(fileInDocNoToIdMapping)) {
            HashMap<Integer, List<Integer>> invIndex = (HashMap<Integer, List<Integer>>) inInvIndex.readObject();
            HashMap<String, Integer> termToId = (HashMap<String, Integer>) inTermToId.readObject();
            HashMap<Integer, String> idToDocNoMap = (HashMap<Integer, String>) inDocNoToIdMapping.readObject();
            List<Integer> docLengths = (List<Integer>) inDocLengths.readObject();
            System.out.println("InvertedIndex Size:" + invIndex.size());
            int docCollectionSize = docLengths.size();
            double averageDocLength = getAverageDocLength(docLengths);
            for (int i = 0; i < queries.size(); i++) {
                Map<Integer, Double> bm25Scores = new HashMap<>();
                String[] queryTokens = tokenize(queries.get(i));
                for (String queryToken : queryTokens) {
                    if (stemmingEnabled) {
                        queryToken = PorterStemmer.stem(queryToken);
                    }
                    if (termToId.containsKey(queryToken)) {
                        int termId = termToId.get(queryToken);
                        List<Integer> postings = invIndex.get(termId);
                        int n_i = postings.size() / 2;
                        for (int j = 0; j < postings.size(); j += 2) {
                            int docId = postings.get(j);
                            int f_i = postings.get(j + 1);
                            int d_l = docLengths.get(docId);
                            double bm25Score = runBM25(docCollectionSize, averageDocLength, d_l, n_i, f_i);
                            bm25Scores.merge(docId, bm25Score, Double::sum);
                        }
                    }
                }


                List<Map.Entry<Integer, Double>> sortedBm25Scores = new ArrayList<>(bm25Scores.entrySet());
                sortedBm25Scores.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
                int index = 1;
                int rank = 1;
                for (Map.Entry<Integer, Double> entry : sortedBm25Scores) {
                    if (index > 1000) break;
                    TREC trec = new TREC(topicIds.get(i), idToDocNoMap.get(entry.getKey()), rank, entry.getValue(), runTag);
                    rank++;
                    writeToFile.add(trec);
                    index++;
                }
                System.out.println("Query: " + queries.get(i));
            }
            try (FileWriter fileWriter = new FileWriter(resultPath);
                 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                int count = 0;
                for (TREC item : writeToFile) {
                    bufferedWriter.write(item.getTopicId() + " " + "Q0" + " " + item.getDocNO() + " " + item.getRank() + " " + item.getScore() + " " + item.getRunTag());
                    if (count != writeToFile.size() - 1) {
                        bufferedWriter.newLine();
                    }
                    count++;
                }
                bufferedWriter.close();
                System.out.println("List written to " + resultPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static double runBM25(int N, double av_dl, int d_l, int n_i, int f_i) {
        double k = 1.2 * ((1.0 - 0.75) + 0.75 * ((double) d_l / av_dl));
        //System.out.println("k = " + k);
        double fraction = (double) f_i / (k + f_i);
        //System.out.println("fraction " + fraction);
        return fraction * Math.log((N - n_i + 0.5) / (n_i + 0.5));
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
