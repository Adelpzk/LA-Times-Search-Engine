package GetDoc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class GetDoc {


    public static void main(String[] args) {
        GetDocHandler getDocHandler = new GetDocHandler();
        if (args.length == 0) {
            throw new IllegalArgumentException("No arguments provided. Please provide three arguments. \n" +
                    "First argument is the path to the root directory of your data. \n" +
                    "Second argument is type of retrieval \n" +
                    "Third argument is the name of the document.");
        }
        if (args.length < 3) {
            throw new IllegalArgumentException("Missing an argument. Please provide three arguments. \n" +
                    "First argument is the path to the root directory of your data. \n" +
                    "Second argument is type of retrieval \n" +
                    "Third argument is the name of the document.");
        }

        //getDocHandler.retrieveDocument(args[0], args[1], args[2]);
    }

}
