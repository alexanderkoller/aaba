package de.saar.coli.arranger.rules;

import de.saar.coli.arranger.Chord;
import de.saar.coli.arranger.Config;
import de.saar.coli.arranger.Note;

public interface VoicingRule {
    public int score(Note[] voicing, Chord chord, Config config);
}
