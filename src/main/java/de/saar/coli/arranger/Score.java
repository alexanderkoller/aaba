package de.saar.coli.arranger;

import abcj.ABCJ;
import abcj.model.Library;
import abcj.model.Tune;
import abcj.model.TuneBook;
import abcj.model.TuneList;
import abcj.ui.MainGUI;
import abcj.ui.MainPane;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class Score {
    private List<Note>[] parts = new List[4]; // 0 = Tn, 1 = Ld, 2 = Br, 3 = Bs
    public static String[] PART_NAMES = new String[] { "Tn", "Ld", "Br", "Bs" };
    private String key;
    private String title;
    private String composer;
    private int quartersPerMeasure;
    private List<Pair<Integer,Chord>> chords = new ArrayList<>();
    private List<String> lyrics = new ArrayList<>();

    public Score() {
        this("", "", "", 4);
    }

    public Score(String title, String composer, String key, int quartersPerMeasure) {
        this.title = title;
        this.composer = composer;
        this.key = key;
        this.quartersPerMeasure = quartersPerMeasure;

        for( int i = 0; i < 4; i++ ) {
            parts[i] = new ArrayList<>();
        }
    }

    public void addNote(int part, Note note) {
        parts[part].add(note);
    }

    public List<Note> getPart(int part) {
        return parts[part];
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public String getComposer() {
        return composer;
    }

    public int getQuartersPerMeasure() {
        return quartersPerMeasure;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public void setQuartersPerMeasure(int quartersPerMeasure) {
        this.quartersPerMeasure = quartersPerMeasure;
    }

    public void addChord(int startTime, Chord chord) {
        chords.add(new Pair<>(startTime, chord));
    }

    public void addWord(String word) {
        lyrics.add(word);
    }

    public List<String> getLyrics() {
        return lyrics;
    }

    public void setLyrics(List<String> words) {
        lyrics = words;
    }

    public Chord getChordAtTime(int time) {
        for( int i = 0; i < chords.size(); i++ ) {
            if( chords.get(i).getLeft() <= time
                && (i+1 == chords.size() || chords.get(i+1).getLeft() > time) ) {
                return chords.get(i).getRight();
            }
        }

        return null;
    }

    public void foreachNoteAndChord(int part, BiConsumer<Note,Chord> fn) {
        int time = 0;

        for (Note note : getPart(part)) {
            Chord chord = getChordAtTime(time);
            fn.accept(note, chord);
            time += note.getDuration();
        }
    }

    public int countNotes(int part) {
        return getPart(part).size();
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        for( int i = 0; i < 4; i++ ) {
            buf.append(PART_NAMES[i]);
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
