import java.io.*;
import java.util.*;

public class vitberi_Decode{
    //creating global variables of testingObs and testingTrans in order to make the 'casting' into viterbi_Decode objects easier
    public Map<String, Map<String, Double>> testingObs;
    public Map<String, Map<String, Double>> testingTrans;

    // constructor in order to set global testingObs and testingTrans to parameters
    public vitberi_Decode( Map<String, Map<String, Double>> testingObs, Map<String, Map<String, Double>> testingTrans){
        this.testingObs = testingObs;
        this.testingTrans = testingTrans;
    }

    public static void main(String[] args) throws IOException {
        // created viterbi object in order to use it's methods ==> makes observation and transitions maps fixed with logs
        viterbi counter = new viterbi();
        Map<String, Map<String, Double>> testingObs = counter.observationsMapMake("texts/brown-test-tags.txt","texts/brown-test-sentences.txt");
        Map<String, Map<String, Double>> testingTrans = counter.transitionsMapMake("texts/brown-test-tags.txt");
        Map<String, Map<String, Double>> tempTestingObs = counter.mapProbs(testingObs);
        Map<String, Map<String, Double>> tempTestingTrans = counter.mapProbs(testingTrans);

        // created viterbi_decode object in order to use decode function and print out the bestPath using writeTags function
        vitberi_Decode vdObj = new vitberi_Decode(tempTestingObs, tempTestingTrans);
        BufferedReader sentences = new BufferedReader(new FileReader("texts/brown-test-sentences.txt"));
        ArrayList<ArrayList<String>> bestPaths = new ArrayList<>();
        String line;
        while ((line = sentences.readLine()) != null){
            bestPaths.add(vdObj.decode(line.split(" ")));
        }
        sentences.close();
        vdObj.writeTags(bestPaths);

        // tests how many tags are correct and incorrent using rightWrong function
        List<Integer> hold = vdObj.rightWrong("texts/brown-test-tags.txt","texts/output.txt");
        System.out.println(hold.get(0) + " is the total number of tags that match" );
        System.out.println(hold.get(1) + " is the total number of tags that do not match" );

        // creates Scanner and asks user to input sentences constantly to write into a file q will stop asking user
        // that file with all the sentences gets sent to tagify function in order to address proper tag to each word in
        // sentence, and will print the arraylist of arraylists to print out the tags
        String read;
        System.out.println("Input as many sentences as you'd like and press q when done.\nSentences should be in all lowercase and have a space before the period.");
        System.out.println("After each sentence press ENTER, and when you are done input 'q' then press ENTER.");

        Scanner in = new Scanner(System.in);
        BufferedWriter writeSentences = new BufferedWriter(new FileWriter("texts/Sentences.txt"));

        while(!(read = in.nextLine()).equals("q")){
            writeSentences.write(read + "\n");
        }
        writeSentences.close();

        for (ArrayList<String> eachList: vdObj.tagify("texts/Sentences.txt")){
            for(int i = 0; i<eachList.size(); i++){
                System.out.print(eachList.get(i) + " ");
            }
            System.out.println("\n");
        }
    }

