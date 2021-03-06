package de.saar.coli.arranger;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.Reader;
import java.util.*;

/**
 * A configuration file for the automatic arranger.
 *
 */
public class Config {
    /**
     * Dialects of the ABC format which need to be distinguished by the
     * {@link de.saar.coli.arranger.abc.AbcWriter}.
     */
    public static enum ABC_DIALECT {
        /**
         * The default ABC 2.1 dialect. It is rendered correctly e.g. by EasyABC.
         */
        STANDARD,

        /**
         * The ABC dialect which is rendered correctly by ABC2SVG and $ABC_UI.
         * These renderers ignore the builtin transposition of the Tn/Ld staff, so
         * we need to transpose the notes on that staff ourselves. They also do not
         * understand the "snm" flag, so we do not produce it.
         */
        ABC2SVG
    }

    private String arranger;
    private Scores scores;
    private List<VoicePart> voiceParts;
    private List<Clef> clefs;
    private ABC_DIALECT abcDialect = ABC_DIALECT.STANDARD;

    private static final Set<String> ALL_VOICEPARTS = new HashSet<>(List.of("Tenor", "Lead", "Baritone", "Bass"));

    public static Config read(Reader configReader) {
        Yaml yaml = new Yaml(new Constructor(Config.class));
        Config ret = (Config) yaml.load(configReader);

        ret.voiceParts = sortVoiceParts(ret.voiceParts);

        if( ret.clefs.size() != 2 ) {
            throw new RuntimeException("Error reading configuration file: need exactly two clefs.");
        }

        return ret;
    }

    private static List<VoicePart> sortVoiceParts(List<VoicePart> voiceParts) {
        Map<String,VoicePart> partsByName = new HashMap<>();
        for( VoicePart p : voiceParts ) {
            partsByName.put(p.getName(), p);
        }

        if( ! partsByName.keySet().equals(ALL_VOICEPARTS)) {
            throw new RuntimeException("Error reading configuration file: Must have exactly one entry under voiceParts for each of Tenor, Lead, Baritone, and Bass, but found " + partsByName.keySet() + ".");
        }

        List<VoicePart> sortedParts = new ArrayList<>();
        sortedParts.add(partsByName.get("Tenor"));
        sortedParts.add(partsByName.get("Lead"));
        sortedParts.add(partsByName.get("Baritone"));
        sortedParts.add(partsByName.get("Bass"));

        return sortedParts;
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

    public ABC_DIALECT getAbcDialect() {
        return abcDialect;
    }

    public void setAbcDialect(ABC_DIALECT abcDialect) {
        this.abcDialect = abcDialect;
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
