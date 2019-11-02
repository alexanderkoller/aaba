package de.saar.coli.arranger;

import javax.annotation.processing.Filer;
import java.io.*;
import java.text.ParseException;
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

    public Score read(Reader abcReader) throws IOException, AbcParsingException {
        BufferedReader r = new BufferedReader(abcReader);
        Score score = new Score("", "", "", 4);
        String line;
        int timeInEighths = 0;

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
                List<Note> leadPart = score.getPart(1);
                String[] potentialNotes = line.split("\\s+");

                for (String pn : potentialNotes) {
                    if (pn.startsWith("|")) {
                        // skip barlines
                    } else if( pn.startsWith("\"")) {
                        // chord
                        Chord chord = parseAbcChord(pn);
                        if( chord == null ) {
                            throw new AbcParsingException("Could not parse chord: " + pn);
                        }
                        score.addChord(timeInEighths, chord);
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

    // TODO - add accidentals as defined by key
    private Note parseAbcNote(String note) throws AbcParsingException {
        int pos = 0;
        Character accidental = null;
        int absoluteOffset = 0;

        if (note.charAt(pos) == '^') {
            absoluteOffset = +1;
            pos++;
        } else if (note.charAt(pos) == '_') {
            absoluteOffset = -1;
            pos++;
        } else if( note.charAt(pos) == '=') {
            absoluteOffset = 0;
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

        int duration;

        try {
            duration = Integer.parseInt(note.substring(pos, pos + 1));
        } catch(NumberFormatException e) {
            throw new AbcParsingException("Could not parse ABC note: " + note, e);
        }

        Note ret = Note.create(relativeNote.toUpperCase(), octave, duration);
        return ret.add(absoluteOffset);
    }

    public static class AbcParsingException extends Exception {
        public AbcParsingException() {
        }

        public AbcParsingException(String message) {
            super(message);
        }

        public AbcParsingException(String message, Throwable cause) {
            super(message, cause);
        }

        public AbcParsingException(Throwable cause) {
            super(cause);
        }

        public AbcParsingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}
