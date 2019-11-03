package de.saar.coli.arranger.rules;

import de.saar.coli.arranger.Chord;
import de.saar.coli.arranger.Config;
import de.saar.coli.arranger.Note;

import java.util.Collections;
import java.util.Set;

/**
 * Penalizes voicings which are spread out very widely, ie the highest
 * and lowest note are more than an octave plus a fifth apart.
 * The penalty is determined by the config parameter "wideSpread".
 *
 */
public class VoWideSpread implements VoicingRule {
    @Override
    public int score(Note[] voicing, Chord chord, Config config) {
        Set<Integer> differentAbsoluteNotes = VoUnisonNotes.getDifferentAbsoluteNotes(voicing);
        int highest = Collections.max(differentAbsoluteNotes);
        int lowest = Collections.min(differentAbsoluteNotes);

        if (highest - lowest > 19) { // octave + fifth
            return config.getScores().getWideSpread();
        } else {
            return 0;
        }
    }
}
