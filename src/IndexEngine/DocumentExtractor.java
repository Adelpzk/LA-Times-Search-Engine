package IndexEngine;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class DocumentExtractor {
    private final HashSet<MetaData> documents = new HashSet<>();
    private final List<String> tokens = new ArrayList<>();
    private final List<Integer> docLengths = new ArrayList<>();
    private final DocumentDownloader documentDownloader;
    private final InvertedIndexHandler invertedIndexHandler;
    private String docNo;
    private String date;
    private int internalId;
    private String headLine;
    private final StringBuilder docContent = new StringBuilder();
    private final StringBuilder docTokens = new StringBuilder();

    public DocumentExtractor(DocumentDownloader documentDownloader,
                             InvertedIndexHandler invertedIndexHandler) {
        this.documentDownloader = documentDownloader;
        this.invertedIndexHandler = invertedIndexHandler;
    }

    public void documentExtractor(FileInputStream filePath, String storePath) {
        boolean itsToken = false;
        boolean insideDoc = false;
        boolean insideHeadLine = false;
        try {
            // Per Instructions https://stackoverflow.com/questions/1080381/gzipinputstream-reading-line-by-line
            InputStream gzipStream = new GZIPInputStream(filePath);
            Reader decoder = new InputStreamReader(gzipStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(decoder);
            String line;
            int count = 0;
            while ((line = bufferedReader.readLine()) != null) {
                internalId = count;
                getDocNo(line);
                if (docNo != null) {
                    getDate(docNo);
                }

                if (line.contains("<DOC>")) {
                    insideDoc = true;
                }

                if (insideDoc) {
                    docContent.append(line).append(System.lineSeparator());
                }

                if (line.contains("<TEXT>") || line.contains("<HEADLINE>") || line.contains("<GRAPHIC>")) {
                    itsToken = true;
                    if (line.contains("<HEADLINE>")) {
                        insideHeadLine = true;
                    }
                    line = bufferedReader.readLine();
                }

                if (line.contains("</HEADLINE>")) {
                    insideHeadLine = false;
                }

                if (insideHeadLine) {
                    if (!line.contains("<HEADLINE>") && !line.contains("<P>") && !line.contains("</P>")) {
                        if (headLine != null) {
                            headLine = "%s %s".formatted(headLine, line);
                        } else {
                            headLine = line;
                        }
                    }
                }
                if (line.contains("</TEXT>") || line.contains("</HEADLINE>") || line.contains("</GRAPHIC>")) {
                    itsToken = false;
                }
                if (itsToken && !line.contains("<P>") && !line.contains("</P>")) {
                    docTokens.append(line).append(System.lineSeparator());
                }
                if (line.contains("</DOC>")) {
                    String documentContent = docTokens.toString().replaceAll("[^a-zA-Z0-9]", " ");
                    documentContent = documentContent.replaceAll("\\s+", " ").toLowerCase();
                    docLengths.add(documentContent.length());
                    MetaData metaData = new MetaData(docNo, date, docContent.toString(), headLine, internalId);
                    itsToken = false;
                    insideDoc = false;
                    documents.add(metaData);
                    tokens.add(documentContent);
                    docTokens.setLength(0);
                    docContent.setLength(0);
                    headLine = "";
                    count++;
                }

            }
            System.out.println(documents.size() + " docs found");
            documentDownloader.documentSaver(documents, docLengths, storePath);
            invertedIndexHandler.indexBuilder(tokens, storePath);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void getDocNo(String line) {
        String docNoTag = "<DOCNO>";
        String docNoClosingTag = "</DOCNO>";
        if (line.contains(docNoTag) && line.contains(docNoClosingTag)) {
            int docNoIndex = line.indexOf(docNoTag);
            int endDocNoIndex = line.indexOf(docNoClosingTag);
            docNo = line.substring(docNoIndex + docNoTag.length(), endDocNoIndex).trim();
        }
    }

    private void getDate(String docNo) {
        String dateDocNo = docNo.substring(2, 8);
        // Technique was used from https://www.tutorialspoint.com/format-date-with-simpledateformat-mm-dd-yy-in-java
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("MMddyy");

        SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMMM d, yyyy");

        Date inputDate;
        try {
            inputDate = inputDateFormat.parse(dateDocNo);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        date = outputDateFormat.format(inputDate);
    }
}
