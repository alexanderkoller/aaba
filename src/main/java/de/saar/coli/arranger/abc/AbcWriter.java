package de.saar.coli.arranger.abc;

import au.com.codeka.carrot.CarrotEngine;
import au.com.codeka.carrot.CarrotException;
import au.com.codeka.carrot.Configuration;
import au.com.codeka.carrot.bindings.MapBindings;
import au.com.codeka.carrot.resource.MemoryResourceLocator;
import au.com.codeka.carrot.resource.ResourceLocator;
import de.saar.coli.arranger.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class for writing a score in ABC notation.
 *
 */
public class AbcWriter {
    private Config config;

    public AbcWriter(Config config) {
        this.config = config;
    }

    /**
     * Writes the score to the given writer in ABC notation.
     *
     * @param score
     * @param writer
     * @throws IllegalArgumentException - if something went wrong in filling out the ABC template
     * @throws IOException - if an I/O error occurred
     */
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
                // ABC2SVG does not respect the built-in transposition of the upper clef,
                // so we have to do it by hand.
                if( i <= 1 && config.getAbcDialect() == Config.ABC_DIALECT.ABC2SVG ) {
                    note = note.transpose(12);
                }
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

            bindings.put(VoicePart.PART_NAMES[i], buf.toString());
        }

        List<String> clefspec = new ArrayList<>();
        for( Clef clef : config.getClefs()) {
            clefspec.add(clef.getClefSpec());
        }
        bindings.put("clefspec", clefspec);

        // look up template filename for the given ABC dialect
        String abcTemplateResourceName;
        switch(config.getAbcDialect()) {
            case ABC2SVG: abcTemplateResourceName = "abc2svg.abc"; break;
            default: abcTemplateResourceName = "template.abc";
        }

        // render ABC using the given template
        try {
            String abc = engine.process(abcTemplateResourceName, new MapBindings(bindings));
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
        if (key.getNotesInKey().contains(note.getRelativeNote())) {
            // note exists in key, use spelling from key;
            // in ABC notation, this means that note is spelled unmodified
            int baseNote = key.getBaseNote(note.getRelativeNote());
            n = Note.getNoteName(baseNote);
        } else if (chordKey != null && chordKey.getNotesInKey().contains(note.getRelativeNote())) {
            // note exists in key of current chord, use chord key's accidental
            // (only if chord is available in score)
            n = note.getNoteName(chordKey, true);
        } else {
            // note exists in neither, use fallback tactics for key
            n = note.getNoteName(key);
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
        ret.add("abc2svg.abc", slurp("/abc2svg.abc"));
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
