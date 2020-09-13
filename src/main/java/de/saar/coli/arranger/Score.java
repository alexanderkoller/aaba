package de.saar.coli.arranger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * A musical score, or piece of sheet music. Scores
 * consist of notes in the four parts of barbershop music,
 * in the order defined by the constants in {@link VoicePart},
 * i.e. {@link VoicePart#TENOR} = 0. Scores can also contain
 * lyrics and chords.
 */
public class Score {
    private List<Note>[] parts = new List[4]; // 0 = Tn, 1 = Ld, 2 = Br, 3 = Bs
    private String key;
    private String title;
    private String composer;
    private int quartersPerMeasure;
    private String tempo = null;
    private List<Pair<Integer,Chord>> chords = new ArrayList<>();
    private List<String> lyrics = new ArrayList<>();

    /**
     * Creates a score with an empty title and composer,
     * a key of C major, and a 4/4 time signature.
     */
    public Score() {
        this("", "", "C", 4);
    }

    /**
     * Creates a score with the given title, composer, key, and time
     * signature. Time signatures that are not in quarters are currently
     * not supported.
     *
     * @param title
     * @param composer
     * @param key
     * @param quartersPerMeasure
     */
    public Score(String title, String composer, String key, int quartersPerMeasure) {
        this.title = title;
        this.composer = composer;
        this.key = key;
        this.quartersPerMeasure = quartersPerMeasure;

        for( int i = 0; i < 4; i++ ) {
            parts[i] = new ArrayList<>();
        }
    }

    /**
     * Adds the given note to the end of the given voice part.
     *
     * @param part
     * @param note
     */
    public void addNote(int part, Note note) {
        parts[part].add(note);
    }

    /**
     * Returns the notes in the given voice part.
     *
     * @param part
     * @return
     */
    public List<Note> getPart(int part) {
        return parts[part];
    }

    /**
     * Returns the key of this score.
     *
     * @return
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the title of this score.
     *
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the composer of this score.
     *
     * @return
     */
    public String getComposer() {
        return composer;
    }

    /**
     * Returns the time signature of this score, in quarters per measure.
     *
     * @return
     */
    public int getQuartersPerMeasure() {
        return quartersPerMeasure;
    }

    /**
     * Changes the key of this score.
     *
     * @param key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Changes the title of this score.
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Changes the composer of this score.
     *
     * @param composer
     */
    public void setComposer(String composer) {
        this.composer = composer;
    }

    /**
     * Changes the time signature of thi score, in quarters per measure.
     *
     * @param quartersPerMeasure
     */
    public void setQuartersPerMeasure(int quartersPerMeasure) {
        this.quartersPerMeasure = quartersPerMeasure;
    }

    /**
     * Returns the tempo of the song, as per the "Q:" field in the ABC notation.
     *
     * @return
     */
    public String getTempo() {
        return tempo;
    }

    public void setTempo(String tempo) {
        this.tempo = tempo;
    }

    /**
     * Adds a chord at the end of this score.
     * The "startTime" is the time in 1/8 notes since
     * the beginning of the score at which this chord
     * is to be played. This makes it possible to hold
     * the same chord over several notes in the music.
     *
     * @param startTime
     * @param chord
     */
    public void addChord(int startTime, Chord chord) {
        chords.add(new Pair<>(startTime, chord));
    }

    /**
     * Adds a word to the end of the lyrics.
     *
     * @param word
     */
    public void addWord(String word) {
        lyrics.add(word);
    }

    /**
     * Returns the lyrics of the song, as a list of words.
     *
     * @return
     */
    public List<String> getLyrics() {
        return lyrics;
    }

    /**
     * Returns the chord that is played at the given time.
     * Time is counted in 1/8 notes since the beginning
     * of the score.
     *
     * @param time
     * @return
     */
    public Chord getChordAtTime(int time) {
        for( int i = 0; i < chords.size(); i++ ) {
            if( chords.get(i).getLeft() <= time
                && (i+1 == chords.size() || chords.get(i+1).getLeft() > time) ) {
                return chords.get(i).getRight();
            }
        }

        return null;
    }

    /**
     * Returns a copy of this score with all the notes removed.
     *
     * @return
     */
    public Score cloneWithoutNotes() {
        Score ret = new Score();
        ret.setTitle(getTitle());
        ret.setComposer(getComposer());
        ret.setKey(getKey());
        ret.setQuartersPerMeasure(getQuartersPerMeasure());
        ret.lyrics = new ArrayList<>(lyrics);

        if( tempo != null ) {
            ret.setTempo(tempo);
        }

        for (Pair<Integer, Chord> chord : chords) {
            ret.addChord(chord.getLeft(), chord.getRight());
        }

        return ret;
    }

    /**
     * Iterates over all the notes in the given part.
     * With each note, the corresponding chord is passed
     * to the given consumer "fn".
     *
     * @param part
     * @param fn
     */
    public void foreachNoteAndChord(int part, BiConsumer<Note,Chord> fn) {
        int time = 0;

        for (Note note : getPart(part)) {
            Chord chord = getChordAtTime(time);
            fn.accept(note, chord);
            time += note.getDuration();
        }
    }

    /**
     * Counts the number of notes in the given part.
     *
     * @param part
     * @return
     */
    public int countNotes(int part) {
        return getPart(part).size();
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        for( int i = 0; i < 4; i++ ) {
            buf.append(VoicePart.PART_NAMES[i]);
            buf.append(":");
            for( Note note : parts[i]) {
                buf.append(" ");
                buf.append(note.toString());
            }
            buf.append("\n");
        }

        return buf.toString();
    }
}
