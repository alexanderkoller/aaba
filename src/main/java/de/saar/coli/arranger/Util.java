package de.saar.coli.arranger;

import de.saar.coli.arranger.abc.AbcWriter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class Util {
    public static String slurp(String resourceName) {
        Reader r = new InputStreamReader(AbcWriter.class.getResourceAsStream(resourceName));
        return slurp(r);
    }

    /**
     * Reads the entire Reader into a string and returns it.
     *
     * @param reader
     * @return
     */
    public static String slurp(Reader reader) {
        try {
            char[] arr = new char[8 * 1024];
            StringBuilder buffer = new StringBuilder();
            int numCharsRead;
            while ((numCharsRead = reader.read(arr, 0, arr.length)) != -1) {
                buffer.append(arr, 0, numCharsRead);
            }
            reader.close();

            return buffer.toString();
        } catch (IOException e) {
            return null;
        }
    }
}
