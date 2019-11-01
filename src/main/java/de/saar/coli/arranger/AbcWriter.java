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
        Score s = new Score();

        s.addNote(0, Note.create("E4", 2));
        s.addNote(1, Note.create("C4", 2) );
        s.addNote(2, Note.create("G3", 2));
        s.addNote(3, Note.create("C3", 2));

        s.addNote(1, Note.create("D4", 2) );
        s.addNote(1, Note.create("E4", 2) );
        s.addNote(1, Note.create("F4", 2) );
        s.addNote(1, Note.create("C4", 2) );
        s.addNote(1, Note.create("D4", 2) );

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
        abc.write(s, "Test Song", "AK", "C", 4, w);
        w.println();
        w.close();
    }

    public void write(Score score, String title, String composer, String key, int quartersPerMeasure, Writer writer) throws IllegalArgumentException, IOException {
        CarrotEngine engine = new CarrotEngine(new Configuration.Builder()
                .setResourceLocator(makeResourceLocator())
                .build());

        int eightsPerMeasure = quartersPerMeasure*2;
        Map<String,Object> bindings = new HashMap<>();
        bindings.put("title", title);
        bindings.put("composer", composer);
        bindings.put("key", key);
        bindings.put("timesig", Integer.toString(quartersPerMeasure) + "/4");

        for( int i = 0; i < 4; i++ ) {
            List<Note> part = score.getPart(i);
            StringBuilder buf = new StringBuilder();
            int eightsInMeasure = 0;

            for( Note note : part ) {
                buf.append(abcNote(note));
                buf.append(" ");
                eightsInMeasure += note.getDuration();

                if( eightsInMeasure >= 8 ) {
                    buf.append("| ");
                    eightsInMeasure = 0;
                }
            }

            buf.append("|]");

            bindings.put(Score.PART_NAMES[i], buf.toString());
        }

        try {
            String abc = engine.process("template.abc", new MapBindings(bindings));
            writer.write(abc);
        } catch (CarrotException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private String abcNote(Note note) {
        String n = Note.getNoteName(note.getRelativeNote());
        StringBuilder buf = new StringBuilder();

        if( note.getOctave() >= 5 ) {
            buf.append(n.toLowerCase());
            for( int i = 5; i < note.getOctave(); i++ ) {
                buf.append("'");
            }
        } else {
            buf.append(n.toUpperCase());
            for( int i = 4; i > note.getOctave(); i-- ) {
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
        } catch(IOException e) {
            return null;
        }
    }
}
