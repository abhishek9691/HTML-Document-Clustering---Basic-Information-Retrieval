/**
 * Created with IntelliJ IDEA.
 * User: sethi
 * Date: 3/3/14
 * Time: 10:07 PM
 * To change this template use File | Settings | File Templates.
 */
import org.apache.commons.lang.StringEscapeUtils;

import java.io.*;
import java.util.*;

public class TermWeights {
    //The terms, and how many documents contain that term (used to compute IDF)
    private static Map<String, Integer> tokenCounter = new HashMap<String, Integer>();

    //all the tokenized files as the key, value is a map of the tokens in that document where the key is the
    //token, and the value is the frequency in that document
    private static Map<String, Map<String, Integer>> tokenizedFiles = new HashMap<String, Map<String, Integer>>();

    //the max similarity
    private static SimilarityPair maxSimilarity = new SimilarityPair(-1, -1, -1);
    private static SimilarityPair minSimilarity = new SimilarityPair(2, 2, 2);

    //the list of clusters. initially each document is its own cluster
    private static ArrayList<Cluster> clusters = new ArrayList<Cluster>();

    //the list of the output for the program, as it is processing
    private static ArrayList<String> output = new ArrayList<String>();

    //array of stop words
    private static ArrayList<String> stopWords = new ArrayList<String>(Arrays.asList("a","about","above","according","across","actually"
            ,"adj","after","afterwards","again","against","all","almost","alone","along","already","also","although",
            "always","among","amongst","an","and","another","any","anybody","anyhow","anyone","anything","anywhere",
            "are","area","areas","aren't","around","as","ask","asked","asking","asks","at","away","b","back","backed",
            "backing","backs","be","became","because","become","becomes","becoming","been","before","beforehand","began"
            ,"begin","beginning","behind","being","beings","below","beside","besides","best","better","between","beyond"
            ,"big","billion","both","but","by","c","came","can","can't","cannot","caption","case","cases","certain",
            "certainly","clear","clearly","co","come","could","couldn't","d","did","didn't","differ","different",
            "differently","do","does","doesn't","don't","done","down","downed","downing","downs","during","e","each",
            "early","eg","eight","eighty","either","else","elsewhere","end","ended","ending","ends","enough","etc","even",
            "evenly","ever","every","everybody","everyone","everything","everywhere","except","f","face","faces","fact",
            "facts","far","felt","few","fifty","find","finds","first","five","for","former","formerly","forty","found",
            "four","from","further","furthered","furthering","furthers","g","gave","general","generally","get","gets",
            "give","given","gives","go","going","good","goods","got","great","greater","greatest","group","grouped",
            "grouping","groups","h","had","has","hasn't","have","haven't","having","he","he'd","he'll","he's",
            "hence","her","here","here's","hereafter","hereby","herein","hereupon","hers","herself","high","higher",
            "highest","him","himself","his","how","however","hundred","i","i'd","i'll","i'm","i've","ie","if",
            "important","in","inc","indeed","instead","interest","interested","interesting","interests","into","is",
            "isn't","it","it's","its","itself","j","just","k","l","large","largely","last","later","latest","latter",
            "latterly","least","less","let","let's","lets","like","likely","long","longer","longest","ltd","m","made",
            "make","makes","making","man","many","may","maybe","me","meantime","meanwhile","member","members","men",
            "might","million","miss","more","moreover","most","mostly","mr","mrs","much","must","my","myself","n",
            "namely","necessary","need","needed","needing","needs","neither","never","nevertheless","new","newer",
            "newest","next","nine","ninety","no","nobody","non","none","nonetheless","noone","nor","not","nothing",
            "now","nowhere","number","numbers","o","of","off","often","old","older","oldest","on","once","one","one's",
            "only","onto","open","opened","opens","or","order","ordered","ordering","orders","other","others","otherwise",
            "our","ours","ourselves","out","over","overall","own","p","part","parted","parting","parts","per","perhaps",
            "place","places","point","pointed","pointing","points","possible","present","presented","presenting","presents",
            "problem","problems","put","puts","q","quite","r","rather","really","recent","recently","right","room","rooms",
            "s","said","same","saw","say","says","second","seconds","see","seem","seemed","seeming","seems","seven",
            "seventy","several","she","she'd","she'll","she's","should","shouldn't","show","showed","showing","shows",
            "sides","since","six","sixty","small","smaller","smallest","so","some","somebody","somehow","someone",
            "something","sometime","sometimes","somewhere","state","states","still","stop","such","sure","t","take",
            "taken","taking","ten","than","that","that'll","that's","that've","the","their","them","themselves","then",
            "thence","there","there'd","there'll","there're","there's","there've","thereafter","thereby","therefore",
            "therein","thereupon","these","they","they'd","they'll","they're","they've","thing","things","think","thinks",
            "thirty","this","those","though","thought","thoughts","thousand","three","through","throughout","thru",
            "thus","to","today","together","too","took","toward","towards","trillion","turn","turned","turning","turns",
            "twenty","two","u","under","unless","unlike","unlikely","until","up","upon","us","use","used","uses","using",
            "v","very","via","w","want","wanted","wanting","wants","was","wasn't","way","ways","we","we'd","we'll",
            "we're","we've","well","wells","were","weren't","what","what'll","what's","what've","whatever","when",
            "whence","whenever","where","where's","whereafter","whereas","whereby","wherein","whereupon","wherever",
            "whether","which","while","whither","who","who'd","who'll","who's","whoever","whole","whom","whomever",
            "whose","why","will","with","within","without","won't","work","worked","working","works","would","wouldn't",
            "x","y","year","years","yes","yet","you","you'd","you'll","you're","you've","young","younger","youngest",
            "your","yours","yourself","yourselves","z"));

