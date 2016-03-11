/**
 * Created with IntelliJ IDEA.
 * User: sethi
 * Date: 4/24/14
 * Time: 1:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimilarityPair {
    private int row;
    private int column;
    private double similarity;

    @Override
    public String toString() {
        return "SimilarityPair{" +
                "row=" + row +
                ", column=" + column +
                ", similarity=" + similarity +
                '}';
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public SimilarityPair(int row, int column, double similarity) {
        this.row = row;
        this.column = column;
        this.similarity = similarity;
    }
}
