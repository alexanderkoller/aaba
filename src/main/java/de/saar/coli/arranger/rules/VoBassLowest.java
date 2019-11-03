package de.saar.coli.arranger.rules;

import de.saar.coli.arranger.Chord;
import de.saar.coli.arranger.Config;
import de.saar.coli.arranger.Note;
import de.saar.coli.arranger.VoicePart;

/**
 * Disallows voicings in which the bass is not the lowest note.
 */
public class VoBassLowest implements VoicingRule {
    @Override
    public int score(Note[] voicing, Chord chord, Config config) {
        int bassNote = voicing[VoicePart.BASS].getAbsoluteNote();

        if (bassNote > voicing[VoicePart.BARI].getAbsoluteNote()
                || bassNote > voicing[VoicePart.LEAD].getAbsoluteNote()
                || bassNote > voicing[VoicePart.TENOR].getAbsoluteNote()) {
            return Integer.MIN_VALUE;
        }

        return 0;
    }
}
