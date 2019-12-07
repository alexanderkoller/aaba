package de.saar.coli.arranger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A voice part of a barbershop arrangement. Each voice part has a name and a range
 * of notes they can sing.
 *
 */
public class VoicePart {
    public static final int TENOR = 0;
    public static final int LEAD = 1;
    public static final int BARI = 2;
    public static final int BASS = 3;
    public static String[] PART_NAMES = new String[] { "Tn", "Ld", "Br", "Bs" };

    private String name;
    private Note lowLimit;
    private Note highLimit;

    public VoicePart(String name, Note lowLimit, Note highLimit) {
        this.name = name;
        this.lowLimit = lowLimit;
        this.highLimit = highLimit;
    }

    public VoicePart() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Note getLowLimit() {
        return lowLimit;
    }

    public Note getHighLimit() {
        return highLimit;
    }

    public List<Note> getNotesInRange(Set<Integer> relativeNotes, int duration) {
        List<Note> notesForPart = new ArrayList<>();

        for( int absoluteNote = getLowLimit().getAbsoluteNote(); absoluteNote <= getHighLimit().getAbsoluteNote(); absoluteNote++) {
            if( relativeNotes.contains(absoluteNote%12)) {
                notesForPart.add(Note.create(absoluteNote, duration));
            }
        }

        return notesForPart;
    }

    public void setBottom(String bottom) {
        lowLimit = Note.create(bottom, 0);
    }

    public void setTop(String top) {
        highLimit = Note.create(top, 0);
    }

    @Override
    public String toString() {
        return String.format("%s:%s-%s", name, lowLimit, highLimit);
    }
}
