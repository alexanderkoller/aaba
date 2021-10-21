
/**
 * Copied and modified from https://github.com/EverydaySpice/jabc
*/



lexer grammar AbcNotationLexer;


@header{
    package de.saar.coli.arranger.abc;
}


// standard data types:
INT: [0-9]+;
NEWLINE: '\r' ? '\n';
WS: ('\t' | ' ')+;
// skip all tokens that are not \r or \n (New lines)
COMMENT: '%' ~[\r\n]* {System.out.println("lc > " + getText());} NEWLINE->skip;

// data storage symbols in the header.
// if the is a string following the data field,
// the lexer must change to the STRING_MODE sublexer.
IdentifierSymbol:   'X:';
TitleSymbol:        'T:' ->mode(STRING_MODE);
MeterSymbol:        'M:';
LengthSymbol:       'L:';
KeySymbol:          'K:'  ->mode(STRING_MODE);
NotesSymbol:        'N:'  ->mode(STRING_MODE);
VoiceSymbol:        'V:'  ->mode(LINE_MODE);
ComposerSymbol:     'C:'  ->mode(STRING_MODE);
TempoSymbol:        'Q:';
// skip all not supported symbols.
// TODO: support more symbols.
NotSupportedSymbol: [ABDFGHImOPRrSsUWwZ] ':' .*? NEWLINE ->skip;


// notes and other musical expressions:
NOTE: [a-gA-G];
MULTIPLIER:     INT;
OCTAVE_UP:      '\'';
OCTAVE_DOWN:    ',';
Flat:           '_';
Sharp:          '^';
Slash:          '/';

Rest:                   'z';
InvisibleRest:          'x';
BarRest:                'Z';
BracketOpen:            '(';
BracketClosed:          ')';
SquareBracketOpen:      '[';
SquareBracketClosed:    ']';
VerticalBar:            '|';
Colon:                  ':';
Equals:                 '=';
Minus:                  '-';
Backslash:              '\\';

// if there is a quotation mark, a string is following:
Quotationmark:          '"' ->mode(STRING_MODE);

Decoration:             ('!' [a-zA-Z0-9().+<>]* '!') |
                        '.'  |
                        '~'  |
                        'H'  |
                        'L'  |
                        'M'  |
                        'O'  |
                        'P'  |
                        'S'  |
                        'T'  |
                        'u'  |
                        'v'  ;

// Sublexer for parsing strings. Needed because there are conflicts
// when lexing Notes like "G2G2", which looks also like a string.
mode STRING_MODE;
STRING_MODE_EXIT: (NEWLINE | '"' | ']' ) ->mode(DEFAULT_MODE);
STRING_MODE_COMMENT: '%' ~[\r\n]* {System.out.println("lc > " + getText());}->skip;
STRING: (ID | INT | WS | [-_;,/.$§!?=&()] | '|' )+;
ID: [a-zA-Z \u00C4 \u00D6 \u00DC \u00E4 \u00F6 \u00FC]+;

mode LINE_MODE;
LINE_MODE_EXIT: NEWLINE ->mode(DEFAULT_MODE);
LINE: (~('\n'))+;

