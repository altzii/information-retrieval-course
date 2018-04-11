package ru.kpfu.itis.index;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * @author alexander.leontyev
 *         10.04.2018
 */
public class IndexBuilderStarter {
    private static final String CONTENT_FILE_NAME = "articles_with_analyzers.xml";

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        new InvertedIndexBuilder().buildIndex(CONTENT_FILE_NAME);
    }

}
