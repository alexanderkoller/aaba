package de.saar.coli.arranger;

import au.com.codeka.carrot.CarrotEngine;
import au.com.codeka.carrot.CarrotException;
import au.com.codeka.carrot.Configuration;
import au.com.codeka.carrot.bindings.MapBindings;
import au.com.codeka.carrot.resource.FileResourceLocator;
import au.com.codeka.carrot.resource.MemoryResourceLocator;
import au.com.codeka.carrot.resource.ResourceLocator;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbcWriter {
    public static void main(String[] args) throws IOException {
        Score s = new Score("Test Song", "AK", "C", 4);

        s.addNote(0, Note.create("E4", 2));
        s.addNote(1, Note.create("C4", 2));
        s.addNote(2, Note.create("G3", 2));
        s.addNote(3, Note.create("C3", 2));

        s.addNote(1, Note.create("D4", 2));
        s.addNote(1, Note.create("E4", 2));
        s.addNote(1, Note.create("F4", 2));
        s.addNote(1, Note.create("C4", 2));
        s.addNote(1, Note.create("D4", 2));

        s.addNote(0, Note.create("E4", 2));
        s.addNote(0, Note.create("E4", 2));
        s.addNote(0, Note.create("E4", 2));
        s.addNote(0, Note.create("E4", 2));
        s.addNote(0, Note.create("E4", 2));
        s.addNote(2, Note.create("G3", 2));
        s.addNote(3, Note.create("C3", 2));
        s.addNote(2, Note.create("G3", 2));
        s.addNote(3, Note.create("C3", 2));
        s.addNote(2, Note.create("G3", 2));
        s.addNote(3, Note.create("C3", 2));
        s.addNote(2, Note.create("G3", 2));
        s.addNote(3, Note.create("C3", 2));
        s.addNote(2, Note.create("G3", 2));
        s.addNote(3, Note.create("C3", 2));


        PrintWriter w = new PrintWriter(new OutputStreamWriter(System.out));
        AbcWriter abc = new AbcWriter();
        abc.write(s, w);
        w.println();
        w.close();
    }

    public void write(Score score, Writer writer) throws IllegalArgumentException, IOException {
        CarrotEngine engine = new CarrotEngine(new Configuration.Builder()
                .setResourceLocator(makeResourceLocator())
                .build());

        int eightsPerMeasure = score.getQuartersPerMeasure() * 2;
        Map<String, Object> bindings = new HashMap<>();
        bindings.put("title", score.getTitle());
        bindings.put("composer", score.getComposer());
        bindings.put("key", score.getKey());
        bindings.put("timesig", Integer.toString(score.getQuartersPerMeasure()) + "/4");
        bindings.put("lyrics", String.join(" ", score.getLyrics()));

        Key key = Key.lookup(score.getKey());

        for (int i = 0; i < 4; i++) {
            List<Note> part = score.getPart(i);
            StringBuilder buf = new StringBuilder();
            int timeSinceStart = 0; // in 1/8 notes
            int timeInMeasure = 0;

            for (Note note : part) {
                Chord currentChord = score.getChordAtTime(timeSinceStart);

                buf.append(" ");
                buf.append(abcNote(note, key, currentChord));

                timeSinceStart += note.getDuration();
                timeInMeasure += note.getDuration();
                if (timeInMeasure >= score.getQuartersPerMeasure() * 2) {
                    buf.append(" |");
                    timeInMeasure = 0;
                }
            }

            buf.append("]");

            bindings.put(Score.PART_NAMES[i], buf.toString());
        }

        try {
            String abc = engine.process("template.abc", new MapBindings(bindings));
            writer.write(abc);
        } catch (CarrotException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private String abcNote(Note note, Key key, Chord currentChord) {
        String n = null;
        StringBuilder buf = new StringBuilder();
        Key chordKey = currentChord == null ? null : currentChord.getKey();

        // get note name with accidentals, in standard spelling
        if (key.notesInKey.contains(note.getRelativeNote())) {
            // note exists in key, use spelling from key;
            // in ABC notation, this means that note is spelled unmodified
            int baseNote = key.getBaseNote(note.getRelativeNote());
            n = Note.getNoteName(baseNote);
        } else if (chordKey != null && chordKey.notesInKey.contains(note.getRelativeNote())) {
            // note exists in key of current chord, use chord key's accidental
            // (only if chord is available in score)
            n = note.getNoteName(chordKey, true);




            System.err.printf("%s in chord key %s -> spell as %s\n", Note.getNoteName(note.getRelativeNote()), chordKey, n);
        } else {
            // note exists in neither, use fallback tactics for key
            n = note.getNoteName(key);
            System.err.printf("%s fallback spell as %s (chord key was %s)\n", Note.getNoteName(note.getRelativeNote()), n, chordKey);
        }

        // convert standard spelling of accidentals to ABC notation
        char possibleAccidental = n.charAt(n.length()-1);
        switch(possibleAccidental) {
            case '#':
                n = "^" + n.substring(0, n.length() - 1);
                break;
            case 'b':
                n = "_" + n.substring(0, n.length() - 1);
                break;
            case '@':
                n = "=" + n.substring(0, n.length() - 1);
                break;
        }

        assert n.length() <= 2; // no _Gb or such

        if (note.getOctave() >= 5) {
            buf.append(n.toLowerCase());
            for (int i = 5; i < note.getOctave(); i++) {
                buf.append("'");
            }
        } else {
            buf.append(n.toUpperCase());
            for (int i = 4; i > note.getOctave(); i--) {
                buf.append(",");
            }
        }

        buf.append(Integer.toString(note.getDuration())); // ASSPT:  duration in 1/8 notes
        return buf.toString();
    }

    private ResourceLocator.Builder makeResourceLocator() {
        MemoryResourceLocator.Builder ret = new MemoryResourceLocator.Builder();
        ret.add("template.abc", slurp("/template.abc"));
        return ret;
    }

    private static String slurp(String resourceName) {
        Reader r = new InputStreamReader(AbcWriter.class.getResourceAsStream(resourceName));
        return slurp(r);
    }

    /**
     * Reads the entire Reader into a string and returns it.
     *
     * @param reader
     * @return
     */
    private static String slurp(Reader reader) {
        try {
            char[] arr = new char[8 * 1024];
            StringBuilder buffer = new StringBuilder();
            int numCharsRead;
            while ((numCharsRead = reader.read(arr, 0, arr.length)) != -1) {
                buffer.append(arr, 0, numCharsRead);
            }
            reader.close();

            return buffer.toString();
        } catch (IOException e) {
            return null;
        }
    }
}
