package de.saar.coli.arranger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Score {
    private List<Note>[] parts = new List[4]; // 0 = Tn, 1 = Ld, 2 = Br, 3 = Bs
    public static String[] PART_NAMES = new String[] { "Tn", "Ld", "Br", "Bs" };
    private String key;
    private String title;
    private String composer;
    private int quartersPerMeasure;

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
