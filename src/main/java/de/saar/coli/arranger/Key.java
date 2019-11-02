package de.saar.coli.arranger;

import java.util.Map;

public enum Key {
    C(Map.of()),
    G(Map.of("F",+1)),
    D(Map.of("F", +1, "C", +1)),
    F(Map.of("B",-1)),
    Bb(Map.of("B", -1, "E", -1))
    ;

    public final Map<String,Integer> accidentals;

    private Key(Map<String, Integer> accidentals) {
        this.accidentals = accidentals;
    }

    public int getAccidentalForNote(String note) {
        Integer acc = accidentals.get(note);

        if( acc == null ) {
            return 0;
        } else {
            return acc;
        }
    }
}
