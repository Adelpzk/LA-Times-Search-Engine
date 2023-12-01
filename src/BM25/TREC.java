package BM25;

public class TREC {

    private final String topicId;
    private final String docNO;
    private final int rank;
    private final double score;
    private final String runTag;


    public TREC(String topicId, String docNO, int rank, double score, String runTag) {
        this.topicId = topicId;
        this.docNO = docNO;
        this.rank = rank;
        this.score = score;
        this.runTag = runTag;
    }
    public String getRunTag() {
        return runTag;
    }

    public double getScore() {
        return score;
    }

    public int getRank() {
        return rank;
    }

    public String getDocNO() {
        return docNO;
    }

    public String getTopicId() {
        return topicId;
    }
}
