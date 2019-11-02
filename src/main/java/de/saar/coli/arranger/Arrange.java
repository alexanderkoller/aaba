package de.saar.coli.arranger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Arrange {
    private static final VoicePart[] VOICE_PARTS = new VoicePart[]{
            new VoicePart("Tenor", Note.create("G3", 0), Note.create("B4", 0)),
            new VoicePart("Lead", Note.create("C3", 0), Note.create("G4", 0)),
            new VoicePart("Baritone", Note.create("C3", 0), Note.create("G4", 0)),
            new VoicePart("Bass", Note.create("F2", 0), Note.create("C4", 0))
    };

    private static final int TENOR = 0, LEAD = 1, BARI = 2, BASS = 3;

    public static void main(String[] args) throws IOException, AbcParser.AbcParsingException {
        Score score = new AbcParser().read(new FileReader("down_our_way.abc"));

        // print score
        score.foreachNoteAndChord(LEAD, (note, chord) -> {
            StringBuilder buf = new StringBuilder();

            for (int chordNote : chord.getNotes()) {
                buf.append(Note.getNoteName(chordNote));
                buf.append(" ");
            }

//            System.out.printf("%s %s -> %s\n", note, chord, buf.toString());
        });

        Arrange arranger = new Arrange();
        Score bestArrangedScore = arranger.arrange(score);

//        System.out.println(bestArrangedScore);

        AbcWriter abcw = new AbcWriter();
        FileWriter fw = new FileWriter("arranged.abc");
        abcw.write(bestArrangedScore, fw);
        fw.flush();
        fw.close();
    }

    private Score arrange(Score score) {
        List<List<List<Note>>> possibleNotes = computePossibleNotes(score);
//        for (List<List<Note>> notesAtTime : possibleNotes) {
//            System.out.println();
//            System.out.println("Tn: " + notesAtTime.get(TENOR));
//            System.out.println("Ld: " + notesAtTime.get(LEAD));
//            System.out.println("Br: " + notesAtTime.get(BARI));
//            System.out.println("Bs: " + notesAtTime.get(BASS));
//        }

        int n = score.countNotes(LEAD);
        assert n == possibleNotes.size();

        Map<Item, Integer> bestScores = new HashMap<>();
        BackpointerColumn backpointers = new BackpointerColumn(null);
        int time = 0;

        for (int pos = 0; pos < n; pos++) {
//            System.err.printf("Processing position %d ...\n", pos);

            List<List<Note>> notesHere = possibleNotes.get(pos);
            Chord chordHere = score.getChordAtTime(time);

            Map<Item, Integer> bestScoresNext = new HashMap<>();
            BackpointerColumn backpointersNext = new BackpointerColumn(backpointers);

            for (Note bs : notesHere.get(BASS)) {
                if (chordHere.isAllowedBassNote(bs)) {
                    for (Note ld : notesHere.get(LEAD)) {
                        for (Note br : notesHere.get(BARI)) {
                            for (Note tn : notesHere.get(TENOR)) {
                                int itemScore = scoreLexicalChord(tn, ld, br, bs, chordHere);

                                if (itemScore > Integer.MIN_VALUE) {
                                    Note[] notes = new Note[]{tn, ld, br, bs};
                                    Item it = new Item(notes);

                                    if (pos == 0) {
                                        // first timestep
                                        backpointersNext.getBackpointers().put(it, null); // null backpointer = lexical cell
                                        bestScoresNext.put(it, itemScore);
                                    } else {
                                        // later timesteps
                                        for (Map.Entry<Item, Integer> oldEntry : bestScores.entrySet()) {
                                            int transitionScore = scoreTransition(oldEntry.getKey().lastNotes, notes);
                                            int totalScore = itemScore + transitionScore + oldEntry.getValue();

                                            if (totalScore >= -100) {
                                                Item newItem = new Item(notes);
                                                Backpointer bp = new Backpointer(oldEntry.getKey(), totalScore);
                                                backpointersNext.getBackpointers().put(newItem, bp);

                                                Integer oldBestScore = bestScoresNext.get(newItem);
                                                if (oldBestScore == null || oldBestScore < totalScore) {
                                                    bestScoresNext.put(newItem, totalScore);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            time += score.getPart(LEAD).get(pos).getDuration();
            bestScores = bestScoresNext;
            backpointers = backpointersNext;
        }


        // show goal items
        List<Map.Entry<Item, Integer>> sortedEntries = new ArrayList<>(bestScores.entrySet());
        Collections.sort(sortedEntries, Comparator.comparing(Map.Entry::getValue));
        Map.Entry<Item, Integer> bestGoalItem = sortedEntries.get(sortedEntries.size() - 1);
        System.out.printf("Best arrangement has score %d\n", bestGoalItem.getValue());

        Score bestArrangedScore = extractBestScore(bestGoalItem.getKey(), backpointers, score);
        return bestArrangedScore;
    }

    private static class BackpointerColumn {
        private BackpointerColumn previous;
        private ListMultimap<Item, Backpointer> backpointers;

        public BackpointerColumn(BackpointerColumn previous) {
            this.previous = previous;
            backpointers = ArrayListMultimap.create();
        }

        public BackpointerColumn getPrevious() {
            return previous;
        }

        public ListMultimap<Item, Backpointer> getBackpointers() {
            return backpointers;
        }
    }

    private Score extractBestScore(Item bestFinalItem, BackpointerColumn backpointers, Score originalScore) {
        List<Note[]> notes = new ArrayList<>();
        Item item = bestFinalItem;

//        System.err.printf("[%03d] %s\n", notes.size(), item);
        notes.add(item.lastNotes);

        while (backpointers != null) {
            List<Backpointer> backpointersHere = backpointers.getBackpointers().get(item);
            Collections.sort(backpointersHere, Comparator.comparing(Backpointer::getScore).reversed());
            Backpointer bp = backpointersHere.get(0); // best backpointer

            if (bp == null) {
                break;
            } else {
                item = bp.getPreviousItem();
                System.err.printf("[%03d] %s\n", notes.size(), item);
                notes.add(item.lastNotes);

                backpointers = backpointers.getPrevious();
            }
        }

        Collections.reverse(notes);

        Score ret = new Score();
        ret.setTitle(originalScore.getTitle());
        ret.setComposer("AK arranger");
        ret.setKey(originalScore.getKey());
        ret.setQuartersPerMeasure(originalScore.getQuartersPerMeasure());
        ret.setLyrics(originalScore.getLyrics());

        for( Pair<Integer,Chord> chord : originalScore.getAllChords()) {
            ret.addChord(chord.getLeft(), chord.getRight());
        }

        for (Note[] notesHere : notes) {
            for (int part = 0; part < 4; part++) {
                ret.addNote(part, notesHere[part]);
            }
        }

        return ret;
    }

    private int scoreTransition(Note[] from, Note[] to) {
        int score = 0;

        // penalize wide jumps in Br and Tn
        for (int part = 0; part < 4; part++) {
            if (part == TENOR || part == BARI) {
                if (from[part].getAbsoluteDistance(to[part]) > 3) {
                    score -= 20;
                }
            }
        }

        // penalize parallel octaves
        for (int part = 0; part < 4; part++) {
            for (int other = part + 1; other < 4; other++) {
                if (from[part].getAbsoluteDistance(from[other]) == 12) {
                    if (to[part].getAbsoluteDistance(to[other]) == 12) {
                        score -= 20;
                    }
                }
            }
        }

        return score;
    }

    private int scoreLexicalChord(Note tn, Note ld, Note br, Note bs, Chord chord) {
        // must use all chord notes
        Set<Integer> differentRelativeNotes = new HashSet<>(List.of(tn.getRelativeNote(), ld.getRelativeNote(), br.getRelativeNote(), bs.getRelativeNote()));
        if (differentRelativeNotes.size() < chord.getNotes().size()) {
            return Integer.MIN_VALUE;
        }

        int score = 0;

        // Tn likes to be highest note
        if (tn.getAbsoluteNote() < ld.getAbsoluteNote() || tn.getAbsoluteNote() < br.getAbsoluteNote()) {
            score -= 10;
        }

        // Bs really wants to be lowest note
        if (bs.getAbsoluteNote() > br.getAbsoluteNote() || bs.getAbsoluteNote() > ld.getAbsoluteNote() || bs.getAbsoluteNote() > tn.getAbsoluteNote()) {
//            score -= 100;
            return Integer.MIN_VALUE;
        }

        // disprefer unison
        Set<Integer> differentAbsoluteNotes = new HashSet<>(List.of(tn.getAbsoluteNote(), ld.getAbsoluteNote(), br.getAbsoluteNote(), bs.getAbsoluteNote()));
        if (differentAbsoluteNotes.size() < 4) {
            score -= 50;
        }

        // disprefer very wide spread
        int highest = Collections.max(differentAbsoluteNotes);
        int lowest = Collections.min(differentAbsoluteNotes);
        if (highest - lowest > 19) { // octave + third
            score -= 20;
        }

        return score;
    }

    // possibleNotes[time][part] = list(possible notes for that part at that time)
    private List<List<List<Note>>> computePossibleNotes(Score score) {
        List<List<List<Note>>> ret = new ArrayList<>();

        score.foreachNoteAndChord(LEAD, (note, chord) -> {
            Set<Integer> chordNotes = chord.getNotes();
            List<List<Note>> notesAtTime = new ArrayList<>();
            ret.add(notesAtTime);

            for (int part = 0; part < 4; part++) {
                if (part == LEAD) {
                    List<Note> x = List.of(note);
                    notesAtTime.add(x);
                } else {
                    notesAtTime.add(VOICE_PARTS[part].getNotesInRange(chordNotes, note.getDuration()));
                }
            }
        });

        return ret;
    }

    private static class Backpointer {
        private int score;
        private Item previousItem;

        public Backpointer(Item previousItem, int score) {
            this.score = score;
            this.previousItem = previousItem;
        }

        public int getScore() {
            return score;
        }

        public Item getPreviousItem() {
            return previousItem;
        }

        @Override
        public String toString() {
            return "Backpointer{" +
                    "score=" + score +
                    ", previousItem=" + previousItem +
                    '}';
        }
    }

    private static class Item {
        private Note[] lastNotes;

        public Item(Note[] lastNotes) {
            this.lastNotes = lastNotes;
        }

        public Note[] getLastNotes() {
            return lastNotes;
        }

        @Override
        public String toString() {
            return String.format("[%s %s %s %s]",
                    lastNotes[TENOR], lastNotes[LEAD], lastNotes[BARI], lastNotes[BASS]);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Item item = (Item) o;

            return lastNotes[0].getAbsoluteNote() == item.lastNotes[0].getAbsoluteNote()
                    && lastNotes[1].getAbsoluteNote() == item.lastNotes[1].getAbsoluteNote()
                    && lastNotes[2].getAbsoluteNote() == item.lastNotes[2].getAbsoluteNote()
                    && lastNotes[3].getAbsoluteNote() == item.lastNotes[3].getAbsoluteNote();
        }

        @Override
        public int hashCode() {
            int[] x = new int[]{lastNotes[0].getAbsoluteNote(), lastNotes[1].getAbsoluteNote(), lastNotes[2].getAbsoluteNote(), lastNotes[3].getAbsoluteNote()};
            return Arrays.hashCode(x);
        }
    }
}
