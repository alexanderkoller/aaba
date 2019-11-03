package de.saar.coli.arranger.rules;

import de.saar.coli.arranger.Chord;
import de.saar.coli.arranger.Config;
import de.saar.coli.arranger.Note;

import java.util.HashSet;
import java.util.Set;

/**
 * Disallows voicings that do not use all the notes in the given chord.
 *
 */
public class VoUseAllChordNotes implements VoicingRule {
    @Override
    public int score(Note[] voicing, Chord chord, Config config) {
        Set<Integer> differentRelativeNotes = new HashSet<>();

        for (Note part : voicing) {
            differentRelativeNotes.add(part.getRelativeNote());
        }

        if (differentRelativeNotes.size() < chord.getNotes().size()) {
            return Integer.MIN_VALUE;
        }

        return 0;
    }
}
