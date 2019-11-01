package de.saar.coli.arranger;

import javax.annotation.processing.Filer;
import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads limited ABC notation. Assumptions:
 * <p>
 * - song has a single voice
 * - only T, C, K, and M codes are read, others are ignored
 * - every note must have an explicit duration
 * - must have space between adjacent notes
 * - no rests
 */
public class AbcParser {
    private static Pattern LINE_PATTERN = Pattern.compile("\\s*(\\S+):\\s*(.+)");

    public Score read(Reader abcReader) throws IOException {
        BufferedReader r = new BufferedReader(abcReader);
        Score score = new Score("", "", "", 4);
        String line;

        while ((line = r.readLine()) != null) {
            Matcher m = LINE_PATTERN.matcher(line);

            if( line.startsWith("%")) {
                // skip
            } else if (m.matches()) {
                String key = m.group(1);
                String value = m.group(2).trim();

                switch (key) {
                    case "T":
                        score.setTitle(value);
                        break;
                    case "C":
                        score.setComposer(value);
                        break;
                    case "K":
                        score.setKey(value);
                        break;
                    case "M":
                        score.setQuartersPerMeasure(Integer.parseInt(value.substring(0, 1)));
                        break;
                }
            } else {
                int timeInEighths = 0;
                List<Note> leadPart = score.getPart(1);
                String[] potentialNotes = line.split("\\s+");

                for (String pn : potentialNotes) {
                    if (pn.startsWith("|")) {
                        // skip barlines
                    } else if( pn.startsWith("\"")) {
                        // chord
                        score.addChord(timeInEighths, parseAbcChord(pn));
                    } else {
                        // note
                        Note note = parseAbcNote(pn);
                        leadPart.add(note);
                        timeInEighths += note.getDuration();
                    }
                }
            }
        }

        return score;
    }

    private Chord parseAbcChord(String chord) {
        chord = chord.substring(1, chord.length()-1);
        return Chord.lookup(chord);
    }

    private Note parseAbcNote(String note) {
        int pos = 0;
        int absoluteOffset = 0;

        if (note.charAt(pos) == '^') {
            absoluteOffset = +1;
            pos++;
        } else if (note.charAt(pos) == '_') {
            absoluteOffset = -1;
            pos++;
        }

        String relativeNote = Character.toString(note.charAt(pos++));

        int octave = 0;
        if (Character.isUpperCase(relativeNote.charAt(0))) {
            octave = 4;
            while (note.charAt(pos) == ',') {
                octave--;
                pos++;
            }
        } else {
            octave = 5;
            while (note.charAt(pos) == '\'') {
                octave++;
                pos++;
            }
        }

        int duration = Integer.parseInt(note.substring(pos, pos + 1));

        Note ret = Note.create(relativeNote.toUpperCase(), octave, duration);
        return ret.add(absoluteOffset);
    }
}
