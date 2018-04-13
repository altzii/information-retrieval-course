package ru.kpfu.itis.tfidf;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Scanner;

/**
 * @author alexander.leontyev
 *         13.04.2018
 */
public class TfidfBuilderStarter {

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
        System.out.println("Введите слова:");
        Scanner sc = new Scanner(System.in);
        new TfidfBuilder().buildTfidf(sc.nextLine());
    }

}

