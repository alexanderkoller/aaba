package de.saar.coli.arranger;

import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.*;

public class TestScore {
    @Test
    public void testNotes() {
        Score s = new Score("Test Song", "AK", "C", 4);

        s.addNote(0, Note.create("E5", 2));
        s.addNote(1, Note.create("C5", 2) );
        s.addNote(2, Note.create("G4", 2));
        s.addNote(3, Note.create("C4", 2));

        String str = s.toString();
        assertEquals(S1, str);
    }

    @Test
    public void testParser() throws IOException {
        AbcParser p = new AbcParser();
        Score score = p.read(new StringReader(ABC));

        assertEquals("Test Song", score.getTitle());
        assertEquals("AK", score.getComposer());
        assertEquals("C", score.getKey());
        assertEquals(4, score.getQuartersPerMeasure());

        List<Note> ld = score.getPart(1);
        List<Note> gold = List.of(
                Note.create("C3", 2),
                Note.create("D7", 2),
                Note.create("Eb4", 2),
                Note.create("Gb4", 2),
                Note.create("C4", 2),
                Note.create("D4",2)
                );

        assertEquals(gold, ld);

        assertEquals(Chord.lookup("C"), score.getChordAtTime(0));
        assertEquals(Chord.lookup("C"), score.getChordAtTime(7));
        assertEquals(Chord.lookup("G7"), score.getChordAtTime(8));
    }

    @Test
    public void testChords() {
        Score score = new Score("", "", "", 4);

        score.addChord(0, Chord.lookup("C"));
        score.addChord(4, Chord.lookup("G7"));

        assertEquals(Chord.lookup("C"), score.getChordAtTime(0));
        assertEquals(Chord.lookup("C"), score.getChordAtTime(1));
        assertNotEquals(Chord.lookup("C"), score.getChordAtTime(4));
        assertEquals(Chord.lookup("G7"), score.getChordAtTime(4));
        assertEquals(Chord.lookup("G7"), score.getChordAtTime(7));
    }

    private static final String ABC =
            "%abc-2.1\n" +
                    "T:Test Song\n" +
                    "C:AK\n" +
                    "M:4/4\n" +
                    "K:C\n" +
                    "\"C\" C,2 d''2 _E2 ^F2 | \"G7\" C2 D2 |]";

    private static final String S1 = "Tn: E5:2\n" +
            "Ld: C5:2\n" +
            "Br: G4:2\n" +
            "Bs: C4:2\n";

}
