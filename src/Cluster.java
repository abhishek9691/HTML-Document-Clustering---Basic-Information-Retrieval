import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: sethi
 * Date: 4/24/14
 * Time: 1:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class Cluster {
    private ArrayList<Integer> docsInCluster;

    private double similarity;

    @Override
    public String toString() {
        return "Cluster = {" + docsInCluster + "}";
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public Cluster(int firstDoc) {
        docsInCluster = new ArrayList<Integer>();
        docsInCluster.add(firstDoc);
    }

    public Cluster(){}

    public ArrayList<Integer> getDocsInCluster() {
        return docsInCluster;
    }

    public void setDocsInCluster(ArrayList<Integer> docsInCluster) {
        this.docsInCluster = docsInCluster;
    }

    public void addDoc(int doc){
        docsInCluster.add(new Integer(doc));
    }

    public void addCluster(Cluster other){
        ArrayList<Integer> otherDocs = other.getDocsInCluster();
        Iterator<Integer> it = otherDocs.iterator();
        while(it.hasNext()){
            docsInCluster.add(it.next());
        }
    }
}
