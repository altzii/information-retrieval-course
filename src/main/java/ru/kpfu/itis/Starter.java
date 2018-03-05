package ru.kpfu.itis;

import org.w3c.dom.Document;
import ru.kpfu.itis.parser.Parser;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;

import static ru.kpfu.itis.util.FileUtil.currentTimeXmlFileName;
import static ru.kpfu.itis.util.FileUtil.writeDocumentToFile;

/**
 * @author alexander.leontyev
 *         01.03.2018
 */
public class Starter {
    private static final String PARSE_PAGE_URL = "http://www.mathnet.ru/php/archive.phtml?jrnid=uzku&wshow=issue&" +
            "bshow=contents&series=0&year=2017&volume=159&issue=1&option_lang=rus&bookID=1681";

    public static void main(String[] args) throws ParserConfigurationException, TransformerException {
        Document document = new Parser().parsePage(PARSE_PAGE_URL, 2017);
        writeDocumentToFile(document, new File(currentTimeXmlFileName()));
    }
}
