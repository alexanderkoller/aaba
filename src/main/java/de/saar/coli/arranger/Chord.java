package de.saar.coli.arranger;

import java.util.Objects;

public class Chord {
    private String str;

    public static Chord lookup(String str) {
        Chord ret = new Chord();
        ret.str = str;
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chord chord = (Chord) o;
        return Objects.equals(str, chord.str);
    }

    @Override
    public String toString() {
        return str;
    }
}