    public static void main(String args[]){
        String inputDir = args[0];
        String outputDir = args[1];
        File inputFiles = new File(inputDir);
        File[] files = inputFiles.listFiles();
        int numFiles = files.length;

        long tokenStart = System.nanoTime();
        long tokenizeStart = System.nanoTime();
        for(File currFile : files){
            tokenize(currFile.getPath());
        }
        double tokenizeEnd = System.nanoTime();
        double elapsed = (tokenizeEnd- tokenizeStart) * 1.0e-09;
        System.out.println("Elapsed Time: "+elapsed+" seconds to tokenize the files");

        //Map<String, Map<String, Double>> weightsPerDoc = calculateWeights(numFiles);
        Map<String, Double> docMagnitudes = calculateMagnitudes(tokenizedFiles);
        double[][] simMatrix = calculateSimMatrix(numFiles, tokenizedFiles, docMagnitudes, inputDir);
        //find most and least similar first
        findMax(simMatrix, numFiles);
        output.add("Most similar documents: "+maxSimilarity);
        findMin(simMatrix, numFiles);
        output.add("Least similar documents: "+minSimilarity);
        //begin clustering
        cluster(simMatrix, numFiles);
        writeOutput(outputDir);
        //System.out.println(reverseDictionary.size() + " "+ tokenCounter.size() + " "+tokenizedFiles.size());
        double tokenEnd = System.nanoTime();
        double tokenElapsed = (tokenEnd - tokenStart) * 1.0e-09;
        System.out.println("Elapsed Time: "+tokenElapsed+" seconds to perform analysis");
    }

