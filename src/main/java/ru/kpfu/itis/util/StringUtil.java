package ru.kpfu.itis.util;

/**
 * @author alexander.leontyev
 *         09.03.2018
 */
public class StringUtil {

    public static String removeUnknownSymbols(String string) {
        return string.replaceAll("[^А-Яа-я\\s+]", "");
    }

}
