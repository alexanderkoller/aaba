package de.saar.coli.arranger;

import de.saar.coli.arranger.abc.AbcWriter;

import java.io.IOException;
import java.io.StringWriter;

public class Arrangement {
    private Score arrangement;
    private Score original;
    private int score;
    private long runtimeNs;

    public Arrangement(Score arrangement, Score original, int score, long runtimeNs) {
        this.arrangement = arrangement;
        this.original = original;
        this.score = score;
        this.runtimeNs = runtimeNs;
    }

    public Score getArrangement() {
        return arrangement;
    }

    public Score getOriginal() {
        return original;
    }

    public int getScore() {
        return score;
    }

    public long getRuntimeNs() {
        return runtimeNs;
    }
}