    private static void writeOutput(String outputDir){
        File outputDirectory = new File(outputDir);
        outputDirectory.delete();
        outputDirectory.mkdirs();
        try {
            PrintWriter dictionaryFile = new PrintWriter(new File(outputDirectory.getPath()+
                    File.separator+"Clusters_and_output.txt"));
            Iterator<String> termsIT = output.iterator();
            int size = clusters.size();
            dictionaryFile.write("Final Clusters after execution: "+System.getProperty("line.separator"));
            for(int i = 0; i < size; i++){
                if(clusters.get(i) instanceof EmptyCluster)
                    continue;
                dictionaryFile.write(clusters.get(i)+System.getProperty("line.separator"));
            }
            dictionaryFile.write("----------------------------------------------------------------------------"+
                    System.getProperty("line.separator"));
            dictionaryFile.write("Order of clustering that was done: "+System.getProperty("line.separator"));
            while(termsIT.hasNext()){
                dictionaryFile.write(termsIT.next()+System.getProperty("line.separator"));
            }
            dictionaryFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void cluster(double[][] simMatrix, int files){
        SimilarityPair previousMax = new SimilarityPair(-1, -1, -1);
        while(true){
            if(previousMax.getSimilarity() < 0.4 && previousMax.getSimilarity() > -0.01){
                break;
            }
            findMax(simMatrix, files);
            //System.out.println(maxSimilarity);
            int lowEnd = 0;
            int highEnd = 0;
            if(maxSimilarity.getColumn() > maxSimilarity.getRow()){
                highEnd = maxSimilarity.getColumn();
                lowEnd = maxSimilarity.getRow();
            } else {
                highEnd = maxSimilarity.getRow();
                lowEnd = maxSimilarity.getColumn();
            }
            for(int row = 0; row < files; row++){
                //adjust all values in the column
                //compare the value from the specific row, and the lowend and highend columns, select accordingly
                if(row == lowEnd || row == highEnd){
                    continue;
                }
                double avg = (simMatrix[row][lowEnd] + simMatrix[row][highEnd]) / 2.0;
                simMatrix[row][lowEnd] = avg;
            }
            for(int col = 0; col < files; col++){
                //adjust all the values for a specific row
                if(col == lowEnd || col == highEnd){
                    continue;
                }
                double avg = (simMatrix[lowEnd][col] + simMatrix[highEnd][col]) / 2.0;
                simMatrix[lowEnd][col] = avg;
            }
            for(int row = 0; row < files; row++){
                //adjust all values in the column
                simMatrix[row][highEnd] = 0.0;
            }
            for(int col = 0; col < files; col++){
                //adjust all the values for a specific row
                simMatrix[highEnd][col] = 0.0;
            }
            //sets the high end to an empty cluster as it was already used up
            output.add("Merging together clusters: "+clusters.get(lowEnd)+", "+clusters.get(highEnd));
            clusters.get(lowEnd).addCluster(clusters.get(highEnd));
            clusters.set(highEnd, new EmptyCluster());
            previousMax = maxSimilarity;
            maxSimilarity = new SimilarityPair(-1, -1, -1);
        }
    }

    private static void findMax(double[][] simMatrix, int files){
        for(int i = 0; i < files; i++){
            for(int j = 0; j < files; j++){
                //System.out.println(simMatrix[i][j]);
                if(i == j){
                    continue;
                }
                if(simMatrix[i][j] > maxSimilarity.getSimilarity()){
                    maxSimilarity = new SimilarityPair(i, j, simMatrix[i][j]);
                }
            }
        }
    }

    private static void findMin(double[][] simMatrix, int files){
        for(int i = 0; i < files; i++){
            for(int j = 0; j < files; j++){
                //System.out.println(simMatrix[i][j]);
                if(i == j){
                    continue;
                }
                if(simMatrix[i][j] < minSimilarity.getSimilarity() && simMatrix[j][i] == 0){
                    minSimilarity = new SimilarityPair(i, j, simMatrix[i][j]);
                }
            }
        }
    }

    private static double[][] calculateSimMatrix(int numFiles, Map<String, Map<String, Integer>> freq,
                                                 Map<String, Double> docMagnitudes, String inputDir){
        //all values are initialized to 0.0
        double[][] simMatrix = new double[numFiles][numFiles];
        for(int i = 0; i < numFiles; i++){
            //initialize all the clusters/documents
            clusters.add(new Cluster(i+1));
            for(int j = 0; j < numFiles; j++){
                //same document
                if(i == j){
                    continue;
                }
                //the other part of the triangle is already completed
                if(simMatrix[j][i] != 0.0){
                    continue;
                }
                String firstDocName = ""+(i+1);
                String secondDocName = ""+(j+1);
                if(firstDocName.length() == 1){
                    firstDocName = "00"+(i+1);
                } else if(firstDocName.length() == 2){
                    firstDocName = "0"+(i+1);
                }
                if(secondDocName.length() == 1){
                    secondDocName = "00"+(j+1);
                } else if(secondDocName.length() == 2){
                    secondDocName = "0"+(j+1);
                }
                String firstDoc = inputDir+"\\"+firstDocName+".html";
                String secondDoc = inputDir+"\\"+secondDocName+".html";
                double calculateCosineSim = cosineSim(firstDoc, secondDoc, freq, docMagnitudes);
                simMatrix[i][j] = calculateCosineSim;
            }
        }
        return simMatrix;
    }

    private static double cosineSim(String firstDoc, String secondDoc, Map<String, Map<String, Integer>> freq,
                                    Map<String, Double> docMagnitudes){
        Map<String, Integer> firstTokens = freq.get(firstDoc);
        Map<String, Integer> secondTokens = freq.get(secondDoc);
        Set<String> allTokens = tokenCounter.keySet();
        Iterator<String> it = allTokens.iterator();
        double dotProduct = 0.0;
        while(it.hasNext()){
            String token = it.next();
            double localMultiply = 0.0;
            if(firstTokens.containsKey(token) && secondTokens.containsKey(token)){
                localMultiply = firstTokens.get(token) * secondTokens.get(token);
            }
            dotProduct += localMultiply;
        }
        //System.out.println("dot product: "+dotProduct);
        double magnitudeProduct = (docMagnitudes.get(firstDoc) * docMagnitudes.get(secondDoc));
        //System.out.println("magnitude product: "+magnitudeProduct);
        return (dotProduct / magnitudeProduct);
    }

    private static Map<String, Double> calculateMagnitudes(Map<String, Map<String, Integer>> freq){
        Set<String> keys = freq.keySet();
        Iterator<String> it = keys.iterator();
        Map<String, Double> docMagnitudes = new HashMap<String, Double>();
        while(it.hasNext()){
            String doc = it.next();
            Map<String, Integer> tokensForDoc = freq.get(doc);
            Set<String> tokens = tokensForDoc.keySet();
            Iterator<String> tokensIT = tokens.iterator();
            double total = 0.0;
            while(tokensIT.hasNext()){
                String token = tokensIT.next();
                total += Math.pow(tokensForDoc.get(token), 2.0);
            }
            /*String docToPrint = doc.substring(doc.lastIndexOf("\\")+1);
            String docName = docToPrint.substring(0, docToPrint.lastIndexOf('.'));
            System.out.println(docName);*/
            double magnitude = Math.sqrt(total);
            docMagnitudes.put(doc, magnitude);
        }
        return docMagnitudes;
    }

    //returns a map where the:
    //key = path to the file, value = map with key as the token and it's value is the weight
    private static Map<String, Map<String, Double>> calculateWeights(int numFiles){
        long start = System.nanoTime();
        Map<String, Map<String, Double>> weights = new HashMap<String, Map<String, Double>>();
        //Iterate over all the files and look at the frequencies
        Set<String> keys = tokenizedFiles.keySet();
        Iterator<String> it = keys.iterator();
        while(it.hasNext()){
            String path = it.next();
            //the map of all the tokens and frequency for that token in the current document
            Map<String, Integer> fileTokens = tokenizedFiles.get(path);
            int numTokens = fileTokens.size();
            //map of all the calculated weights
            Map<String, Double> calculatedWeights = new HashMap<String, Double>();

            Set<String> tokens =fileTokens.keySet();
            Iterator<String> tokIt = tokens.iterator();

            while(tokIt.hasNext()){
                String token = tokIt.next();
                //calculate the tf values
                double normalizedTF = (fileTokens.get(token).doubleValue())/((double)numTokens);
                //calculate the idf values
                double takeLogOf = ((double)numFiles) / tokenCounter.get(token).doubleValue();
                double idf = Math.log(takeLogOf);
                //calculate the weight of the token
                double weight = normalizedTF * idf;
                //System.out.println(token + " "+normalizedTF + " "+ " "+takeLogOf +" "+ idf + " "+ weight);
                calculatedWeights.put(token, weight);
            }
            weights.put(path, calculatedWeights);
        }
        double end = System.nanoTime();
        double elapsed = (end - start) * 1.0e-09;
        System.out.println("Elapsed Time: "+elapsed+" seconds to calculate the weights");
        return weights;
    }

    private static void tokenize(String pathToFile){
        try {
            //collection of all the tokens in the specific document
            Set<String> tokensInDoc = new HashSet<String>();
            //all the tokens and their associated frequencies
            Map<String, Integer> allTokens = new HashMap<String, Integer>();
            InputStreamReader in = new InputStreamReader(new FileInputStream(pathToFile));
            BufferedReader br = new BufferedReader(in);
            StringBuilder content = new StringBuilder();
            String s = "";
            while((s = br.readLine()) != null){
                content.append(s);
            }
            String file = content.toString();
            //have to escape the HTML before splitting it
            file = StringEscapeUtils.unescapeHtml(file);
            //replace all of the punctuation first
            file = file.replaceAll("\\p{Punct}", " ");
            //split based on white spaces
            String[] splitted = file.split("\\s+");
            for(String curr : splitted){
                //if a stopword is found or a word that is only 1 charcater in length, then continue on and don't add it
                if(stopWords.contains(curr.trim().toLowerCase()) || curr.trim().toLowerCase().length() == 1){
                    continue;
                }
                //collect a set of all the tokens in the current document
                if(!tokensInDoc.contains(curr.toLowerCase().trim())){
                    tokensInDoc.add(curr.toLowerCase().trim());
                }
                //calculate the frequency of each token in each document
                if(allTokens.containsKey(curr.toLowerCase().trim())){
                    allTokens.put(curr.toLowerCase().trim(), allTokens.get(curr.toLowerCase().trim()).intValue()+1);
                } else {
                    allTokens.put(curr.toLowerCase().trim(), new Integer(1));
                }
            }
            //iterate over all the tokens, and add them to the counter for the term per document
            //if it is in the map, increment it
            //if not, then add it to the map with a set value of 1 first
            Iterator<String> it = tokensInDoc.iterator();
            while(it.hasNext()){
                String toCheck = it.next();
                if(tokenCounter.containsKey(toCheck)){
                    tokenCounter.put(toCheck, tokenCounter.get(toCheck).intValue()+1);
                } else {
                    tokenCounter.put(toCheck, new Integer(1));
                }
            }
            //add the frequencies to the map containing each document with a map of the token & its frequency
            tokenizedFiles.put(pathToFile, allTokens);
        } catch (Exception e){
            System.out.println("Error parsing this file: "+pathToFile);
            e.printStackTrace();
        }
    }
}
