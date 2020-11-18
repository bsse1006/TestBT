package naturalLanguageProcessor;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TextProcessor
{
    private String unprocessedText;
    private List<String> keywords = new ArrayList<>();
    private List<String> tokens = new ArrayList<>();
    private List<String> stopWordsList = new ArrayList<>();

    public TextProcessor(String unprocessedText) throws Exception {
        this.unprocessedText = unprocessedText;
        textProcessing();
    }

    public List<String> getKeywords() {
        return keywords;
    }

    private String readFileAsString(String fileName)throws Exception
    {
        String data = "";
        data = new String(Files.readAllBytes(Paths.get(fileName)));
        return data;
    }

    private void stopWordsFileToList () throws Exception {

        String data = readFileAsString("src/files/stopWords.txt");

        StringTokenizer st = new StringTokenizer(data);
        while (st.hasMoreTokens())
        {
            stopWordsList.add(st.nextToken());
        }
    }

    private void textProcessing () throws Exception {
        stopWordsFileToList();
        splitCamelCaseAndTokenization();
        removeStopWordsAndStem();
    }


    private void stemTerm (List<String> words) {
        PorterStemmer stemmer = new PorterStemmer();
        for(String value: words){
            stemmer.setCurrent(value);
            stemmer.stem();
            keywords.add(stemmer.getCurrent());
        }
    }

    private void splitCamelCaseAndTokenization ()
    {
        StringTokenizer st = new StringTokenizer(unprocessedText);

        unprocessedText = "";

        while (st.hasMoreTokens())
        {
            int counter = 0; //to check if camelCase
            String token = st.nextToken();
            for (String w : token.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
                unprocessedText = w + ' ' + unprocessedText;
                counter++;
            }
            if (counter>1) //camelCase
            {
                unprocessedText = token + ' ' + unprocessedText;
            }
        }
    }

    private void removeStopWordsAndStem () throws Exception {

        //CharArraySet stopSet = EnglishAnalyzer.getDefaultStopSet();
        CharArraySet stopSet = new CharArraySet(stopWordsList, true);

        Analyzer analyzer = new StandardAnalyzer();

        TokenStream tokenStream = analyzer.tokenStream("myfield", new StringReader(unprocessedText));

        tokenStream = new StopFilter(tokenStream, stopSet);
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();

        while (tokenStream.incrementToken()) {
            String term = charTermAttribute.toString();
            tokens.add(term);
        }

        stemTerm(tokens);
    }
}