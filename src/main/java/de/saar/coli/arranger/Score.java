package de.saar.coli.arranger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Score {
    private List<Note>[] parts = new List[4]; // 0 = Tn, 1 = Ld, 2 = Br, 3 = Bs
    public static String[] PART_NAMES = new String[] { "Tn", "Ld", "Br", "Bs" };

    public Score() {
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
