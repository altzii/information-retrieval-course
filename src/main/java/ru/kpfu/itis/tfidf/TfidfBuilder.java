package ru.kpfu.itis.tfidf;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.kpfu.itis.analyzer.MystemAnalyzer;
import ru.kpfu.itis.index.InvertedIndexBuilder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.kpfu.itis.index.InvertedIndexBuilder.getAllArticlesLinks;
import static ru.kpfu.itis.util.StringUtil.removeUnknownSymbols;
import static ru.kpfu.itis.util.StringUtil.splitToWords;

/**
 * @author alexander.leontyev
 *         12.04.2018
 */
public class TfidfBuilder {
    public static final String ARTICLES_FILENAME = "articles_with_analyzers.xml";
    private DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    private MystemAnalyzer mystemAnalyzer = new MystemAnalyzer();

    public void buildTfidf(String input) throws ParserConfigurationException, IOException, SAXException {

        dbFactory.setNamespaceAware(true);
        dbFactory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();

        Document inputDoc = docBuilder.parse(new File(ARTICLES_FILENAME));
        NodeList mystemAbstracts = inputDoc.getElementsByTagName("mystem-abstract");
        NodeList mystemTitles = inputDoc.getElementsByTagName("mystem-title");

        HashMap<String, HashSet<String>> abstractsMap = InvertedIndexBuilder.getInvertedIndexMap(mystemAbstracts);
        HashMap<String, HashSet<String>> titlesMap = InvertedIndexBuilder.getInvertedIndexMap(mystemTitles);
        HashMap<String, HashSet<String>> map = (HashMap<String, HashSet<String>>) mergeMaps(abstractsMap, titlesMap);

        String[] words = input.toLowerCase().trim().split("\\s+");
        HashSet<String> articlesUnion = getArticlesUnion(words, map);

        HashMap<String, Double> scoresMap = new HashMap<>();

        System.out.println("Объединение статей: " + articlesUnion + "\n");

        for (String word : words) {
            System.out.println("Слово: " + word);

            if (articlesUnion != null) {
                articlesUnion.forEach((String article) -> {
                    double tfidf = calculateTf(word, article, inputDoc) *
                            calculateIdf(word, getAllArticlesLinks(inputDoc).size(), map);

                    Double score = scoresMap.get(article);

                    if (scoresMap.containsKey(article)) {
                        score += tfidf;
                    } else {
                        score = tfidf;
                    }

                    scoresMap.put(article, score);

                    System.out.println("Статья: " + article);
                    System.out.println("TF-IDF: " + tfidf + "\n");

                });
            }
        }

        System.out.println("************ Results ************\n");

        sortByValueDesc(scoresMap).forEach((article, score) ->
                System.out.println("Article: " + article + ", Score: " + score));


    }

    public HashSet<String> getArticlesUnion(String[] words, HashMap<String, HashSet<String>> map) {
        HashSet<String> articlesUnion = null;

        for (String word : words) {
            if (articlesUnion == null) {
                articlesUnion = map.get(word);
            } else {
                articlesUnion.addAll(map.get(word));
            }
        }
        return articlesUnion;
    }

    public String[] getArticleWords(String article, NodeList nodeList) {
        String[] words = null;

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String articleLink = node.getParentNode().getAttributes().getNamedItem("link").getTextContent();

            if (articleLink.equals(article)) {
                words = splitToWords(removeUnknownSymbols(node.getTextContent().toLowerCase().trim()));
            }
        }
        return words;
    }

    private double calculateTf(String word, String article, Document doc) {
        // число вхождений слова word в документ doc
        int count = 0;
        double zoneCoefficient = 0.0;
        boolean isInTitle = false;
        boolean isInAbstract = false;

        NodeList mystemAbstracts = doc.getElementsByTagName("mystem-abstract");
        NodeList mystemTitles = doc.getElementsByTagName("mystem-title");

        String[] abstractWords = getArticleWords(article, mystemAbstracts);
        String[] titleWords = getArticleWords(article, mystemTitles);

        for (String abstractWord : abstractWords) {
            if (abstractWord.equals(word)) {
                count++;
                isInAbstract = true;
            }
        }

        for (String titleWord : titleWords) {
            if (titleWord.equals(word)) {
                count++;
                isInTitle = true;
            }
        }

        if (isInAbstract) zoneCoefficient += 0.4;
        if (isInTitle) zoneCoefficient += 0.6;

        return count * zoneCoefficient / (abstractWords.length + titleWords.length);
    }

    private double calculateIdf(String word, Integer allArticlesCount,
                                HashMap<String, HashSet<String>> map) {
        // число документов, в которых встречается слово
        int articlesCount = map.get(word).size();

        return log((double) allArticlesCount / articlesCount, 2 );
    }

    private static Map<String, HashSet<String>> mergeMaps(HashMap<String, HashSet<String>> firstMap,
                                                          HashMap<String, HashSet<String>> secondMap) {
        return Stream.of(firstMap, secondMap)
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> {
                    HashSet<String> both = new HashSet<>(a);
                    both.addAll(b);
                    return both;
                }));
    }

    static double log(double x, int base) {
        return (Math.log(x) / Math.log(base));
    }

    private static Map<String, Double> sortByValueDesc(Map<String, Double> unsortMap) {

        List<Map.Entry<String, Double>> list =
                new LinkedList<>(unsortMap.entrySet());

        list.sort(Comparator.comparing(o -> (o.getValue())));
        Collections.reverse(list);

        Map<String, Double> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

}

