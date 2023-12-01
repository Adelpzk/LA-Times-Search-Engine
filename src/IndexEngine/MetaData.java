package IndexEngine;

public class MetaData {

    private String docNo;
    private String date;
    private String document;
    private String headLine;
    private int internalId;

    public MetaData(String docNo, String date, String document, String headLine, int internalId) {
        this.docNo = docNo;
        this.date = date;
        this.document = document;
        this.headLine = headLine;
        this.internalId = internalId;
    }

    public String getDocNo() {
        return docNo;
    }

    public String getDate() {
        return date;
    }

    public String getDocument() { return document; }

    public String getHeadLine() {
        return headLine;
    }

    public int getInternalID() {
        return internalId;
    }


}
