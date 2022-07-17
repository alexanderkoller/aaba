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
 *     <li>Only T, C, K, Q, and M codes are read; all others are ignored. The K: field must come before all notes.</li>
 *     <li>If the song has more than one voice, only the last voice is used as the lead voice, and the others are ignored.</li>
 *     <li>Rests and ties are currently ignored.</li>
 *     <li>Chords are supported (enclosed in double quotes), and are spelled as explained in {@link de.saar.coli.arranger.Chord.ChordType}.</li>
 * </ul>
 */
public class AbcParser {
    public Score read(Reader abcReader) throws IOException, AbcParsingException {
        AbcNotationLexer lexer = new AbcNotationLexer(CharStreams.fromReader(abcReader));
        AbcNotationParser parser = new AbcNotationParser(new CommonTokenStream(lexer));

        AbcParseTreeVisitor visitor = new AbcParseTreeVisitor();
        ParseTreeWalker.DEFAULT.walk(visitor, parser.tune());
        return visitor.score;
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
            leadPart = score.getPart(1);
            leadPart.clear();
        }

        @Override
        public void enterTitle(AbcNotationParser.TitleContext ctx) {
            String title = ctx.string().getText().trim();
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
            String composer = ctx.string().getText().trim();
            score.setComposer(composer);
        }

        @Override
        public void enterTempo(AbcNotationParser.TempoContext ctx) {
            score.setTempo(ctx.getText().substring(2).trim());
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
                    ParseTree accidentalClass = ctx.accidental().getChild(0);

                    if (accidentalClass instanceof AbcNotationParser.SharpContext) {
                        accidentalOffset = +1;
                    } else if (accidentalClass instanceof AbcNotationParser.FlatContext) {
                        accidentalOffset = -1;
                    } else if (accidentalClass instanceof AbcNotationParser.NaturalContext) {
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
                    throw new RuntimeException(String.format("Unknown chord '%s' at time %d.\n", chordStr, timeInEighths));
                } else {
                    score.addChord(timeInEighths, chord);
                }
            }
        }

        @Override
        public void enterRest(AbcNotationParser.RestContext ctx) {
            System.err.println("rest");
        }

        @Override
        public void enterLine_of_words(AbcNotationParser.Line_of_wordsContext ctx) {
            String line = ctx.LINE().getText().trim();
            String[] words = line.split("\\s+");
            for( String word : words ) {
                score.addWord(word);
            }
        }
    }
}


