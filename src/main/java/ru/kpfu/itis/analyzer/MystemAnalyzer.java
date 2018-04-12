package ru.kpfu.itis.analyzer;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author alexander.leontyev
 *         09.03.2018
 */
public class MystemAnalyzer extends TextAnalyzer {
    private static final String MYSTEM_BIN_PATH = System.getProperty("user.home") + "/bin/mystem";

    private ProcessBuilder processBuilder = new ProcessBuilder(MYSTEM_BIN_PATH,
            "-lnd", "-e", "utf-8",
            "--format", "text");

    @Override
    public String analyzeWord(String word) {
        String mystemAnswer = null;

        try {
            Process process = processBuilder.start();

            OutputStream in = process.getOutputStream();
            InputStream out = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(out, StandardCharsets.UTF_8));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(in, StandardCharsets.UTF_8));

            writer.write(word + "\n");
            writer.flush();
            mystemAnswer = reader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mystemAnswer;
    }
}
