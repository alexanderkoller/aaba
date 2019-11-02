package de.saar.coli.arranger;

import java.util.*;

public class Chord {
    private int root;
    private ChordType type;
    private static final ChordType[] SUFFIX_CHECKING_ORDER = new ChordType[] { ChordType.DIMINISHED, ChordType.HALF_DIMINISHED, ChordType.MINOR_SEVENTH, ChordType.MINOR_SIXTH, ChordType.MAJOR_SEVENTH, ChordType.ADDNINE, ChordType.SEVEN_NINE, ChordType.SIXTH, ChordType.MINOR, ChordType.SEVENTH, ChordType.MAJOR };

    public static enum ChordType {
        MAJOR("", List.of(0,7), 0, 4, 7),
        SIXTH("6", List.of(0,7), 0, 4, 7, 9),
        SEVENTH("7", List.of(0,7), 0, 4, 7, 10),
        ADDNINE("add9", List.of(0,7), 0, 4, 7, 2),
        SEVEN_NINE("9", List.of(0,7), 4, 7, 10, 2),
        MAJOR_SEVENTH("mj7", List.of(0,7), 0, 4, 7, 11),
        MINOR("m", List.of(0, 3, 7), 0, 3, 7),
        MINOR_SIXTH("m6", List.of(0,3,7), 0, 3, 7, 9),
        MINOR_SEVENTH("m7", List.of(0,7), 0, 3, 7, 10),
        HALF_DIMINISHED("x7", List.of(0), 0, 3, 6, 10),
        DIMINISHED("07", List.of(0, 3, 6, 9), 0, 3, 6, 9)
        ;

        public final List<Integer> chordNotes;
        public final String name;
        public final Set<Integer> allowedBassNotes;

        private ChordType(String name, List<Integer> allowedBassNotes, Integer... notes) {
            this.name = name;
            this.chordNotes = Arrays.asList(notes);
            this.allowedBassNotes = new HashSet<>(allowedBassNotes);
        }
    }

    // TODO reuse Chord objects
    public static Chord lookup(String root, ChordType type) {
        Chord chord = new Chord();
        chord.root = Note.getNoteId(root);
        chord.type = type;
        return chord;
    }

    public static Chord lookup(String chordName) {
        ChordType type = chordTypeBySuffix(chordName);
        String root = chordName.substring(0, chordName.length()-type.name.length());
        return lookup(root, type);
    }

    public Set<Integer> getNotes() {
        Set<Integer> ret = new HashSet<>();

        for( int chordNote : type.chordNotes ) {
            ret.add((root+chordNote) % 12);
        }

        return ret;
    }

    private static ChordType chordTypeBySuffix(String chordName) {
        for( ChordType type : SUFFIX_CHECKING_ORDER ) {
            if( chordName.endsWith(type.name)) {
                return type;
            }
        }

        return null;
    }

    public boolean isAllowedBassNote(Note note) {
        int relativeNote = (note.getAbsoluteNote() - root)%12;
        return type.allowedBassNotes.contains(relativeNote);
    }

    public int getRoot() {
        return root;
    }

    public ChordType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chord chord = (Chord) o;
        return Objects.equals(root, chord.root) &&
                type == chord.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(root, type);
    }

    @Override
    public String toString() {
        return Note.getNoteName(root) + type.name;
    }
}
