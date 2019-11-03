package de.saar.coli.arranger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A key, such as C major or A minor.
 * Names of keys are like "Bb" and "F#m": they consist of the root note
 * plus a suffix for major or minor.
 */
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

    private final Map<String,Integer> accidentals;
    private final Set<Integer> notesInKey;
    private final Map<Integer,Integer> accidentalNoteToBaseNote;
    private final int direction;
    private String majorName;

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
            notesInKey.add(nWithAccidental.getRelativeNote());

            if( nWithAccidental.getRelativeNote() != n.getRelativeNote() ) {
                accidentalNoteToBaseNote.put(nWithAccidental.getRelativeNote(), n.getRelativeNote());
            }
        }
    }

    /**
     * Returns the notes in this key, as relative note numbers.
     *
     * @return
     */
    public Set<Integer> getNotesInKey() {
        return notesInKey;
    }

    /**
     * Returns 1 for keys that use sharps, -1 for keys that use flats.
     * By convention, C major and A minor count as sharp keys.
     *
     * @return
     */
    public int getDirection() {
        return direction;
    }

    /**
     * Returns a mapping from the accidental notes in this key
     * to their natural base notes. Both accidental and base notes
     * are represented as relative note numbers. For instance, in Bb major,
     * this mapping sends 10 (= Bb) to 11 (= B) and 3 (= Eb) to 4 (= E).
     * Notes which are not accidentals in this key do not appear
     * in the mapping; so the mapping can be used to check whether
     * a certain note in this key has an accidental or not.
     *
     * @return
     */
    public Map<Integer, Integer> getAccidentalNoteToBaseNote() {
        return accidentalNoteToBaseNote;
    }

    /**
     * Returns the accidental on the note with the given
     * canonical name (-1, 0, or +1).
     *
     * @param note
     * @return
     */
    public int getAccidentalForNote(String note) {
        Integer acc = accidentals.get(note);

        if( acc == null ) {
            return 0;
        } else {
            return acc;
        }
    }

    /**
     * Returns the base note for the given note.
     * If the note has an accidental in this key, the natural
     * version of the note is returned; so Eb &rarr; E in Bb major.
     * If the note doesn't have an accidental in the first
     * place, it is returned unmodified.
     *
     * @param relativeNote
     * @return
     */
    public int getBaseNote(int relativeNote) {
        Integer baseNote = accidentalNoteToBaseNote.get(relativeNote);

        if( baseNote == null ) {
            return relativeNote;
        } else {
            return baseNote;
        }
    }

    /**
     * Returns the key with the given name.
     *
     * @param name
     * @return
     */
    public static Key lookup(String name) {
        return keyTable.get(name);
    }

    private static final String[] BASE_NOTES;
    private static final Map<String,Key> keyTable;

    /**
     * Returns the name of this key as a major key.
     * Thus both C major and A minor are represented
     * as "C".
     *
     * @return
     */
    @Override
    public String toString() {
        return majorName;
    }
}
