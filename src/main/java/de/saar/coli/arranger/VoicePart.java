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
    public static final VoicePart[] VOICE_PARTS = new VoicePart[]{
            new VoicePart("Tenor", Note.create("G3", 0), Note.create("B4", 0)),
            new VoicePart("Lead", Note.create("C3", 0), Note.create("G4", 0)),
            new VoicePart("Baritone", Note.create("C3", 0), Note.create("G4", 0)),
            new VoicePart("Bass", Note.create("F2", 0), Note.create("C4", 0))
    };

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
