package ru.kpfu.itis.parser;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;

/**
 * @author alexander.leontyev
 *         01.03.2018
 */
public class Parser {

    private WebClient webClient;

    private static final String HOME_PAGE_URL = "http://m.mathnet.ru";

    private static final String GET_LINKS_XPATH = "//td[@width='90%']/a[@class='SLink']";

    private static final String GET_ABSTRACT_XPATH = "//b[contains(text(),'Аннотация')]/following::text()" +
            "[preceding::b[1][contains(text(),'Аннотация')] and not(parent::b)]";

    private static final String GET_KEYWORDS_XPATH = "//b[contains(text(), 'Ключевые')]/following-sibling::i[1]//text()";

    public Parser() {
        webClient = new WebClient();
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);
    }

    public Document parsePage(String parsePageUrl, Integer year) throws ParserConfigurationException {

        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        documentFactory.setNamespaceAware(true);
        documentFactory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder docBuilder = documentFactory.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        try {
            HtmlPage parsePage = webClient.getPage(parsePageUrl);

            List<HtmlAnchor> articleLinks = parsePage.getByXPath(GET_LINKS_XPATH);

            Element articles = document.createElement("articles");
            articles.setAttribute("link", parsePageUrl);
            articles.setAttribute("year", String.valueOf(year));
            document.appendChild(articles);

            for (HtmlAnchor articleLink : articleLinks) {
                String articlePageUrl = HOME_PAGE_URL + articleLink.getHrefAttribute();
                HtmlPage articlePage = webClient.getPage(articlePageUrl);

                Element article = document.createElement("article");
                article.setAttribute("link", articlePageUrl);

                Element title = document.createElement("title");
                title.appendChild(document.createTextNode(articleLink.getTextContent()));
                article.appendChild(title);

                StringBuilder abstractWords = new StringBuilder();
                List<Object> abstractList = articlePage.getByXPath(GET_ABSTRACT_XPATH);

                for (Object abstractItem : abstractList) {
                    abstractWords.append(abstractItem.toString());
                }

                Element abstractElem = document.createElement("abstract");
                abstractElem.appendChild(document.createTextNode(abstractWords.toString()));
                article.appendChild(abstractElem);

                Element keywordsElem = document.createElement("keywords");

                List<Object> keywordsList = articlePage.getByXPath(GET_KEYWORDS_XPATH);
                for (Object keywordItem : keywordsList) {
                    keywordsElem.appendChild(document.createTextNode(keywordItem.toString()));
                }

                article.appendChild(keywordsElem);
                articles.appendChild(article);
            }
            document.normalizeDocument();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            webClient.close();
        }

        return document;
    }
}
