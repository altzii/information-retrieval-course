package ru.kpfu.itis.intersection;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Scanner;

/**
 * @author alexander.leontyev
 *         11.04.2018
 */
public class IntersectionSearchStarter {
    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
        System.out.println("Введите слова:");
        Scanner sc = new Scanner(System.in);
        System.out.println("Результат:\n" + new IntersectionSearch().intersectionSearch(sc.nextLine()));
    }
}
