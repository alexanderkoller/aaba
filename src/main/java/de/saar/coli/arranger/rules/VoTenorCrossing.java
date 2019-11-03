package de.saar.coli.arranger.rules;

import de.saar.coli.arranger.Chord;
import de.saar.coli.arranger.Config;
import de.saar.coli.arranger.Note;
import de.saar.coli.arranger.VoicePart;

/**
 * Penalizes voicings in which the tenor does not have the highest note.
 * The penalty is determined by the config parameter "tenorCrossing".
 */
public class VoTenorCrossing implements VoicingRule {
    @Override
    public int score(Note[] voicing, Chord chord, Config config) {
        int score = 0;

        // Tn likes to be highest note
        if (voicing[VoicePart.TENOR].getAbsoluteNote() < voicing[VoicePart.LEAD].getAbsoluteNote()
                || voicing[VoicePart.TENOR].getAbsoluteNote() < voicing[VoicePart.BARI].getAbsoluteNote()) {
            score += config.getScores().getTenorCrossing();
        }

        return score;
    }
}
