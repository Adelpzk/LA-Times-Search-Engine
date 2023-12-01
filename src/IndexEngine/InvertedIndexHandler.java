package IndexEngine;


import utilities.*;
import java.io.*;
import java.util.*;

public class InvertedIndexHandler {
    // Lexicon mapping terms to IDs
    Map<String, Integer> termToId = new HashMap<>();
    // Lexicon mapping IDs back to terms
    Map<Integer, String> idToTerm = new HashMap<>();
    // Inverted index, mapping termIds to Postings
    Map<Integer, List<Integer>> invIndex = new HashMap<>();

    private static boolean stemmingEnabled;

    public InvertedIndexHandler() {
        stemmingEnabled = ConfigLoader.getBooleanProperty("stemming.enabled", false);
    }

    public void indexBuilder(List<String> docs, String storePath) {
        int docId = 0;
        for (String doc : docs) {
            String[] tokens = doc.split("\\s+");
            List<Integer> tokenIds = convertTokensToTokenIds(tokens, termToId, idToTerm);
            Map<Integer, Integer> wordCounts = countWords(tokenIds);
            addToPostings(wordCounts, docId, invIndex);
            docId++;
        }
        saveFiles(invIndex, idToTerm, termToId, storePath);
        System.out.println("Lexicon and Inverted Index were saved");
    }

    private List<Integer> convertTokensToTokenIds(String[] tokens, Map<String, Integer> termToId, Map<Integer, String> idToTerm) {
        List<Integer> tokenIds = new ArrayList<>();
        for (String token : tokens) {
            if(stemmingEnabled){
                token = PorterStemmer.stem(token);
            }
            if (termToId.containsKey(token)) {
                tokenIds.add(termToId.get(token));
            } else {
                int id = termToId.size();
                termToId.put(token, id);
                idToTerm.put(id, token);
                tokenIds.add(id);
            }
        }
        return tokenIds;
    }

    private Map<Integer, Integer> countWords(List<Integer> tokenIds) {
        Map<Integer, Integer> wordCounts = new HashMap<>();
        for (int id : tokenIds) wordCounts.put(id, wordCounts.getOrDefault(id, 0) + 1);
        return wordCounts;
    }

    private void addToPostings(Map<Integer, Integer> wordCounts, int docId, Map<Integer, List<Integer>> invIndex) {
        for (int id : wordCounts.keySet()) {
            int count = wordCounts.get(id);
            List<Integer> postingsList = invIndex.getOrDefault(id, new ArrayList<>());
            postingsList.add(docId);
            postingsList.add(count);
            invIndex.put(id, postingsList);
        }

    }

    private void saveFiles(Map<Integer, List<Integer>> invIndex, Map<Integer, String> idToTerm, Map<String, Integer> termToId, String storePath) {
        System.out.println("Processing Lexicon and Inverted Index");
        File invIndexFile;
        if(stemmingEnabled){
            invIndexFile = new File(storePath, "invIndex-stemmed");
        }else{
            invIndexFile = new File(storePath, "invIndex");
        }
        File idToTermFile = new File(storePath, "idToTerm");
        File termToIdFile = new File(storePath, "termToId");
        try (BufferedOutputStream fileInvIndex = new BufferedOutputStream(new FileOutputStream(invIndexFile));
             BufferedOutputStream fileIdToTerm = new BufferedOutputStream(new FileOutputStream(idToTermFile));
             BufferedOutputStream fileTermToId = new BufferedOutputStream(new FileOutputStream(termToIdFile));
             ObjectOutputStream out = new ObjectOutputStream(fileInvIndex);
             ObjectOutputStream out2 = new ObjectOutputStream(fileIdToTerm);
             ObjectOutputStream out3 = new ObjectOutputStream(fileTermToId)) {

            out.writeObject(invIndex);
            out2.writeObject(idToTerm);
            out3.writeObject(termToId);

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }
}
