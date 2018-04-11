package ru.kpfu.itis.index;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import static ru.kpfu.itis.util.StringUtil.removeUnknownSymbols;
import static ru.kpfu.itis.util.StringUtil.splitToWords;

/**
 * @author alexander.leontyev
 *         10.04.2018
 */
public class InvertedIndexBuilder {
    private DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

    public void buildIndex(String fileName) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        dbFactory.setNamespaceAware(true);
        dbFactory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();

        Document inputDoc = docBuilder.parse(new File(fileName));
        inputDoc.normalizeDocument();

        Document resultDoc = docBuilder.newDocument();
        Element rootElem = resultDoc.createElement("words");
        resultDoc.appendChild(rootElem);

        /* для заголовков */
        Element titleElem = resultDoc.createElement("title");
        rootElem.appendChild(titleElem);

        NodeList mystemTitles = inputDoc.getElementsByTagName("mystem-title");
        Element mystemTitleElem = createElement(mystemTitles, resultDoc, "mystem");
        titleElem.appendChild(mystemTitleElem);

        NodeList porterTitles = inputDoc.getElementsByTagName("porter-title");
        Element porterTitleElem = createElement(porterTitles, resultDoc, "porter");
        titleElem.appendChild(porterTitleElem);

        /* для аннотаций */
        Element abstractElem = resultDoc.createElement("abstract");
        rootElem.appendChild(abstractElem);

        NodeList mystemAbstracts = inputDoc.getElementsByTagName("mystem-abstract");
        Element mystemAbstractElem = createElement(mystemAbstracts, resultDoc, "mystem");
        abstractElem.appendChild(mystemAbstractElem);

        NodeList porterAbstracts = inputDoc.getElementsByTagName("mystem-abstract");
        Element porterAbstractElem = createElement(porterAbstracts, resultDoc, "porter");
        abstractElem.appendChild(porterAbstractElem);

        writeDocToFile(resultDoc, new File("inverted_index.xml"));
    }


    private Element createElement(NodeList nodeList, Document resultDoc, String tagName) {
        Element element = resultDoc.createElement(tagName);

        HashMap<String, HashSet<String>> map = new HashMap<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String[] words = splitToWords(removeUnknownSymbols(node.getTextContent().toLowerCase().trim()));
            String articleLink = node.getParentNode().getAttributes().getNamedItem("link").getTextContent();

            for (String word : words) {
                if (map.containsKey(word)) {
                    map.get(word).add(articleLink);
                } else {
                    HashSet<String> articles = new HashSet<>();
                    articles.add(articleLink);
                    map.put(word, articles);
                }
            }
        }

        for (String word : map.keySet()) {
            Element wordElem = resultDoc.createElement("word");
            wordElem.setAttribute("name", word);

            Element articlesElem = resultDoc.createElement("articles");
            articlesElem.setAttribute("count", String.valueOf(map.get(word).size()));

            map.get(word).forEach(articleLink -> {
                Element articleElem = resultDoc.createElement("article");
                articleElem.setTextContent(articleLink);
                articlesElem.appendChild(articleElem);
            });

            wordElem.appendChild(articlesElem);
            element.appendChild(wordElem);
        }

        return element;
    }

    private void writeDocToFile(Document doc, File file) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(source, result);
    }

}