    public ArrayList<String> decode(String[] toDecode){
        Map<String, Double> currScore = new HashMap<>(); // will have the current score for the prev tag
        ArrayList<Map<String, String>> backtrack = new ArrayList<>(); // Will have the N->NP things like that
        currScore.put("#", 0.0);
        // for all the words in the sentence
        for (int i = 0; i< toDecode.length; i++){
            // create map of scores that keeps track of each word's score and backtrack that keeps track of tags with best scores
            Map<String, Double> scores = new HashMap<>();
            // create a hashmap to put into backtrack that will hold the tags
            Map<String, String> backtrackAdd = new HashMap<String, String>();
            backtrack.add(backtrackAdd);
            // for each tag that 'comes out of' the word like n and np come out of #
            for(String state: currScore.keySet()){
                // check if the current state is a . or somethig that might not transition to anything
                if(!testingTrans.containsKey(state)){
                    continue;
                }
                for(String next: testingTrans.get(state).keySet()){
                    // calculating the score, transitions is the score of this current to the next
                    double transitionScore = testingTrans.get(state).get(next);
                    double score = currScore.get(state) + transitionScore;
                    // checking if obs has the word we want and then if it does we add the value to it
                    if (testingObs.get(next).containsKey(toDecode[i])) {
                        score += testingObs.get(next).get(toDecode[i]);
                    }
                    // otherwise we subtract unseen value
                    else{
                        score -= 100;
                    }
                    // checks for the biggest score to update for each unique tag
                    if(!scores.containsKey(next) || scores.get(next) < score) {
                        scores.put(next, score);
                        backtrackAdd.put(next, state);
                    }
                }
            }
            // update currScore to equal scores in order to move along down the tags
            currScore = scores;
        }

        // figuring out the biggest score among the last tags in order to see which tag we will backtrack from
        double temp = Double.NEGATIVE_INFINITY;
        String curr = "";
        for(String score: currScore.keySet()){
            if (currScore.get(score) > temp){
                curr = score;
                temp = currScore.get(score);
            }
        }

        // with figured out tag to backtrack from, we backtrack from there and add it to answer arraylist
        ArrayList<String> answer = new ArrayList<>();
        for(int i = backtrack.size() - 1; i >= 0; i--){
            String currValue = backtrack.get(i).get(curr);
            answer.add(curr);
            curr = currValue;
        }

        // we must reverse because it is added into answer wrong way
        Collections.reverse(answer);
        return answer;
    }

    public void writeTags(ArrayList<ArrayList<String>> a) throws IOException {
        // open up a writer and then for each array in arralysit of arraylists we will write the tag with a space
        // make sure to write in an enter for each time we finish reading an array
        BufferedWriter writer = new BufferedWriter(new FileWriter("texts/output.txt"));
        for (ArrayList<String> eachList: a){
            for(int i = 0; i<eachList.size(); i++){
                writer.write(eachList.get(i) + " ");
            }
            writer.write("\n");
        }
        writer.close();
    }

    public ArrayList<ArrayList<String>> tagify(String file) throws IOException {
        // create a viterbi object in order to use the obs maps, transition maps etc
        // create a reader in order to read from sentences.ttxt that has all of the user's sentences
        // for each sentence we add to bestPaths and return it (arraylist of arraylists in order to print in the main)
        viterbi counter = new viterbi();
        Map<String, Map<String, Double>> tempTestingObs = counter.mapProbs(testingObs);
        Map<String, Map<String, Double>> tempTestingTrans = counter.mapProbs(testingTrans);
        vitberi_Decode vdObj = new vitberi_Decode(tempTestingObs, tempTestingTrans);
        BufferedReader sentences = new BufferedReader(new FileReader(file));
        ArrayList<ArrayList<String>> bestPaths = new ArrayList<>();
        String line;
        while ((line = sentences.readLine()) != null){
            bestPaths.add(vdObj.decode(line.split(" ")));
        }
        sentences.close();

        return bestPaths;
    }

    public List<Integer> rightWrong(String tagsFile, String outputFile) throws IOException {
        // creates and empty list and 2 ints
        List<Integer> totalRightWrong = new ArrayList<>();
        BufferedReader tagReader = null;
        BufferedReader outReader = null;
        int right = 0;
        int wrong = 0;
        try {
            // reads the files and while they both have valid lines with words it continues
            tagReader = new BufferedReader(new FileReader(tagsFile));
            outReader = new BufferedReader(new FileReader(outputFile));
            String tagReading = tagReader.readLine();
            String outReading = outReader.readLine();
            while (tagReading != null && outReading != null) {
                // splits the line from each file
                String[] testTags = tagReading.split(" ");
                String[] outTags = outReading.split(" ");
                // for every tag that the lines have it compares them if they are the same right is incremented
                // if they are not the same wrong is incremented
                for (int word = 0; word < outTags.length; word++) {
                    if (Objects.equals(outTags[word], testTags[word])) {
                        right++;
                    } else {
                        wrong++;
                    }
                }
                // reads the next lines in each file to continue the while loop
                tagReading = tagReader.readLine();
                outReading = outReader.readLine();
            }
            // closes the files
            tagReader.close();
            outReader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // adds the int of correct as the first index
        // adds the int of incorrect as the second index
        // returns
        totalRightWrong.add(right);
        totalRightWrong.add(wrong);
        return totalRightWrong;
    }
}
