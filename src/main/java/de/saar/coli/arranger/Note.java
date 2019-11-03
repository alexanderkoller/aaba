package de.saar.coli.arranger;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A note with a certain pitch and duration. Durations are always measured in
 * integer multiples of eighth notes for now.<p>
 *
 * We distinguish the following terminology:
 * <ul>
 *     <li>An <i>absolute note number</i> is an integer number that uniquely
 *     determines the note e.g. on a keyboard. Technically, it is the note's MIDI number,
 *     so e.g. the absolute number 60 represents middle C.
 *     </li>
 *     <li>The <i>relative note number</i> of a note is its position within the
 *     octave. C is always mapped to 0, C#/Db is mapped to 1, and so on. The notes C3 and C4
 *     have different absolute numbers (48 and 60, respectively), but both have the relative
 *     number 0. You can map from absolute to relative note numbers by modulo 12.
 *     </li>
 *     <li>An object of the class Note represents a note based on its absolute
 *     note number and its duration in eighth notes.</li>
 * </ul>
 *
 */
public class Note {
    private int midiNumber;
    private int duration;

    private static List<String> NOTE_NAMES = Arrays.asList(new String[] { "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"});
    private static Pattern NOTE_PATTERN = Pattern.compile("([A-Za-z]+)([0-9]+)");

    /**
     * Creates a note with the given absolute note number and duration.
     *
     * @param absoluteNote
     * @param duration
     * @return
     */
    public static Note create(int absoluteNote, int duration) {
        Note ret = new Note();
        ret.midiNumber = absoluteNote;
        ret.duration = duration;
        return ret;
    }

    /**
     * Creates a note based on its relative note number and the octave.
     * @param relativeNote
     * @param octave
     * @param duration
     * @return
     */
    public static Note create(int relativeNote, int octave, int duration) {
        return create(12*(octave+1) + relativeNote, duration);
    }

    /**
     * Creates a note based on a string representing its relative note
     * (e.g. "C" or "Db") and octave. Note that all halftones need to be
     * spelled with b's; so Db and not C#.
     *
     * @param relativeNote
     * @param octave
     * @param duration
     * @return
     */
    public static Note create(String relativeNote, int octave, int duration) {
        return create(getNoteId(relativeNote), octave, duration);
    }

    /**
     * Creates a note based on its scientific name (e.g. "C4"). Note that all halftones need to be
     *  spelled with b's; so Db and not C#.
     * @param noteWithOctave
     * @param duration
     * @return
     */
    public static Note create(String noteWithOctave, int duration) {
        Matcher m = NOTE_PATTERN.matcher(noteWithOctave);

        if( m.matches() ) {
            return create(m.group(1), Integer.parseInt(m.group(2)), duration);
        } else {
            return null;
        }
    }

    /**
     * Returns the absolute note number of this note.
     *
     * @return
     */
    public int getAbsoluteNote() {
        return midiNumber;
    }

    /**
     * Returns the absolute value of the distance to the other
     * note, in semitones.
     *
     * @param other
     * @return
     */
    public int getAbsoluteDistance(Note other) {
        return Math.abs(midiNumber - other.midiNumber);
    }


    /**
     * Returns the note that is "offset" semitones above this note.
     * If "offset" is negative, the note is shifted down.
     *
     * @param offset
     * @return
     */
    public Note add(int offset) {
        return Note.create(midiNumber+offset, duration);
    }

    /**
     * Returns the relative note number of this note.
     *
     * @return
     */
    public int getRelativeNote() {
        return midiNumber % 12;
    }

    /**
     * Returns the octave of this note (C4 is middle C).
     *
     * @return
     */
    public int getOctave() {
        return midiNumber/12 - 1;
    }

    /**
     * Returns the duration of this note in eighth notes.
     *
     * @return
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Returns a canonical note name for the given relative note number.
     * Because we have no information about the key in this method,
     * accidentals are always spelled as flats; so relative number 1
     * is "Db" and not "C#".
     *
     * @param relativeNote
     * @return
     */
    public static String getNoteName(int relativeNote) {
        return NOTE_NAMES.get(relativeNote);
    }

    /**
     * Returns the name of this note in the given key. See {@link #getNoteName(Key, boolean)}
     * for details.
     *
     * @param key
     * @return
     */
    public String getNoteName(Key key) {
        return getNoteName(key, false);
    }

    /**
     * Returns the name of this note in the given key. Accidentals are spelled
     * with respect to the key; so relative note number 1 is spelled "Db" in
     * Bb minor and "C#" in A major. Naturals are spelled as "@", and are
     * usually suppressed, so relative note 2 is just "D" in C major.
     * However, you can force the note to be spelled with the natural, "D@",
     * by setting the parameter "forceNatural" to true. This can be useful
     * when spelling a chord note in the context of a different key.
     *
     * @param key
     * @return
     */
    public String getNoteName(Key key, boolean forceNatural) {
        assert key != null;

        if( key.getNotesInKey().contains(getRelativeNote()) ) {
            // note exists in key

            Integer baseNote = key.getAccidentalNoteToBaseNote().get(getRelativeNote());
            if (baseNote == null) {
                // note was not accidental-modified away from base note, so does not need an accidental
                return NOTE_NAMES.get(getRelativeNote()) + (forceNatural ? "@" : "");
            } else {
                // note in key, with accidental -> look up spelling in key
                int accidental = getRelativeNote() - baseNote;
                return NOTE_NAMES.get(baseNote) + accidentalString(accidental, key);
            }
        } else { // note does not exist in key
            // does accidental-modified note exist in key as an accidental-modified note?
            int noteWithAccidental = (getRelativeNote() + key.getDirection()) % 12;
            if( key.getAccidentalNoteToBaseNote().containsKey(noteWithAccidental)) {
                // spell as natural
                return NOTE_NAMES.get(getRelativeNote()) + "@";
            } else {
                // note is foreign to key, spell with accidental of usual type in key
                int baseNote = (getRelativeNote() - key.getDirection())%12;
                return NOTE_NAMES.get(baseNote) + accidentalString(key.getDirection(), key);
            }
        }
    }

    private static String accidentalString(int accidental, Key key) {
        if( key.getDirection() == +1 ) {
            // key with sharps
            switch(accidental) {
                case +1: return "#";
                case -1: return "@";
            }
        } else {
            // key with flats
            switch(accidental) {
                case +1: return "@";
                case -1: return "b";
            }
        }

        throw new IllegalArgumentException("Unexpected accidental " + accidental + " in key " + key);
    }

    /**
     * Returns the absolute note number of the note with the given
     * canonical name. Note that accidentals must be spelled with flats
     * in the canonical name, i.e. "Db" and not "C#".
     *
     * @param noteName
     * @return
     */
    public static int getNoteId(String noteName) {
        return NOTE_NAMES.indexOf(noteName);
    }

    @Override
    public String toString() {
        return getNoteName(getRelativeNote()) + getOctave() + ":" + duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return midiNumber == note.midiNumber &&
                duration == note.duration;
    }

    @Override
    public int hashCode() {
        return Objects.hash(midiNumber, duration);
    }
}
