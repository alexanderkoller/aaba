package de.saar.coli.arranger.rules;

import de.saar.coli.arranger.Chord;
import de.saar.coli.arranger.Config;
import de.saar.coli.arranger.Note;

import java.util.HashSet;
import java.util.Set;

/**
 * Penalizes voicings in which two parts sing the exact same note.
 * The penalty is determined by the "unisonNotes" config parameter.
 *
 */
public class VoUnisonNotes implements VoicingRule {
    @Override
    public int score(Note[] voicing, Chord chord, Config config) {
        if (getDifferentAbsoluteNotes(voicing).size() < 4) {
            return config.getScores().getUnisonNotes();
        } else {
            return 0;
        }
    }

    public static Set<Integer> getDifferentAbsoluteNotes(Note[] voicing) {
        Set<Integer> differentAbsoluteNotes = new HashSet<>();

        for (Note part : voicing) {
            differentAbsoluteNotes.add(part.getAbsoluteNote());
        }

        return differentAbsoluteNotes;
    }
}
