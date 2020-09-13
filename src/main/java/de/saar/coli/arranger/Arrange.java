package de.saar.coli.arranger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import de.saar.coli.arranger.abc.AbcParser;
import de.saar.coli.arranger.abc.AbcWriter;
import de.saar.coli.arranger.rules.*;

import java.io.*;
import java.util.*;

/**
 * Arranges a melody and chords into a barbershop arrangement.
 * This uses a Viterbi-style algorithm to traverse the melody
 * from left to right, remembering possible chord voicings
 * for each time step together with the best score of the voicings
 * and voice leading decisions up to that point. This algorithm
 * runs in linear time in the length of the melody. The scores
 * for the voicings and voice leadings are determined by
 * the rules in the de.saar.coli.arranger.rules package.
 */
public class Arrange {
    private Config config;

    private static final VoiceLeadingRule[] VOICE_LEADING_RULES = {
            new LdHarmonyLeaps(),
            new LdParallelOctaves()
    };

    private static final VoicingRule[] VOICING_RULES = {
            new VoUseAllChordNotes(),
            new VoBassLowest(),
            new VoTenorCrossing(),
            new VoUnisonNotes(),
            new VoWideSpread()
    };

    public Arrange(Config config) {
        this.config = config;
    }

    public static void main(String[] args) throws IOException, AbcParser.AbcParsingException {
        Args arguments = new Args();
        JCommander jc = JCommander.newBuilder().addObject(arguments).build();
        jc.parse(args);

        if (arguments.help) {
            jc.usage();
            System.exit(0);
        }

        Config config = loadConfig(arguments.configFilename);

        System.out.printf("Reading melody and chords from: %s\n", arguments.inputFilename);
        System.out.printf("Writing arrangement to: %s\n\n", arguments.outputFilename);

        Score score = new AbcParser().read(new FileReader(arguments.inputFilename));
        Arrange arranger = new Arrange(config);
        Arrangement bestArrangement = arranger.arrange(score);

        if( bestArrangement == null ) {
            System.out.println("Could not find a valid arrangement.");
        } else {
            AbcWriter abcw = new AbcWriter(config);
            FileWriter fw = new FileWriter(arguments.outputFilename);
            abcw.write(bestArrangement.getArrangement(), fw);
            fw.flush();
            fw.close();
        }
    }

    public static Config loadConfig(String configFilename) throws FileNotFoundException {
        if( configFilename != null && new File(configFilename).exists() ) {
            System.out.printf("Reading configuration from %s ...\n", configFilename);
            return Config.read(new FileReader(configFilename));
        } else {
            System.out.println("Using default configuration.");
            return Config.read(new InputStreamReader(Arrange.class.getResourceAsStream("/config.yaml")));
        }
    }

