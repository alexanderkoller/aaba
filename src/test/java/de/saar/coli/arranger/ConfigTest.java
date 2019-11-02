package de.saar.coli.arranger;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class ConfigTest {
    @Test
    public void testConfigReader() {
        InputStream is = getClass().getResourceAsStream("/config.yaml");
        assertNotNull(is);

        Reader r = new InputStreamReader(is);
        Config config = Config.read(r);

        assertEquals("Automatically arranged by Alexander's Arranger", config.getArranger());
        assertEquals(-20, config.getScores().getHarmonyJumps());
    }
}
