package GetDoc;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GetDocHandler {
    private String yearFolder;
    private String monthFolder;
    private String dayFolder;


    public GetDocHandler() {

    }

    @SuppressWarnings("unchecked")
    public BufferedReader retrieveDocumentMetaData(String fileName) {

        findSubDirectories(fileName);

        String filePath = "./Data/latimes-index";
        String finalPathToMetaData = filePath + "/" + yearFolder + "/" + monthFolder + "/" + dayFolder + "/" + fileName + ".MetaData";

        return readFileContent(finalPathToMetaData, false);

    }

    public BufferedReader retrieveDocument(String fileName) {

        findSubDirectories(fileName);

        String filePath = "./Data/latimes-index";
        String finalPath = filePath + "/" + yearFolder + "/" + monthFolder + "/" + dayFolder + "/" + fileName;

        return readFileContent(finalPath, true);
    }

    private BufferedReader readFileContent(String finalPathToMetaData, boolean rawDoc) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(finalPathToMetaData));
            return bufferedReader;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void findSubDirectories(String fileName) {
        String date = fileName.substring(2, 8);

        SimpleDateFormat inputDateFormat = new SimpleDateFormat("MMddyy");

        SimpleDateFormat yearSubDirectory = new SimpleDateFormat("yyyy");

        SimpleDateFormat monthSubDirectory = new SimpleDateFormat("MMMM");

        SimpleDateFormat daySubDirectory = new SimpleDateFormat("dd");

        Date inputDate;
        try {
            inputDate = inputDateFormat.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        yearFolder = yearSubDirectory.format(inputDate);
        monthFolder = monthSubDirectory.format(inputDate);
        dayFolder = daySubDirectory.format(inputDate);
    }
}
