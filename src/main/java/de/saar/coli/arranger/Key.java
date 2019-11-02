package de.saar.coli.arranger;

import com.google.common.base.MoreObjects;

import java.util.*;

public class Key {
    static {
        keyTable = new HashMap<>();
        BASE_NOTES = new String[] { "C", "D", "E", "F", "G", "A", "B"};

        add("C", "Am", new Key(+1));
        add("G", "Em", new Key(+1, "F"));
        add("D", "Bm", new Key(+1, "F", "C"));
        add("A", "F#m", new Key(+1, "F", "C", "G"));
        add("E", "C#m", new Key(+1, "F", "C", "G", "D"));
        add("B", "G#m", new Key(+1, "F", "C", "G", "D", "A"));
        add("F#", "D#m", new Key(+1, "F", "C", "G", "D", "A", "E"));
        add("F", "Dm", new Key(-1, "B"));
        add("Bb", "Gm", new Key(-1, "B", "E"));
        add("Eb", "Cm", new Key(-1, "B", "E", "A"));
        add("Ab", "Fm", new Key(-1, "B", "E", "A", "D"));
        add("Db", "Bbm", new Key(-1, "B", "E", "A", "D", "G"));
        add("Gb", "Ebm", new Key(-1, "B", "E", "A", "D", "G", "C"));
    }

    public final Map<String,Integer> accidentals;
    public final Set<Integer> notesInKey;
    public final Map<Integer,Integer> accidentalNoteToBaseNote;
    public final int direction;
    public String majorName;

    private static void add(String majorName, String minorName, Key key) {
        keyTable.put(majorName, key);
        keyTable.put(minorName, key);
        key.majorName = majorName;
    }



    private Key(int direction, String... notesWithAccidentals) {
        accidentals = new HashMap<>();
        this.direction = direction;

        for( String note : notesWithAccidentals ) {
            accidentals.put(note, direction);
        }

        notesInKey = new HashSet<>();
        accidentalNoteToBaseNote = new HashMap<>();
        for( String note : BASE_NOTES ) {
            Note n = Note.create(note, 0, 0);
            Note nWithAccidental = n.add(getAccidentalForNote(note));
            notesInKey.add(nWithAccidental.getAbsoluteNote() % 12);

            if( nWithAccidental.getRelativeNote() != n.getRelativeNote() ) {
                accidentalNoteToBaseNote.put(nWithAccidental.getRelativeNote(), n.getRelativeNote());
            }
        }
    }

    public int getAccidentalForNote(String note) {
        Integer acc = accidentals.get(note);

        if( acc == null ) {
            return 0;
        } else {
            return acc;
        }
    }

    public int getBaseNote(int relativeNote) {
        Integer baseNote = accidentalNoteToBaseNote.get(relativeNote);

        if( baseNote == null ) {
            return relativeNote;
        } else {
            return baseNote;
        }
    }

    public static Key lookup(String name) {
        return keyTable.get(name);
    }

    private static final String[] BASE_NOTES;
    private static final Map<String,Key> keyTable;

    @Override
    public String toString() {
        return majorName;
    }
}
