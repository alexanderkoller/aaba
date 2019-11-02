package de.saar.coli.arranger;

import abcj.ABCJ;
import abcj.model.Library;
import abcj.model.Tune;
import abcj.model.TuneBook;
import abcj.model.TuneList;
import abcj.ui.MainGUI;
import abcj.ui.MainPane;

import java.util.ArrayList;
import java.util.List;

public class Score {
    private List<Note>[] parts = new List[4]; // 0 = Tn, 1 = Ld, 2 = Br, 3 = Bs
    public static String[] PART_NAMES = new String[] { "Tn", "Ld", "Br", "Bs" };
    private String key;
    private String title;
    private String composer;
    private int quartersPerMeasure;
    private List<Pair<Integer,Chord>> chords = new ArrayList<>();

    public Score(String title, String composer, String key, int quartersPerMeasure) {
        this.title = title;
        this.composer = composer;
        this.key = key;
        this.quartersPerMeasure = quartersPerMeasure;

        for( int i = 0; i < 4; i++ ) {
            parts[i] = new ArrayList<>();
        }
    }

    public void addNote(int part, Note note) {
        parts[part].add(note);
    }

    public List<Note> getPart(int part) {
        return parts[part];
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public String getComposer() {
        return composer;
    }

    public int getQuartersPerMeasure() {
        return quartersPerMeasure;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public void setQuartersPerMeasure(int quartersPerMeasure) {
        this.quartersPerMeasure = quartersPerMeasure;
    }

    public void addChord(int startTime, Chord chord) {
        chords.add(new Pair<>(startTime, chord));
    }

    public Chord getChordAtTime(int time) {
        for( int i = 0; i < chords.size(); i++ ) {
            if( chords.get(i).getLeft() <= time
                && (i+1 == chords.size() || chords.get(i+1).getLeft() > time) ) {
                return chords.get(i).getRight();
            }
        }

        return null;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        for( int i = 0; i < 4; i++ ) {
            buf.append(PART_NAMES[i]);
            buf.append(":");
            for( Note note : parts[i]) {
                buf.append(" ");
                buf.append(note.toString());
            }
            buf.append("\n");
        }

        return buf.toString();
    }

    public static void main(String[] args) {
        Score s = new Score("Test Song", "AK", "C", 4);

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


        ScoreViewer viewer = new ScoreViewer();
        viewer.addScore(s);

        viewer.show();


//        Library lib = new Library();
//        TuneBook book = new TuneBook(lib, "tunebook.txt", "Test Tunebook", true);
//        TuneList list = new TuneList(lib, "Test Tunelist");
//
//        Tune tune = book.createNewTune();
//        tune.setABCText(S);
//
//        Tune tune2 = book.createNewTune();
//        tune.setABCText(SS);
//
//        list.addTune(tune);
//
//        ABCJ abcj = new ABCJ();
//        abcj.initApplication();
//        abcj.disableGUI();
//
//        System.err.println("x");
//
//        MainGUI gui = new MainPane(abcj);
//        System.err.println("y");
//
//        gui.addTuneBook(book);
//        gui.addTuneList(list);
//        gui.refreshTuneList(list);
//        gui.addNewTune(tune);
//        gui.addNewTune(tune2);
//        System.err.println("z");
//
//        abcj.refreshMenu();
//        abcj.enableGUI();
    }

    private static final String S = "X:1\n" +
            "T:Test Song\n" +
            "C:AK\n" +
            "M:4/4\n" +
            "K:C\n" +
            "\"C\" C,2 d''2 _E2 ^F2 | \"G7\" C2 D2 |]\n";


    private static final String SS = "X:1\n" +
            "T:Test Song 2\n" +
            "C:AK\n" +
            "M:4/4\n" +
            "K:C\n" +
            "\"C\" C,2 d''2 _E2 ^F2 | \"G7\" C2 D2 |]\n";
}
