package de.saar.coli.arranger;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class ConfigTest {
    @Test
    public void testConfigReader() throws FileNotFoundException {
        Config config = Arrange.loadConfig(null);
        assertEquals("Automatically arranged by AABA", config.getArranger());
        assertEquals(-20, config.getScores().getHarmonyLeaps());
    }
}
