package de.saar.coli.arranger;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.Reader;

public class Config {
    private String arranger;
    private Scores scores;

    public static Config read(Reader configReader) {
        Yaml yaml = new Yaml(new Constructor(Config.class));
        return (Config) yaml.load(configReader);
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
