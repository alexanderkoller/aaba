package de.saar.coli.arranger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Key {
    C("C", "Am", 0),
    G("G", "Em", +1, "F"),
    D("D", "Bm", +1, "F", "C"),
    A("A", "F#m", +1, "F", "C", "G"),
    E("E", "C#m", +1, "F", "C", "G", "D"),
    B("B", "G#m", +1, "F", "C", "G", "D", "A"),
    Fsharp("F#", "D#m", +1, "F", "C", "G", "D", "A", "E"),
    F("F", "Dm", -1, "B"),
    Bb("Bb", "Gm", -1, "B", "E"),
    Eb("Eb", "Cm", -1, "B", "E", "A"),
    Ab("Ab", "Fm", -1, "B", "E", "A", "D"),
    Db("Db", "Bbm", -1, "B", "E", "A", "D", "G"),
    Gb("Gb", "Ebm", -1, "B", "E", "A", "D", "G", "C")
    ;

    public final Map<String,Integer> accidentals;
    public final String majorName;
    public final String minorName;

    private Key(String majorName, String minorName, int direction, String... notesWithAccidentals) {
        accidentals = new HashMap<>();
        this.majorName = majorName;
        this.minorName = minorName;

        for( String note : notesWithAccidentals ) {
            accidentals.put(note, direction);
        }

        Lookup.put(majorName, this);
        Lookup.put(minorName, this);
    }

    public int getAccidentalForNote(String note) {
        Integer acc = accidentals.get(note);

        if( acc == null ) {
            return 0;
        } else {
            return acc;
        }
    }

    public static class Lookup {
        private static final Map<String,Key> lookup = new HashMap<>();

        private static void put(String name, Key key) {
            lookup.put(name, key);
        }

        public static Key lookup(String name) {
            return lookup.get(name);
        }
    }
}
