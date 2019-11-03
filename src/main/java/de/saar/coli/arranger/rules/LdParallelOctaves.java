package de.saar.coli.arranger.rules;

import de.saar.coli.arranger.Config;
import de.saar.coli.arranger.Note;

/**
 * Penalizes voice movements in parallel octaves. The penalty
 * is determined by the config parameter "parallelOctaves".
 *
 */
public class LdParallelOctaves implements VoiceLeadingRule {
    @Override
    public int score(Note[] from, Note[] to, Config config) {
        int score = 0;

        // penalize parallel octaves
        for (int part = 0; part < 4; part++) {
            for (int other = part + 1; other < 4; other++) {
                if (from[part].getAbsoluteDistance(from[other]) == 12) {
                    if (to[part].getAbsoluteDistance(to[other]) == 12) {
                        score += config.getScores().getParallelOctaves();
                    }
                }
            }
        }

        return score;
    }
}
