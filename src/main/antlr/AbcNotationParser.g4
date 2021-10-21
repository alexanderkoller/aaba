

/**
 * Copied and modified from https://github.com/EverydaySpice/jabc
*/



parser grammar AbcNotationParser;


@header{
    package de.saar.coli.arranger.abc;
}

options { tokenVocab=AbcNotationLexer; }

// --->BASIC RULES:
fraction: numerator=INT Slash denominator=INT;
string: text=STRING STRING_MODE_EXIT;
endOfLine: NEWLINE | COMMENT;

// --->TUNE:
tune: header (voice)+ ;
// voiceInfo example: V: left hand
voice: (voiceInfo score*);
voiceInfo: VoiceSymbol text=STRING STRING_MODE_EXIT;
score:(bar suppresScoreLinebreak?)+ (NEWLINE | EOF);
// >---END OF TUNE

// --->HEADER:
header: identifier title+ (meter| length| notes| tempo | composer )* key;

identifier:     IdentifierSymbol    text=INT         endOfLine;

title:          TitleSymbol  string ;

meter:          MeterSymbol (NOTE | fraction) endOfLine?;

length:         LengthSymbol        space? fraction space? endOfLine;

key:            KeySymbol           string      ;

notes:          NotesSymbol   string     ;

composer:       ComposerSymbol   string      ;

tempo:          TempoSymbol space? (fractionTempo | stringTempo | integerTempo) space* endOfLine;
fractionTempo:  stringQuotation? (space? fraction)+ space? Equals space? speed=INT space? stringQuotation?;
stringTempo:    stringQuotation;
stringQuotation: Quotationmark string;
integerTempo:    stringQuotation? speed=INT stringQuotation?;
// >---END OF HEADER


// --->BAR:
bar: space* (musicalExpression)+ endOfBar;
endOfBar: space* barline;
barline: simpleBarline
       | thinThikBarline
       | thikThinBarline
       | thinThinBarline
       | startOfRepeatedBarline
       | endOfRepeatedBarline
       | startAndEndOfRepeatedBarline;

simpleBarline: VerticalBar;
thinThinBarline: VerticalBar VerticalBar;
thikThinBarline: SquareBracketOpen VerticalBar;
thinThikBarline: VerticalBar SquareBracketClosed;
startOfRepeatedBarline: VerticalBar Colon;
endOfRepeatedBarline: Colon VerticalBar;
startAndEndOfRepeatedBarline: Colon Colon;
suppresScoreLinebreak: Backslash NEWLINE;
// >---BAR


// --->NOTES and other muscial Expressions:
musicalExpression: (inlineField | slurStart | slurEnd | multipleNotes | note | chord | rest );

// if there is whitespace (space) before the note it is assumed that its beam should be
// displayed broken. This has to be checked in the visitor when visiting a note node.
note: space? decoration? accidental? noteExpression noteOctave? noteLength? tiedNote?;

chord: space? Quotationmark STRING STRING_MODE_EXIT;

multipleNotes: decorationExpression? space* SquareBracketOpen (note)+ SquareBracketClosed tiedNote?;

rest: space? (Rest | InvisibleRest) noteLength space?;

noteExpression: noteString=NOTE;
decoration: decorationName=Decoration;
decorationExpression: decorationName=Decoration;
noteLength: (delimiter
            | multiplier);

noteOctave: (octaveUp
            | octaveDown)+;

accidental: (flat
            | sharp
            | natural)+;

slurStart: space? BracketOpen;
slurEnd:   space? BracketClosed;

inlineField: (meterChange | lengthChange | keyChange | tempoChange);
meterChange: SquareBracketOpen meter SquareBracketClosed;
lengthChange: SquareBracketOpen length SquareBracketClosed;
keyChange: SquareBracketOpen key SquareBracketClosed?;
tempoChange: SquareBracketOpen tempo SquareBracketClosed;

flat: Flat;
sharp: Sharp;
natural: Equals;
tiedNote: Minus;

delimiter: Slash denominator=INT?;
multiplier: (numerator=INT |  numerator=INT Slash+ denominator=INT?);
octaveUp: OCTAVE_UP;
octaveDown: OCTAVE_DOWN;

space: (WS | NEWLINE)+;
// >---END OF NOTES
