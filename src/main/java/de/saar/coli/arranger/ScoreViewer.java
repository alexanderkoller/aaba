package de.saar.coli.arranger;

import abcj.ABCJ;
import abcj.model.Library;
import abcj.model.Tune;
import abcj.model.TuneBook;
import abcj.model.TuneList;
import abcj.ui.MainGUI;
import abcj.ui.MainPane;
import de.saar.coli.arranger.abc.AbcWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays a score using ABCJ. Works with basic functionality for now,
 * but because ABCJ only supports ABC 1.6, multiple voices are not
 * displayed correctly.
 *
 */
public class ScoreViewer {
    private List<Tune> abcs = new ArrayList<>();
    private TuneBook book;
    private TuneList list;
    private MainGUI gui;
    private ABCJ abcj;

    public ScoreViewer() {
        Library lib = new Library();
        book = new TuneBook(lib, "tunebook.txt", "Test Tunebook", true);
        list = new TuneList(lib, "Test Tunelist");

        abcj = new ABCJ();
        abcj.initApplication();
        abcj.disableGUI();

        gui = new MainPane(abcj);
    }

    public void addScore(Score score) {
        StringWriter sw = new StringWriter();

        try {
            new AbcWriter().write(score, sw);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Tune tune = book.createNewTune();
        tune.setABCText(sw.toString());
        list.addTune(tune);
        abcs.add(tune);
    }

    public void show() {
        gui.addTuneBook(book);
        gui.addTuneList(list);
        gui.refreshTuneList(list);

        // TODO support multiple tunes
        gui.addNewTune(abcs.get(0));

        abcj.refreshMenu();
        abcj.enableGUI();
    }
}
