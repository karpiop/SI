package notepack;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

public class Markov {

    private static final Random rand = new Random();
    private static Matrix matrix;


    /**
     * Predict new element in set based on >memory< number of previous elements.
     * Each element has a chance to be chosen proportional to the number of times that it occurred in the entry set
     * after elements in prev list in matrix, which means that only elements that occurred at least one time can be
     * chosen.
     */
    private static Note nextPrediction() {
        Note nextObj = null;
        int sum = 0;
        int nextElementRand = Math.abs(rand.nextInt()) % matrix.getCounterSum();
        int counterListSize = matrix.getElement().getCounterList().size();
        for (int i = 0; i < counterListSize; i++) {
            if (sum <= nextElementRand && matrix.getElement().getCounter(i) != 0)
                nextObj = matrix.getElement().get(i);
            sum += matrix.getElement().getCounter(i);
        }

        if (nextObj == null)
            throw new IllegalStateException("Couldn't find next value");

        return nextObj;
    }

    /**
     * Predicts new values based on MEMORY number of previous elements.
     * Starts prediction from the beginning - starting collection has only null elements.
     * If a collection ends - it loops back to the beginning.
     *
     * @param notes     entry set of elements
     * @param MEMORY    number of previous elements that need to be taken into account
     * @param PRED_SIZE number of elements to be predicted
     * @return collection of new values
     * @see Matrix#memory
     */
    public static Vector<Note> markov(NotesCollection notes, int MEMORY, int PRED_SIZE) {
        //creating matrix and setting up counters by adding notes to it
        matrix = Matrix.create(MEMORY);
        notes.forEach(matrix::add);
        for (int i = 0; i < MEMORY; i++) {
            matrix.add(null);
        }


        //prediction part
        Vector<Note> ret = new Vector<>(PRED_SIZE);
        matrix.clearPrev();

        for (int i = 0; i < PRED_SIZE; i++) {
            Note nextPrediction = nextPrediction();
            matrix.updatePrevWith(nextPrediction);
            ret.add(nextPrediction);
        }
        return ret;
    }

    /**
     * Saves predicted notes to a file.
     *
     * @param markovNotes list of nodes that were predicted with Markov chain
     * @param inFile      file, which was the base for prediction, it contains base sequence to set up a new one
     * @param outFile     file, which the notes will be saved to
     * @throws InvalidMidiDataException
     * @throws IOException
     */
    public static void saveAsMidi(Vector<Note> markovNotes, File inFile, File outFile) throws InvalidMidiDataException, IOException {
        Sequence baseSequence = MidiSystem.getSequence(inFile);
        Sequence in = new Sequence(baseSequence.getDivisionType(), baseSequence.getResolution(), 1);
        MidiEvent event;
        long tick = 0;
        for (Note note : markovNotes) {
            tick += note.getDistanceFromPrevious();

            event = new MidiEvent(note.getMessage(), tick);
            in.getTracks()[0].add(event);

            ShortMessage mess = note.getMessage();
            ShortMessage endingMessage = new ShortMessage(mess.getStatus(), mess.getData1(), 0);
            event = new MidiEvent(endingMessage, tick + note.getDuration());
            in.getTracks()[0].add(event);
        }
        MidiSystem.write(in, MidiSystem.getMidiFileTypes(in)[0], outFile);
    }

    public static void saveAsMidi(Vector<Note> markovNotes, String inPath, String outPath) throws InvalidMidiDataException, IOException {
        saveAsMidi(markovNotes, new File(inPath), new File(outPath));
    }

}
