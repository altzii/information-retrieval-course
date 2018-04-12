package ru.kpfu.itis.intersection;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.kpfu.itis.analyzer.MystemAnalyzer;
import ru.kpfu.itis.index.InvertedIndexBuilder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author alexander.leontyev
 *         11.04.2018
 */
public class IntersectionSearch {
    private static final String ARTICLES_FILENAME = "articles_with_analyzers.xml";
    private DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    private MystemAnalyzer mystemAnalyzer = new MystemAnalyzer();

    public HashSet<String> intersectionSearch(String input) throws ParserConfigurationException, IOException, SAXException {
        dbFactory.setNamespaceAware(true);
        dbFactory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();

        Document inputDoc = docBuilder.parse(new File(ARTICLES_FILENAME));
        NodeList mystemAbstracts = inputDoc.getElementsByTagName("mystem-abstract");

        HashSet<String> searchResult = InvertedIndexBuilder.getAllArticlesLinks(inputDoc);

        HashMap<String, HashSet<String>> map = InvertedIndexBuilder.getInvertedIndexMap(mystemAbstracts);

        String[] words = input.toLowerCase().trim().split("\\s+");

        for (String word : words) {
            boolean isExcept = word.startsWith("-");

            if (isExcept) word = word.replace("-", "");
            word = mystemAnalyzer.analyzeWord(word);

            if (!isExcept) {
                searchResult.retainAll(map.get(word));
            } else {
                searchResult.removeAll(map.get(word));
            }
        }

        return searchResult;
    }
}
