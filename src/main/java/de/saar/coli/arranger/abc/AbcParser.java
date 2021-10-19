package de.saar.coli.arranger.abc;

import de.saar.coli.arranger.Chord;
import de.saar.coli.arranger.Key;
import de.saar.coli.arranger.Note;
import de.saar.coli.arranger.Score;
import de.saar.coli.arranger.abc.AbcNotationLexer;
import de.saar.coli.arranger.abc.AbcNotationParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Trees;

import javax.annotation.processing.Filer;
import java.io.*;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a score in ABC notation and returns it as a
 * {@link Score}. The primary use case is to read just
 * a melody with its accompanying lyrics and chords.
 * As such, this class does not support arbitrary ABC notation,
 * but makes the following assumptions:
 * <ul>
 *     <li>The song has a single voice.</li>
 *     <li>Only T, C, K, Q, and M codes are read; all others are ignored. The K: field must come before all notes.</li>
 *     <li>The L: code (base unit of time) is assumed to be 1/8 notes.</li>
 *     <li>Every note must have an explicit duration (in 1/8 notes).</li>
 *     <li>Adjacent notes are separated by whitespace.</li>
 *     <li>Rests are not supported.</li>
 *     <li>Chords are supported (enclosed in double quotes), and are spelled as explained in {@link de.saar.coli.arranger.Chord.ChordType}.</li>
 * </ul>
 */
public class AbcParser {
    private static Pattern LINE_PATTERN = Pattern.compile("\\s*(\\S+):\\s*(.+)");

    /**
     * Reads a Score from a Reader.
     *
     * @param abcReader
     * @return
     * @throws IOException - an I/O error occurred
     * @throws AbcParsingException - something went wrong in parsing the ABC notation
     */
    public Score read(Reader abcReader) throws IOException, AbcParsingException {
        BufferedReader r = new BufferedReader(abcReader);
        Score score = new Score("", "", "", 4);
        String line;
        int timeInEighths = 0;
        Key key = Key.lookup("C");

        while ((line = r.readLine()) != null) {
            Matcher m = LINE_PATTERN.matcher(line);

            if( line.startsWith("%")) {
                // skip
            } else if (m.matches()) {
                String configKey = m.group(1);
                String value = m.group(2).trim();

                switch (configKey) {
                    case "T":
                        score.setTitle(value);
                        break;
                    case "C":
                        score.setComposer(value);
                        break;
                    case "K":
                        score.setKey(value);
                        key = Key.lookup(value);
                        break;
                    case "M":
                        score.setQuartersPerMeasure(Integer.parseInt(value.substring(0, 1)));
                        break;
                    case "Q":
                        score.setTempo(value);
                        break;

                    case "w":
                        String[] words = value.split("\\s+");
                        for( String word : words ) {
                            score.addWord(word);
                        }
                        break;
                }
            } else {
                List<Note> leadPart = score.getPart(1);
                String[] potentialNotes = line.split("\\s+");

//                System.err.println(Arrays.toString(potentialNotes)); // debugging #7

                for (String pn : potentialNotes) {
                    if( pn.isEmpty() ) {
                        continue;
                    } else if (pn.startsWith("|")) {
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
//                        System.err.printf("read note: [%s]\n", pn);  // debugging #7
                        Note note = parseAbcNote(pn, key);
                        leadPart.add(note);
                        timeInEighths += note.getDuration();
                    }
                }
            }
        }

        return score;
    }

    private Chord parseAbcChord(String chord) {
//        System.err.printf("parse chord: %s\n", chord); // debugging #7
        chord = chord.substring(1, chord.length()-1);
        Chord ret = Chord.lookup(chord);
//        System.err.printf("root: %d\n", ret.getRoot()); // debugging #7
        return ret;
    }

    // TODO - add accidentals as defined by key
    private Note parseAbcNote(String note, Key key) throws AbcParsingException {
        int pos = 0;
        Character accidental = null;
        int accidentalOffset = 0;

        // explicit accidentals
        if (note.charAt(pos) == '^' || note.charAt(pos) == '_' || note.charAt(pos) == '=') {
            accidental = note.charAt(pos++);
        }

        // interpret note in current key
        String relativeNote = Character.toString(note.charAt(pos++));
        accidentalOffset = key.getAccidentalForNote(relativeNote);

        // explicit accidentals overwrite the accidental offset
        if( accidental != null ) {
            switch(accidental) {
                case '^': accidentalOffset = +1; break;
                case '_': accidentalOffset = -1; break;
                case '=': accidentalOffset = 0; break;
            }
        }

        // move note to correct octave
        int octave = 0;
        if (Character.isUpperCase(relativeNote.charAt(0))) {
            octave = 4;
            while (pos < note.length() && note.charAt(pos) == ',') {
                octave--;
                pos++;
            }
        } else {
            octave = 5;
            while (pos < note.length() && note.charAt(pos) == '\'') {
                octave++;
                pos++;
            }
        }

        // parse duration
        int duration;
        try {
            if( pos >= note.length() ) { // note is over
                duration = 1;
            } else {
                duration = Integer.parseInt(note.substring(pos, pos + 1));
            }
        } catch(NumberFormatException e) {
            throw new AbcParsingException("Could not parse ABC note: " + note, e);
        }

        Note ret = Note.create(relativeNote.toUpperCase(), octave, duration);
        ret = ret.add(accidentalOffset);
//        System.err.printf("%s -> %s\n", note, ret); // debugging #7
        return ret;
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

    public static void main(String[] args) throws IOException {
        AbcNotationLexer lexer = new AbcNotationLexer(CharStreams.fromFileName("issue7-nopiano.abc"));
        for(Token tok : lexer.getAllTokens()) {
            System.err.printf("[%s] %s\n", lexer.getVocabulary().getSymbolicName(tok.getType()), tok);
        }

//        System.err.println(lexer.getAllTokens());
        lexer = new AbcNotationLexer(CharStreams.fromFileName("issue7-nopiano.abc"));
        AbcNotationParser parser = new AbcNotationParser(new CommonTokenStream(lexer));
        System.err.println(printSyntaxTree(parser, parser.tune()));
//        System.err.println(parser.tune());
    }


    public static String printSyntaxTree(Parser parser, ParseTree root) {
        StringBuilder buf = new StringBuilder();
        recursive(root, buf, 0, Arrays.asList(parser.getRuleNames()));
        return buf.toString();
    }

    private static void recursive(ParseTree aRoot, StringBuilder buf, int offset, List<String> ruleNames) {
        for (int i = 0; i < offset; i++) {
            buf.append("  ");
        }
        buf.append(Trees.getNodeText(aRoot, ruleNames)).append("\n");
        if (aRoot instanceof ParserRuleContext) {
            ParserRuleContext prc = (ParserRuleContext) aRoot;
            if (prc.children != null) {
                for (ParseTree child : prc.children) {
                    recursive(child, buf, offset + 1, ruleNames);
                }
            }
        }
    }
}
