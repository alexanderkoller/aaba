package de.saar.coli.arranger;

import org.junit.Test;
import static org.junit.Assert.*;

public class KeyTest {
    @Test
    public void testKeyLookup() {
        assertNotNull(Key.lookup("C"));
        assertNotNull(Key.lookup("F#"));
    }

    @Test
    public void testParallelKeys() {
        assertEquals(Key.lookup("C"), Key.lookup("Am"));
    }

    @Test
    public void testAccidentals() {
        assertNoteSpelling("C", "C", "C");
        assertNoteSpelling("Gb", "C", "F#");

        assertNoteSpelling("B", "C", "B");
        assertNoteSpelling("B", "F", "B@");

        assertNoteSpelling("Eb", "B", "D#");
        assertNoteSpelling("Eb", "Bb", "Eb");

        assertNoteSpelling("F", "G", "F@");   // note that was accidental-modified in key -> spell with natural
        assertNoteSpelling("Bb", "G", "A#");  // note that is foreign to the key -> spell with accidental of key
    }

    private void assertNoteSpelling(String noteName, String key, String expectedSpelling) {
        Note note = Note.create(noteName + "4", 0);
        String repr = note.getNoteName(Key.lookup(key));
        assertEquals(expectedSpelling, repr);
    }
}
