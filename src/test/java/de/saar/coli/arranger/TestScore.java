package de.saar.coli.arranger;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestScore {
    @Test
    public void testNotes() {
        Score s = new Score();

        s.addNote(0, Note.create("E5", 2));
        s.addNote(1, Note.create("C5", 2) );
        s.addNote(2, Note.create("G4", 2));
        s.addNote(3, Note.create("C4", 2));

        String str = s.toString();
        assertEquals(S1, str);
    }


    private static final String S1 = "Tn: E5:2\n" +
            "Ld: C5:2\n" +
            "Br: G4:2\n" +
            "Bs: C4:2\n";

}
