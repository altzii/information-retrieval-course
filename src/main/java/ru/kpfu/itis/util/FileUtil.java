package ru.kpfu.itis.util;

import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static javax.xml.transform.OutputKeys.INDENT;

/**
 * @author alexander.leontyev
 *         01.03.2018
 */
public class FileUtil {

    public static String currentTimeXmlFileName() {
        String fileName = new SimpleDateFormat("yyyyMMddHHmm'.xml'").format(new Date());
        return System.getProperty("user.dir") + "/" + fileName;
    }

    public static void writeDocumentToFile(Document document, File file) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(file);
            transformer.setOutputProperty(INDENT, "yes");
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }

    }
}
