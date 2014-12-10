package searcher;

import org.javatuples.Pair;

import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class SingleFilePatternSearcher implements Runnable{
    private final String m_filePath;
    private final String m_stream;
    private final char[] m_charStream;
    private final int m_index;
    private final Pattern m_allDelimiters = Pattern.compile("[\\.,:;\\- ]+");

    public SingleFilePatternSearcher(String filePath, String charStream, int index) {
        m_filePath = filePath;
        m_stream = charStream;
        m_charStream = charStream.toCharArray();
        m_index = index;
    }

    public void findWordSteamForEveryFirstCharInEveryNthWords() {
        List<Pair<String, Integer>>[] fileAsIndexArray = fileToIndexWordListArray(m_index);

        Pair<String, Integer>[] patternSearchResults;
        int matchResults = 0;

        for (int i = 0; i < m_index; i++) {
            patternSearchResults = findPatternInReaderSource(fileAsIndexArray[i]);
            if (patternSearchResults != null) {
                printPatternSearchResults(patternSearchResults);
                ++matchResults;
            }
        }
        if (matchResults == 0) {
            System.out.printf("[%s]: No match found\n", m_filePath);
        }
    }

    private List<Pair<String, Integer>>[] fileToIndexWordListArray(int index){
        List<Pair<String, Integer>>[] fileAsIndexListArray = new List[index];
        int totalCounter = 0;
        int counter = 0;
        String str;

        try (Scanner reader = new Scanner(new File(m_filePath)).useDelimiter(m_allDelimiters)) {
            while (reader.hasNext()) {
                str = reader.next();
                if (!str.isEmpty()) {
                    if (fileAsIndexListArray[counter] == null) {
                        fileAsIndexListArray[counter] = new ArrayList<>();
                    }
                    fileAsIndexListArray[counter].add(Pair.with(str, totalCounter));
                    totalCounter++;
                    counter = (counter + 1) % index;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return fileAsIndexListArray;
    }

    private List<String>[] fileToWordListArray(int index) {
        List<String>[] fileAsWordList = new List[index];
        int counter = 0;
        String str;
        try (Scanner reader = new Scanner(new File(m_filePath)).useDelimiter(m_allDelimiters)) {
            while (reader.hasNext()) {
                str = reader.next();
                if (! str.isEmpty()) {
                    if (fileAsWordList[counter] == null) {
                        fileAsWordList[counter] = new ArrayList<>();
                    }
                    fileAsWordList[counter].add(str);
                    counter = (counter + 1) % index;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return fileAsWordList;
    }

    private void printPatternSearchResults(String[] potentialWordStream) {
        if (potentialWordStream == null) {
            System.out.printf("[%s]: No match found", m_filePath);
        } else {
            StringBuffer resultAsString = new StringBuffer();
            for (int i = 0; i < potentialWordStream.length; i++) {
                resultAsString.append(potentialWordStream[i] + " ");
            }
            System.out.printf("[%s]: %s", m_filePath, resultAsString.toString());
        }
    }

    private void printPatternSearchResults(Pair<String, Integer>[] potentialWordStream) {
        if (potentialWordStream == null) {
            System.out.printf("[%s]: No match found\n", m_filePath);
        } else {
            System.out.printf("[%s]: Found match for <%s>:\n\t", m_filePath, m_stream);
            printWholeChunk(potentialWordStream[0].getValue1(), potentialWordStream[m_charStream.length - 1].getValue1(), m_index);
        }
    }

    private void printWholeChunk(Integer chunkStart, Integer chunkEnd, int searchIndex) {
        try (Scanner reader = new Scanner(new FileReader(new File(m_filePath))).useDelimiter(m_allDelimiters)) {
            int index = 0;
            while (reader.hasNext() && index <= chunkEnd) {
                String str = reader.next();
                if (index >= chunkStart){
                    if (index % searchIndex == 0) {
                        System.out.printf("<%s [%s]> ", str, index);
                    } else {
                        System.out.print(str + " ");
                    }
                }
                index++;
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private Pair<String, Integer>[] findPatternInReaderSource(List<Pair<String, Integer>> words) {
        Pair<String, Integer>[] potentialWordStream = new Pair[m_charStream.length];
        Pair<String, Integer>[] optionalWordStream = new Pair[m_charStream.length];
        int potentialIndex = 0;
        int optionalIndex = 0;
        String str;

        Iterator<Pair<String, Integer>> wordIterator = words.iterator();
        while(wordIterator.hasNext() && potentialIndex < m_charStream.length){
            Pair<String, Integer> nextPair = wordIterator.next();
            str = nextPair.getValue0();
            if (! str.isEmpty() && str.toCharArray()[0] == m_charStream[potentialIndex]) {
                if (potentialWordStream[potentialIndex] == null) {
                    potentialWordStream[potentialIndex] = new Pair<>(str, nextPair.getValue1());
                } else {
                    potentialWordStream[potentialIndex].setAt0(str);
                    potentialWordStream[potentialIndex].setAt1(nextPair.getValue1());
                }
                if (potentialIndex > 0 && str.toCharArray()[0] == m_charStream[optionalIndex]){
                    if (optionalWordStream[optionalIndex] == null) {
                        optionalWordStream[optionalIndex] = new Pair<>(str, nextPair.getValue1());
                    } else {
                        optionalWordStream[optionalIndex].setAt0(str);
                        optionalWordStream[optionalIndex].setAt1(nextPair.getValue1());
                        optionalIndex++;
                    }
                }
                potentialIndex++;
            } else if (str.toCharArray()[0] == m_charStream[optionalIndex]){
                if (optionalWordStream[optionalIndex] == null) {
                    optionalWordStream[optionalIndex] = new Pair<>(str, nextPair.getValue1());
                } else {
                    optionalWordStream[optionalIndex].setAt0(str);
                    optionalWordStream[optionalIndex].setAt1(nextPair.getValue1());
                }
                potentialWordStream = optionalWordStream;
                potentialIndex = optionalIndex + 1;
                optionalIndex = 0;
            } else {
                potentialIndex = 0;
                optionalIndex = 0;
            }
        }
        if (potentialWordStream[m_charStream.length -1] != null) {
            return potentialWordStream;
        }
        return null;
    }

    private String[] findPatternInReaderSource(Collection<String> words) {
        String[] potentialWordStream = new String[m_charStream.length];
        String[] optionalWordStream = new String[m_charStream.length];
        int potentialIndex = 0;
        int optionalIndex = 0;
        String str;


        Iterator<String> wordIterator = words.iterator();
        while(wordIterator.hasNext() && potentialIndex < m_charStream.length){
            str = wordIterator.next();
            if (! str.isEmpty() && str.toCharArray()[0] == m_charStream[potentialIndex]) {
                potentialWordStream[potentialIndex] = str;
                if (potentialIndex > 0 && str.toCharArray()[0] == m_charStream[optionalIndex]){
                    optionalWordStream[optionalIndex] = str;
                    optionalIndex++;
                }
                potentialIndex++;
            } else if (str.toCharArray()[0] == m_charStream[optionalIndex]){
                optionalWordStream[optionalIndex] = str;
                potentialWordStream = optionalWordStream;
                potentialIndex = optionalIndex + 1;
                optionalIndex = 0;
            } else {
                potentialIndex = 0;
                optionalIndex = 0;
            }
        }
        if (potentialWordStream[m_charStream.length -1] != null) {
            return potentialWordStream;
        }
        return null;
    }

    @Override
    public void run() {
        findWordSteamForEveryFirstCharInEveryNthWords();
    }
}
