package ru.kpfu.itis.analyzer;

/**
 * @author alexander.leontyev
 *         09.03.2018
 */
abstract class TextAnalyzer {

    public String analyzeText(String text) {
        String[] words = text.split("\\s+");
        StringBuilder analyzedWords = new StringBuilder();
        for (String word : words) {
            analyzedWords.append(analyzeWord(word)).append(" ");
        }
        return analyzedWords.toString();
    }

    public abstract String analyzeWord(String word);

}