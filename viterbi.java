import java.io.*;
import java.util.*;

public class viterbi {

    public static void main(String[] args) throws IOException {
        // creates a viterbi object and runs all the training and creates their log probabilities
        viterbi counter = new viterbi();
        Map<String, Map<String, Double>> testingObs = counter.observationsMapMake("texts/brown-train-tags.txt","texts/brown-train-sentences.txt");
        Map<String, Map<String, Double>> testingTrans = counter.transitionsMapMake("texts/brown-train-tags.txt");
        Map<String, Map<String, Double>> testingProbsObs = counter.mapProbs(testingObs);
        Map<String, Map<String, Double>> testingProbsTrans = counter.mapProbs(testingTrans);
    }

    public Map<String, Map<String, Double>> transitionsMapMake(String tagFile) throws IOException {
        // creates the map of maps for the tranisitons
        Map<String, Map<String, Double>> transitionsMap = new HashMap<>();
        Map<String, Double> empty = new HashMap<>();
        BufferedReader tagReader = null;
        try {
            // reads the file and runs a while loop as long as it has a valid line with words
            tagReader = new BufferedReader(new FileReader(tagFile));
            String reading = tagReader.readLine();
            while (reading != null) {
                // splits the line and if the map does not have a # key it creates one and adds in the first word with a freq of 1 in the map of map
                // else it just adds in the word with a freq of 1
                String[] tagsInLine = reading.split(" ");
                if (!transitionsMap.containsKey("#")){
                    Map<String, Double> mapForMap = new HashMap<>();
                    transitionsMap.put("#", mapForMap);
                    transitionsMap.get("#").put(tagsInLine[0], 1.0);
                }
                else{
                    transitionsMap.get("#").put(tagsInLine[0], 1.0);
                }
                // for every word in the split if it is not in the mapkey set it creates a new map and
                // adds the word as the key and the empty map as the value
                for (int tags = 0; tags < tagsInLine.length - 1; tags++) {
                    if (!transitionsMap.containsKey(tagsInLine[tags])) {
                        Map<String, Double> mapForMap = new HashMap<>();
                        transitionsMap.put(tagsInLine[tags], mapForMap);
                    }
                    // if the map of map at word key does not contain the next word it is added into the map of map with a freq of 1
                    if (!transitionsMap.get(tagsInLine[tags]).containsKey(tagsInLine[tags + 1])){
                        transitionsMap.get(tagsInLine[tags]).put(tagsInLine[tags + 1], 1.0);
                    }
                    // if it is in the map of map it is updated by adding 1
                    else{
                        double ogVal = transitionsMap.get(tagsInLine[tags]).get(tagsInLine[tags + 1]);
                        transitionsMap.get(tagsInLine[tags]).put(tagsInLine[tags + 1], ogVal+1);
                    }
                }
                // reads the next line to continue the while loop to completion
                reading = tagReader.readLine();
            }
        }
        // return and close
        finally {
            tagReader.close();
        }
        return transitionsMap;
    }

    public Map<String, Map<String, Double>> observationsMapMake(String tagFile, String sentenceFile) throws IOException {
        // creates an empty map or maps
        Map<String, Map<String, Double>> observationsMap = new HashMap<>();
        BufferedReader tagReader = null;
        BufferedReader sentReader = null;
        try {
            // reads both of the parameters and while they both have valid lines with words it continues
            tagReader = new BufferedReader(new FileReader((tagFile)));
            sentReader = new BufferedReader(new FileReader((sentenceFile)));
            String tagReading = tagReader.readLine();
            String sentReading = sentReader.readLine();
            while (tagReading != null && sentReading != null) {
                // splits the lines and for every word in the line it continues
                String[] tagsInSentence = tagReading.split(" ");
                String[] wordsInSentence = sentReading.split(" ");
                for (int word = 0; word < wordsInSentence.length ; word++) {
                    // if the map of map we just created does not have the for loop word it creates a new mapa and adds it as the value and uses the word as the key
                    if (!observationsMap.containsKey(tagsInSentence[word])) {
                        Map<String, Double> mapForMap = new HashMap<>();
                        observationsMap.put(tagsInSentence[word], mapForMap);
                    }
                    // if the map of map does not have word from the for loop it is added into the map of map with a freq of 1
                    if (!observationsMap.get(tagsInSentence[word]).containsKey(wordsInSentence[word])) {
                        observationsMap.get(tagsInSentence[word]).put(wordsInSentence[word], 1.0);
                    } else {
                        // if it is already there its freq is updated by 1
                        observationsMap.get(tagsInSentence[word]).put(wordsInSentence[word], observationsMap.get(tagsInSentence[word]).get(wordsInSentence[word]) + 1.0);
                    }
                }
                // read the next lines in both files to continue the while loop to completion
                tagReading = tagReader.readLine();
                sentReading = sentReader.readLine();
            }
        } catch (Exception e) {
            System.out.println(e);
            return observationsMap;
        } finally {
            // close and return
            tagReader.close();
            sentReader.close();
        }
        return observationsMap;
    }

    public Map<String, Map<String, Double>> mapProbs(Map<String, Map<String, Double>> mapToProb) {
        // since it is a map of maps for every key in the first map it creates an int of 0 and loops through all their
        // values and gets their values and adds the freqs to the int
        for (String all : mapToProb.keySet()) {
            double sum = 0;
            for (String allVals : mapToProb.get(all).keySet()) {
                sum = sum + mapToProb.get(all).get(allVals);
            }
            // it loops through the values of the map of maps again and replaces their freqs with the log equivalent
            for (String allVals : mapToProb.get(all).keySet()) {
                double loggedKey = Math.log(mapToProb.get(all).get(allVals) / sum);
                mapToProb.get(all).put(allVals, loggedKey);
            }
        }
        // return
        return mapToProb;
    }
}