    public Arrangement arrange(Score score) {
        long startTime = System.nanoTime();
        int n = score.countNotes(VoicePart.LEAD);
        List<List<List<Note>>> possibleNotes = computePossibleNotes(score);
        Map<Item, Integer> bestScores = new HashMap<>();
        BackpointerColumn backpointers = new BackpointerColumn(null);
        int time = 0;

        assert n == possibleNotes.size();

        for (int pos = 0; pos < n; pos++) {
            List<List<Note>> notesHere = possibleNotes.get(pos);
            Chord chordHere = score.getChordAtTime(time);

            Map<Item, Integer> bestScoresNext = new HashMap<>();
            BackpointerColumn backpointersNext = new BackpointerColumn(backpointers);

            for (Note bs : notesHere.get(VoicePart.BASS)) {
                if (chordHere.isAllowedBassNote(bs)) {
                    for (Note ld : notesHere.get(VoicePart.LEAD)) {
                        for (Note br : notesHere.get(VoicePart.BARI)) {
                            for (Note tn : notesHere.get(VoicePart.TENOR)) {
                                Note[] notes = new Note[]{tn, ld, br, bs};
                                int voicingScore = scoreVoicing(notes, chordHere);

                                if (voicingScore > Integer.MIN_VALUE) {
                                    Item it = new Item(notes);

                                    if (pos == 0) {
                                        // first timestep
                                        backpointersNext.getBackpointers().put(it, null); // null backpointer = leftmost cell
                                        bestScoresNext.put(it, voicingScore);
                                    } else {
                                        // later timesteps
                                        for (Map.Entry<Item, Integer> oldEntry : bestScores.entrySet()) {
                                            int voiceLeadingScore = scoreVoiceLeading(oldEntry.getKey().lastNotes, notes);
                                            int totalScore = voicingScore + voiceLeadingScore + oldEntry.getValue();

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

            time += score.getPart(VoicePart.LEAD).get(pos).getDuration();
            bestScores = bestScoresNext;
            backpointers = backpointersNext;
        }


        // construct best arrangement from best goal item
        if( bestScores.isEmpty() ) {
            return null;
        } else {
            List<Map.Entry<Item, Integer>> sortedEntries = new ArrayList<>(bestScores.entrySet());
            Collections.sort(sortedEntries, Comparator.comparing(Map.Entry::getValue));
            Map.Entry<Item, Integer> bestGoalItem = sortedEntries.get(sortedEntries.size() - 1);
            System.out.printf("Best arrangement has score %d.\n", bestGoalItem.getValue());

            Score bestArrangedScore = extractBestScore(bestGoalItem.getKey(), backpointers, score);

            return new Arrangement(bestArrangedScore, score, bestGoalItem.getValue(), System.nanoTime()-startTime);
        }
    }

    private Score extractBestScore(Item bestFinalItem, BackpointerColumn backpointers, Score originalScore) {
        List<Note[]> notes = new ArrayList<>();

        Item item = bestFinalItem;
        notes.add(item.lastNotes);

        while (backpointers != null) {
            List<Backpointer> backpointersHere = backpointers.getBackpointers().get(item);
            Collections.sort(backpointersHere, Comparator.comparing(Backpointer::getScore).reversed());
            Backpointer bp = backpointersHere.get(0); // best backpointer

            if (bp == null) {
                break;
            } else {
                item = bp.getPreviousItem();
                notes.add(item.lastNotes);

                backpointers = backpointers.getPrevious();
            }
        }

        Collections.reverse(notes);

        Score ret = originalScore.cloneWithoutNotes();
        ret.setComposer(config.getArranger());
        for (Note[] notesHere : notes) {
            for (int part = 0; part < 4; part++) {
                ret.addNote(part, notesHere[part]);
            }
        }

        return ret;
    }

    private int scoreVoiceLeading(Note[] from, Note[] to) {
        int score = 0;

        for (VoiceLeadingRule rule : VOICE_LEADING_RULES) {
            score += rule.score(from, to, config);
        }

        return score;
    }

    private int scoreVoicing(Note[] voicing, Chord chord) {
        int score = 0;

        for (VoicingRule rule : VOICING_RULES) {
            int ruleScore = rule.score(voicing, chord, config);

            // shortcut for disallowed voicings
            if (ruleScore == Integer.MIN_VALUE) {
                return Integer.MIN_VALUE;
            }

            score += rule.score(voicing, chord, config);
        }

        return score;
    }

    // possibleNotes[time][part] = list(possible notes for that part at that time)
    private List<List<List<Note>>> computePossibleNotes(Score score) {
        List<List<List<Note>>> ret = new ArrayList<>();

        score.foreachNoteAndChord(VoicePart.LEAD, (note, chord) -> {
            Set<Integer> chordNotes = chord.getNotes();
            List<List<Note>> notesAtTime = new ArrayList<>();
            ret.add(notesAtTime);

            for (int part = 0; part < 4; part++) {
                if (part == VoicePart.LEAD) {
                    List<Note> x = List.of(note);
                    notesAtTime.add(x);
                } else {
                    notesAtTime.add(getVoicePart(part).getNotesInRange(chordNotes, note.getDuration()));
                }
            }
        });

        return ret;
    }

    private VoicePart getVoicePart(int partId) {
        return config.getVoiceParts().get(partId);
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
                    lastNotes[VoicePart.TENOR], lastNotes[VoicePart.LEAD], lastNotes[VoicePart.BARI], lastNotes[VoicePart.BASS]);
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

    public static class Args {
        @Parameter(description = "Name of the input file (*.abc).", required = true)
        private String inputFilename = null;

        @Parameter(names = {"--output", "-o"}, description = "Name of the output file (*.abc).")
        private String outputFilename = "arranged.abc";

        @Parameter(names = {"--config", "-c"}, description = "Name of the configuration file (*.yaml).")
        private String configFilename = "aaba.yaml";

        @Parameter(names = "--help", description = "Display usage instructions.", help = true)
        private boolean help;

    }
}
