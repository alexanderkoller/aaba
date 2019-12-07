package de.saar.coli.arranger;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.Reader;
import java.util.List;

/**
 * A configuration file for the automatic arranger.
 *
 */
public class Config {
    private String arranger;
    private Scores scores;
    private List<VoicePart> voiceParts;
    private List<Clef> clefs;

    public static Config read(Reader configReader) {
        Yaml yaml = new Yaml(new Constructor(Config.class));
        Config ret = (Config) yaml.load(configReader);

        String errVoiceParts = checkVoiceParts(ret.voiceParts);
        if( errVoiceParts != null ) {
            throw new RuntimeException("Error reading configuration file: " + errVoiceParts);
        }

        if( ret.clefs.size() != 2 ) {
            throw new RuntimeException("Error reading configuration file: need exactly two clefs.");
        }

        return ret;
    }

    private static String checkVoiceParts(List<VoicePart> voiceParts) {
        if( voiceParts.size() != 4 ) {
            return "Arrangement needs exactly four voice parts.";
        }

        if( ! "Tenor".equals(voiceParts.get(0).getName())
            || ! "Lead".equals(voiceParts.get(1).getName())
            || ! "Baritone".equals(voiceParts.get(2).getName())
            || ! "Bass".equals(voiceParts.get(3).getName()) ) {
            return "Voice parts must be Tenor-Lead-Baritone-Bass, in this order.";
        }

        return null;
    }

    public Scores getScores() {
        return scores;
    }

    public void setScores(Scores scores) {
        this.scores = scores;
    }

    public String getArranger() {
        return arranger;
    }

    public void setArranger(String arranger) {
        this.arranger = arranger;
    }

    public List<VoicePart> getVoiceParts() {
        return voiceParts;
    }

    public void setVoiceParts(List<VoicePart> voiceParts) {
        this.voiceParts = voiceParts;
    }

    public List<Clef> getClefs() {
        return clefs;
    }

    public void setClefs(List<Clef> clefs) {
        this.clefs = clefs;
    }

    public static class Scores {
        private int harmonyLeaps;
        private int parallelOctaves;
        private int tenorCrossing;
        private int unisonNotes;
        private int wideSpread;

        public int getHarmonyLeaps() {
            return harmonyLeaps;
        }

        public void setHarmonyLeaps(int harmonyLeaps) {
            this.harmonyLeaps = harmonyLeaps;
        }

        public int getParallelOctaves() {
            return parallelOctaves;
        }

        public void setParallelOctaves(int parallelOctaves) {
            this.parallelOctaves = parallelOctaves;
        }

        public int getTenorCrossing() {
            return tenorCrossing;
        }

        public void setTenorCrossing(int tenorCrossing) {
            this.tenorCrossing = tenorCrossing;
        }

        public int getUnisonNotes() {
            return unisonNotes;
        }

        public void setUnisonNotes(int unisonNotes) {
            this.unisonNotes = unisonNotes;
        }

        public int getWideSpread() {
            return wideSpread;
        }

        public void setWideSpread(int wideSpread) {
            this.wideSpread = wideSpread;
        }
    }
}
