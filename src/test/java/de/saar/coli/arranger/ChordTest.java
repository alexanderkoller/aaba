package de.saar.coli.arranger;

import com.sun.tools.javac.util.List;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class ChordTest {
    @Test
    public void testLookup() {
        Chord chord = Chord.lookup("Db7");
        assertEquals("Db", Note.getNoteName(chord.getRoot()));
        assertEquals(Chord.ChordType.SEVENTH, chord.getType());

        chord = Chord.lookup("B");
        assertEquals("B", Note.getNoteName(chord.getRoot()));
        assertEquals(Chord.ChordType.MAJOR, chord.getType());
    }

    @Test
    public void testNoteSet() {
        Chord chord = Chord.lookup("Db7");
        Set<Integer> gold = new HashSet<>(List.of(Note.getNoteId("Db"), Note.getNoteId("F"), Note.getNoteId("Ab"), Note.getNoteId("B")));
        assertEquals(gold, chord.getNotes());

        chord = Chord.lookup("B");
        gold = new HashSet<>(List.of(Note.getNoteId("B"), Note.getNoteId("Eb"), Note.getNoteId("Gb")));
        assertEquals(gold, chord.getNotes());
    }
}
