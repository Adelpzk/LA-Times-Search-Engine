package IndexEngine;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class DocumentDownloader {

    private HashMap<Integer, String> idToDocNoMap = new HashMap<>();

    public DocumentDownloader() {

    }

    public void documentSaver(HashSet<MetaData> documents, List<Integer> docLengths, String storePath) {
        createDirectory(storePath, true);
        for (MetaData doc : documents) {
            idToDocNoMap.put(doc.getInternalID(), doc.getDocNo());
            String yearPath = storePath + "/" + getFolderName(doc.getDate(), "yyyy");
            String monthPath = yearPath + "/" + getFolderName(doc.getDate(), "MMMM");
            String dayPath = monthPath + "/" + getFolderName(doc.getDate(), "dd");

            createDirectory(yearPath, false);
            createDirectory(monthPath, false);
            createDirectory(dayPath, false);

            File file = new File(dayPath, doc.getDocNo());
            File fileMetaData = new File(dayPath, doc.getDocNo() + ".MetaData");


            String docContent = doc.getDocument();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(docContent);
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
            try (BufferedWriter writer2 = new BufferedWriter(new FileWriter(fileMetaData))) {
                writer2.write(String.format("""
                                docno: %s
                                internal id: %d
                                date: %s
                                headline: %s
                                """,
                        doc.getDocNo(), doc.getInternalID(), doc.getDate(), doc.getHeadLine()));
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
        File docNoToId = new File(storePath, "DocNoToIdMapping");
        File docLengthsFile = new File(storePath, "doc-lengths");
        try (BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(docNoToId));
             BufferedOutputStream fileDocLengthsOut = new BufferedOutputStream(new FileOutputStream(docLengthsFile));
             ObjectOutputStream out = new ObjectOutputStream(fileOut);
             ObjectOutputStream out2 = new ObjectOutputStream(fileDocLengthsOut)) {

            out.writeObject(idToDocNoMap);
            out2.writeObject(docLengths);

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        System.out.println("Files and folders created successfully.");
    }

    private String getFolderName(String dateMetaData, String format) {

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
            Date date = dateFormat.parse(dateMetaData);
            SimpleDateFormat yearFormat = new SimpleDateFormat(format);
            return yearFormat.format(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void createDirectory(String filePath, boolean initialDirectory) {
        File directoryYear = new File(filePath);
        if (!directoryYear.exists()) {
            boolean created = directoryYear.mkdirs();
            if (initialDirectory) {
                if (created) {
                    System.out.println("Directory created successfully.");
                } else {
                    System.out.println("Failed to create the directory.");
                }
            }
        } else if (initialDirectory) System.out.println("Directory already exists.");
    }

}
