package de.saar.coli.arranger.rules;

import de.saar.coli.arranger.Config;
import de.saar.coli.arranger.Note;

public interface VoiceLeadingRule {
    public int score(Note[] from, Note[] to, Config config);
}
