package BM25;

import java.io.*;

public class ExtractTopicIds {

    public static void main(String[] args) {
        extractTopicIds(args[0]);
    }

    private static void extractTopicIds(String path) {

        try (BufferedReader br = new BufferedReader(new FileReader(path));
             BufferedWriter writer = new BufferedWriter(new FileWriter("Data/latimes-index/queries.txt"))) {
            String line;
            String numberTag = "<number>";
            String numberClosingTag = "</number>";
            String titleTag = "<title>";
            while ((line = br.readLine()) != null) {
                //System.out.print(line);
                if (line.contains(numberTag) && line.contains(numberClosingTag)) {
                    int numberIndex = line.indexOf(numberTag);
                    int endNumberIndex = line.indexOf(numberClosingTag);
                    String number = line.substring(numberIndex + numberTag.length(), endNumberIndex).trim();
                    System.out.print(number);
                    if (!number.equals("416") && !number.equals("423") && !number.equals("437") && !number.equals("444") && !number.equals("447")) {
                        // Write lines to the file
                        writer.write(number);
                        writer.newLine();

                    }else{
                        continue;
                    }
                }
                if (line.contains(titleTag)) {
                    int TitleIndex = line.indexOf(numberTag);
                    String title = line.substring(TitleIndex + numberTag.length()).trim();
                    // Write lines to the file
                    writer.write(title);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}