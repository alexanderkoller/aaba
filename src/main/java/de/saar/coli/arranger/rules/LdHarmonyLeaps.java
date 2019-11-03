package de.saar.coli.arranger.rules;

import de.saar.coli.arranger.Config;
import de.saar.coli.arranger.Note;

import static de.saar.coli.arranger.VoicePart.BARI;
import static de.saar.coli.arranger.VoicePart.TENOR;

/**
 * Penalizes leaps of more than a minor third in the Tn and Br parts.
 * The penalty is determined by the configuration parameter "harmonyLeaps".
 *
 */
public class LdHarmonyLeaps implements VoiceLeadingRule {
    @Override
    public int score(Note[] from, Note[] to, Config config) {
        int score = 0;

        // penalize wide jumps in Br and Tn
        for (int part = 0; part < 4; part++) {
            if (part == TENOR || part == BARI) {
                if (from[part].getAbsoluteDistance(to[part]) > 3) {
                    score += config.getScores().getHarmonyLeaps();
                }
            }
        }

        return score;
    }
}
