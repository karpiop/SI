package notepack;

import java.io.IOException;
import java.util.Random;
import java.util.Vector;
import javax.sound.midi.InvalidMidiDataException;

public class Main {

    private static final int PRED_SIZE = 200;
    private static final int MEMORY = 200;

    public static void main(String[] args) throws InvalidMidiDataException, IOException {
        String midiPath = "in.mid";
        String outPath = "out.mid";
        final NotesCollection notes = new NotesCollection(midiPath);
        notes.forEach(System.out::println);


        System.out.println("\n new  \n");
        Vector<Note> markovNotes = Markov.markov(notes, MEMORY, PRED_SIZE);

        Markov.saveAsMidi(markovNotes, midiPath, outPath);

        for (Note note : markovNotes) {
            System.out.println("key " + note.getKey() + " duration " + note.getDuration() + " " +
                    "distanceFromPrevious " + note.getDistanceFromPrevious());
        }
    }
}
