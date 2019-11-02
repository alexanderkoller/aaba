package de.saar.coli.arranger;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Note {
    private int midiNumber;
    private int duration;

    private static List<String> NOTE_NAMES = Arrays.asList(new String[] { "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"});
    private static Pattern NOTE_PATTERN = Pattern.compile("([A-Za-z]+)([0-9]+)");

    public static Note create(int absoluteNote, int duration) {
        Note ret = new Note();
        ret.midiNumber = absoluteNote;
        ret.duration = duration;
        return ret;
    }

    public static Note create(int relativeNote, int octave, int duration) {
        return create(12*(octave+1) + relativeNote, duration);
    }

    public static Note create(String relativeNote, int octave, int duration) {
        return create(getNoteId(relativeNote), octave, duration);
    }

    public static Note create(String noteWithOctave, int duration) {
        Matcher m = NOTE_PATTERN.matcher(noteWithOctave);

        if( m.matches() ) {
            return create(m.group(1), Integer.parseInt(m.group(2)), duration);
        } else {
            return null;
        }
    }

    public int getAbsoluteNote() {
        return midiNumber;
    }

    public int getAbsoluteDistance(Note other) {
        return Math.abs(midiNumber - other.midiNumber);
    }

    public Note sharp(Note base) {
        return Note.create(base.midiNumber+1, base.duration);
    }

    public Note flat(Note base) {
        return Note.create(base.midiNumber-1, base.duration);
    }

    public Note add(int offset) {
        return Note.create(midiNumber+offset, duration);
    }

    public int getRelativeNote() {
        return midiNumber % 12;
    }

    public int getOctave() {
        return midiNumber/12 - 1;
    }

    public int getDuration() {
        return duration;
    }

    public static String getNoteName(int relativeNote) {
        return NOTE_NAMES.get(relativeNote);
    }

    public String getNoteName(Key key) {
        return getNoteName(key, false);
    }

    /**
     * Returns the name of this note in the given key. Accidentals are spelled
     * with respect to the key, and are represented as "F#", "Bb", etc.
     *
     * @param key
     * @return
     */
    public String getNoteName(Key key, boolean forceNatural) {
        assert key != null;

        if( key.notesInKey.contains(getRelativeNote()) ) {
            // note exists in key

            Integer baseNote = key.accidentalNoteToBaseNote.get(getRelativeNote());
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
            int noteWithAccidental = (getRelativeNote() + key.direction) % 12;
            if( key.accidentalNoteToBaseNote.containsKey(noteWithAccidental)) {
                // spell as natural
                return NOTE_NAMES.get(getRelativeNote()) + "@";
            } else {
                // note is foreign to key, spell with accidental of usual type in key
                int baseNote = (getRelativeNote() - key.direction)%12;
                return NOTE_NAMES.get(baseNote) + accidentalString(key.direction, key);
            }
        }
    }

    private static String accidentalString(int accidental, Key key) {
        if( key.direction == +1 ) {
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
