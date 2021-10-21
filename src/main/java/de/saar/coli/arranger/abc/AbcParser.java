package de.saar.coli.arranger.abc;

import de.saar.coli.arranger.*;
import de.saar.coli.arranger.abc.AbcNotationLexer;
import de.saar.coli.arranger.abc.AbcNotationParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.Trees;
import org.antlr.v4.runtime.tree.xpath.XPath;

import javax.annotation.processing.Filer;
import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.saar.coli.arranger.Arrange.loadConfig;

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
    public Score readA(Reader abcReader) throws IOException, AbcParsingException {
        AbcNotationLexer lexer = new AbcNotationLexer(CharStreams.fromReader(abcReader));
        AbcNotationParser parser = new AbcNotationParser(new CommonTokenStream(lexer));

        AbcParseTreeVisitor visitor = new AbcParseTreeVisitor();
        ParseTreeWalker.DEFAULT.walk(visitor, parser.tune());
        return visitor.score;
    }



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

    public static void main(String[] args) throws IOException, AbcParsingException {


//
        printParseTree("issue7-orig.abc");
        Score s = new AbcParser().readA(new FileReader("issue7-orig.abc"));
        System.err.println(s);
        Config config = loadConfig("aaba.yaml");
        FileWriter w = new FileWriter("x.abc");
        new AbcWriter(config).write(s, w);
        w.flush();
        w.close();

//
//        AbcNotationLexer lexer = new AbcNotationLexer(CharStreams.fromFileName("issue7-orig.abc"));
//        for(Token tok : lexer.getAllTokens()) {
//            System.err.printf("[%s] %s\n", lexer.getVocabulary().getSymbolicName(tok.getType()), tok);
//        }
////
//////        System.err.println(lexer.getAllTokens());
//        lexer = new AbcNotationLexer(CharStreams.fromFileName("issue7-orig.abc"));
//        AbcNotationParser parser = new AbcNotationParser(new CommonTokenStream(lexer));
//        System.err.println(printSyntaxTree(parser, parser.tune()));
//////        System.err.println(parser.tune());
    }

    private static void printParseTree(String filename) throws IOException {
        AbcNotationLexer lexer = new AbcNotationLexer(CharStreams.fromFileName(filename));
        AbcNotationParser parser = new AbcNotationParser(new CommonTokenStream(lexer));
        System.err.println(printSyntaxTree(parser, parser.tune()));
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

    private static class AbcParseTreeVisitor extends de.saar.coli.arranger.abc.AbcNotationParserBaseListener {
        private Score score = new Score("", "", "", 4);
        private int timeInEighths = 0;
        private Key key = Key.lookup("C");
        private List<Note> leadPart = null;
        private int defaultNoteLength = 0; // in eighths

        // TODO: read words - also need to implement this in grammar

        @Override
        public void enterVoiceInfo(AbcNotationParser.VoiceInfoContext ctx) {
            leadPart = null;
        }

        @Override
        public void exitVoiceInfo(AbcNotationParser.VoiceInfoContext ctx) {
//            System.err.println(ctx.LINE().getText());
            leadPart = score.getPart(1);
        }

        @Override
        public void enterTitle(AbcNotationParser.TitleContext ctx) {
            String title = ctx.string().getText().trim();
//            System.err.printf("TITLE: %s\n", title);
            score.setTitle(title);
        }

        @Override
        public void enterMeter(AbcNotationParser.MeterContext ctx) {
            int numerator = Integer.parseInt(ctx.fraction().numerator.getText());
            int denominator = Integer.parseInt(ctx.fraction().denominator.getText());

            int numeratorInQuarters = (numerator*4)/denominator; // numerator of the */4 time signature that is equivalent to n/d
            score.setQuartersPerMeasure(numeratorInQuarters);

            if( defaultNoteLength == 0 ) {
                // If no L: is given, it is determined from M:
                // See here: https://abcnotation.com/wiki/abc:standard:v2.1#lunit_note_length
                if( ((double) numerator) / denominator < 0.75 ) {
                    setDefaultNoteLength(1, 16);
                } else {
                    setDefaultNoteLength(1, 8);
                }
            }
        }

        private void setDefaultNoteLength(int numerator, int denominator) {
            int eighthsInDenominator = 8/denominator; // hopefully the denominator is a divisor of 8
            defaultNoteLength = numerator * eighthsInDenominator;
        }

        @Override
        public void enterLength(AbcNotationParser.LengthContext ctx) {
            int numerator = Integer.parseInt(ctx.fraction().numerator.getText());
            int denominator = Integer.parseInt(ctx.fraction().denominator.getText());
            setDefaultNoteLength(numerator, denominator);
        }

        @Override
        public void enterKey(AbcNotationParser.KeyContext ctx) {
            String keyString = ctx.string().getText().trim();
            score.setKey(keyString);
            key = Key.lookup(keyString);
        }

        @Override
        public void enterComposer(AbcNotationParser.ComposerContext ctx) {
            String composer = ctx.string().getText();
            score.setComposer(composer);
        }

        @Override
        public void enterTempo(AbcNotationParser.TempoContext ctx) {
            // TODO: score.setTempo(value);
            super.enterTempo(ctx);
        }

        @Override
        public void enterNote(AbcNotationParser.NoteContext ctx) {
            if( leadPart != null ) {
                String noteStr = ctx.noteExpression().noteString.getText();

                // interpret note in current key
                String relativeNote = Character.toString(noteStr.charAt(0));
                int accidentalOffset = key.getAccidentalForNote(relativeNote);

                // explicit accidentals overwrite the accidental offset
                if (ctx.accidental() != null) {
                    if (ctx.accidental().sharp() != null) {
                        accidentalOffset = +1;
                    } else if (ctx.accidental().flat() != null) {
                        accidentalOffset = -1;
                    } else if (ctx.accidental().natural() != null) {
                        accidentalOffset = 0;
                    }
                }

                // move note to correct octave
                int octave = Character.isUpperCase(relativeNote.charAt(0)) ? 4 : 5;

                if( ctx.noteOctave() != null ) {
                    if (ctx.noteOctave().octaveUp() != null) {
                        octave += ctx.noteOctave().octaveUp().size();
                    }

                    if (ctx.noteOctave().octaveDown() != null) {
                        octave -= ctx.noteOctave().octaveDown().size();
                    }
                }

                // parse duration
                int duration = defaultNoteLength;
                if (ctx.noteLength() != null) {
                    duration = Integer.parseInt(ctx.noteLength().multiplier().getText());
                }

                Note note = Note.create(relativeNote.toUpperCase(), octave, duration);
                note = note.add(accidentalOffset);

                leadPart.add(note);
                timeInEighths += duration;
            }
        }

        @Override
        public void enterChord(AbcNotationParser.ChordContext ctx) {
            if( leadPart != null ) {  // skip spurious "chords" from the header
                String chordStr = ctx.STRING().getText();
                Chord chord = Chord.lookup(chordStr);

                if (chord == null) {
                    System.err.printf("Warning: unknown chord '%s' at time %d.\n", chordStr, timeInEighths);
                } else {
                    score.addChord(timeInEighths, chord);
                }
            }
        }

        @Override
        public void enterRest(AbcNotationParser.RestContext ctx) {
            System.err.println("rest");
        }
    }
}

