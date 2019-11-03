package de.saar.coli.arranger;

import java.util.*;

/**
 * A chord rooted in a specific note. Each chord is defined by its {@link ChordType}, such as
 * "seventh" or "minor sixth", and its root note. The class offers methods for accessing
 * the notes in the chord.
 */
public class Chord {
    private int root;
    private ChordType type;
    private static final ChordType[] SUFFIX_CHECKING_ORDER = new ChordType[] { ChordType.DIMINISHED, ChordType.HALF_DIMINISHED, ChordType.MINOR_SEVENTH, ChordType.MINOR_SIXTH, ChordType.MAJOR_SEVENTH, ChordType.ADDNINE, ChordType.SEVEN_NINE, ChordType.SIXTH, ChordType.MINOR, ChordType.SEVENTH, ChordType.MAJOR };

    /**
     * A chord type, such as "major" or "minor sixth". The implemented chord times are listed below.
     */
    public static enum ChordType {
        /**
         * major triad (empty name suffix, so "C" = C-E-G is a major triad)
         */
        MAJOR("", "", List.of(0,7), 0, 4, 7),

        /**
         * major sixth (name suffix "6", so "C6" = C-E-G-A is a major sixth chord)
         */
        SIXTH("6", "", List.of(0,7), 0, 4, 7, 9),

        /**
         * dominant seventh (name suffix "7", so "C7" = C-E-G-Bb)
         */
        SEVENTH("7", "", List.of(0,7), 0, 4, 7, 10),

        /**
         * add-nine (name suffix "add9", so "Cadd9" = C-E-G-D)
         */
        ADDNINE("add9", "", List.of(0,7), 0, 4, 7, 2),

        /**
         * dominant ninth without root note (name suffix "9", so "C9" = E-G-Bb-D)
         */
        SEVEN_NINE("9", "", List.of(0,7), 4, 7, 10, 2),

        /**
         * major seventh (name suffix "mj7", so "Cmj7" = C-E-G-B)
         */
        MAJOR_SEVENTH("mj7", "", List.of(0,7), 0, 4, 7, 11),

        /**
         * minor triad (name suffix "m", so "Am" = A-C-E)
         */
        MINOR("m", "m", List.of(0, 3, 7), 0, 3, 7),

        /**
         * minor sixth (name suffix "m6", so "Am6" = A-C-E-F#)
         */
        MINOR_SIXTH("m6", "m", List.of(0,3,7), 0, 3, 7, 9),

        /**
         * minor seventh (name suffix "m7", so "Am7" = A-C-E-G)
         */
        MINOR_SEVENTH("m7", "m", List.of(0,7), 0, 3, 7, 10),

        /**
         * half-diminished (name suffix "x7", so "Ax7" = A-C-Eb-G)
         */
        HALF_DIMINISHED("x7", "m", List.of(0), 0, 3, 6, 10),

        /**
         * full-diminished (name suffix "07", so "A07" = A-C-Eb-Gb)
         */
        DIMINISHED("07", "", List.of(0, 3, 6, 9), 0, 3, 6, 9)
        ;


        private final List<Integer> chordNotes;
        private final String name;
        private final Set<Integer> allowedBassNotes;
        private final String mode;

        private ChordType(String name, String mode, List<Integer> allowedBassNotes, Integer... notes) {
            this.name = name;
            this.chordNotes = Arrays.asList(notes);
            this.mode = mode;
            this.allowedBassNotes = new HashSet<>(allowedBassNotes);
        }

        /**
         * The notes in the chord, measured in semitones above the root of the chord.
         */
        public List<Integer> getChordNotes() {
            return chordNotes;
        }

        /**
         * The name suffix of the chord, such as "m" for minor.
         *
         * @return
         */
        public String getName() {
            return name;
        }

        /**
         * The notes (in semitones above root note) that are allowed
         * in the bass part of barbershop voicings. For major chords, these
         * are roots and fifths; for minor chords, roots, thirds, and fifths.
         * Currently only the root note is allowed in the bass of a half-diminished
         * chord (is this correct?), and all notes are allowed in the bass of
         * a fully diminished chord.
         *
         * @return
         */
        public Set<Integer> getAllowedBassNotes() {
            return allowedBassNotes;
        }

        /**
         * The mode of the chord: "" for major, "m" for minor.
         * This method currently counts half-diminished chords as minor,
         * fully diminished as "major".
         *
         * @return
         */
        public String getMode() {
            return mode;
        }
    }

    /**
     * Looks up a chord based on its root note and its chord type.
     *
     * @param root
     * @param type
     * @return
     */
    // TODO reuse Chord objects
    public static Chord lookup(String root, ChordType type) {
        Chord chord = new Chord();
        chord.root = Note.getNoteId(root);
        chord.type = type;
        return chord;
    }

    /**
     * Looks up a chord based on its name, such as "Cmj7".
     *
     * @param chordName
     * @return
     */
    public static Chord lookup(String chordName) {
        ChordType type = chordTypeBySuffix(chordName);
        String root = chordName.substring(0, chordName.length()-type.name.length());
        return lookup(root, type);
    }

    /**
     * Returns the notes of the chord, as relative notes
     * in the sense of {@link Note}.
     *
     * @return
     */
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

    /**
     * Checks if the given note is allowed in the bass part
     * of this chord.
     *
     * @param note
     * @return
     */
    public boolean isAllowedBassNote(Note note) {
        int relativeNote = (note.getAbsoluteNote() - root)%12;
        return type.allowedBassNotes.contains(relativeNote);
    }

    /**
     * Returns the key to which this chord belongs. This is a
     * combination of the chord's root and its mode, as per
     * {@link ChordType#getMode()}. Thus, the Cmaj7 chord is
     * mapped to the key of C major, and the Am6 chord is
     * mapped to the key of A minor.
     * These keys can be used to spell the accidentals
     * in the chord correctly.
     *
     * @return
     */
    public Key getKey() {
        return Key.lookup(Note.getNoteName(root) + type.mode);
    }

    /**
     * Returns the root note of the chord, as a relative
     * note in the sense of {@link Note}.
     *
     * @return
     */
    public int getRoot() {
        return root;
    }

    /**
     * Returns the type of this chord.
     *
     * @return
     */
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
