package ru.kpfu.itis.parser;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ru.kpfu.itis.analyzer.MystemAnalyzer;
import ru.kpfu.itis.analyzer.PorterAnalyzer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;

import static ru.kpfu.itis.util.StringUtil.removeUnknownSymbols;

/**
 * @author alexander.leontyev
 *         01.03.2018
 */
public class Parser {

    private WebClient webClient;
    private MystemAnalyzer mystemAnalyzer;
    private PorterAnalyzer porterAnalyzer;

    private static final String HOME_PAGE_URL = "http://m.mathnet.ru";

    private static final String GET_LINKS_XPATH = "//td[@width='90%']/a[@class='SLink']";

    private static final String GET_ABSTRACT_XPATH = "//b[contains(text(),'Аннотация')]/following::text()" +
            "[preceding::b[1][contains(text(),'Аннотация')] and not(parent::b)]";

    private static final String GET_KEYWORDS_XPATH = "//b[contains(text(), 'Ключевые')]/following-sibling::i[1]//text()";

    public Parser() {
        webClient = new WebClient();
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        mystemAnalyzer = new MystemAnalyzer();
        porterAnalyzer = new PorterAnalyzer();
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

                String titleContent = articleLink.getTextContent();
                Element title = document.createElement("title");
                title.appendChild(document.createTextNode(titleContent));
                article.appendChild(title);

                titleContent = removeUnknownSymbols(titleContent);
                Element mystemTitle = document.createElement("mystem-title");
                mystemTitle.appendChild(document.createTextNode(mystemAnalyzer.analyzeText(titleContent)));
                article.appendChild(mystemTitle);

                Element porterTitle = document.createElement("porter-title");
                porterTitle.appendChild(document.createTextNode(porterAnalyzer.analyzeText(titleContent)));
                article.appendChild(porterTitle);

                StringBuilder abstractWords = new StringBuilder();
                List<Object> abstractList = articlePage.getByXPath(GET_ABSTRACT_XPATH);

                for (Object abstractItem : abstractList) {
                    abstractWords.append(abstractItem.toString());
                }

                Element abstractElem = document.createElement("abstract");
                abstractElem.appendChild(document.createTextNode(abstractWords.toString()));
                article.appendChild(abstractElem);

                String abstractContent = removeUnknownSymbols(abstractWords.toString());
                Element mystemAbstract = document.createElement("mystem-abstract");
                mystemAbstract.appendChild(document.createTextNode(mystemAnalyzer.analyzeText(abstractContent)));
                article.appendChild(mystemAbstract);

                Element porterAbstract = document.createElement("porter-abstract");
                porterAbstract.appendChild(document.createTextNode(porterAnalyzer.analyzeText(abstractContent)));
                article.appendChild(porterAbstract);

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
