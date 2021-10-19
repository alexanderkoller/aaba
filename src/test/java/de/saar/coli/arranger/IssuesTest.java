package de.saar.coli.arranger;

import de.saar.coli.arranger.abc.AbcParser;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IssuesTest {
//    @Test
    public void testIssue7() throws AbcParser.AbcParsingException, IOException {
        Score score = new AbcParser().read(new InputStreamReader(this.getClass().getResourceAsStream("/issue7.abc")));
    }
}