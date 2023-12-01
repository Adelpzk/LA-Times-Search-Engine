package IndexEngine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class IndexEngine {

    public static void main(String[] args) {
        DocumentDownloader documentDownloader = new DocumentDownloader();
        InvertedIndexHandler invertedIndexHandler = new InvertedIndexHandler();
        DocumentExtractor documentExtractor = new DocumentExtractor(documentDownloader, invertedIndexHandler);
        if (args.length == 0) {
            throw new IllegalArgumentException("No arguments provided. Please provide two arguments. \n" +
                    "First argument is the path to the zipped file data. \n" +
                    "Second argument is the path to where the data will get stored.");
        }
        if (args.length == 1) {
            throw new IllegalArgumentException("Missing an argument. Please provide two arguments. \n" +
                    "First argument is the path to the zipped file data. \n" +
                    "Second argument is the path to where the data will get stored.");
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(args[0]);
            documentExtractor.documentExtractor(fileInputStream, args[1]);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

}
