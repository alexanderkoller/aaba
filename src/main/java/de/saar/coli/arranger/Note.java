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
