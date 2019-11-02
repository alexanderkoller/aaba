package de.saar.coli.arranger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class VoicePart {
    private String name;
    private Note lowLimit;
    private Note highLimit;

    public VoicePart(String name, Note lowLimit, Note highLimit) {
        this.name = name;
        this.lowLimit = lowLimit;
        this.highLimit = highLimit;
    }

    public String getName() {
        return name;
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
}
