package notepack;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.*;

public class NotesCollection extends Vector<Note> {

    List<Note> startedNotes = new ArrayList<>();
    SortedSet<Note> notesInTheSameTick;
    long tick;
    long prevTick;
    Note prevNote;

    public NotesCollection() {
        super();
    }

    public NotesCollection(String fileName) {
        this(new File(fileName));
    }

    /**
     * Constructs a collection of notes based on a *.midi file, which contains tracks, each of which contains a bunch
     * of ShortMessage that contain information about a note.
     *
     * @param file *.midi file
     * @see Note
     */
    public NotesCollection(File file) {
        super();
        Sequencer sequencer;
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            Sequence sequence = MidiSystem.getSequence(file);

            Track[] tracks = sequence.getTracks();
            for (int trackNumber = 0; trackNumber < tracks.length; trackNumber++) {
                serveTrack(trackNumber, tracks);
            }

        } catch (MidiUnavailableException | InvalidMidiDataException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets every message from the track and serves it.
     */
    private void serveTrack(int trackNumber, Track[] tracks) throws InvalidMidiDataException {
        Track track = tracks[trackNumber];
        notesInTheSameTick = new TreeSet<>();
        prevTick = -1;
        for (int i = 0; i < track.size(); i++) {
            MidiEvent event = track.get(i);
            tick = event.getTick();
            serveMessage(trackNumber, event.getMessage());
        }
        if (!startedNotes.isEmpty())
            throw new InvalidMidiDataException("Not every note in track has ended");
    }

    /**
     * Depending on the type of the message it starts it, ends it or starts new tick.
     */
    private void serveMessage(int trackNumber, MidiMessage message) throws InvalidMidiDataException {
        if (message instanceof ShortMessage) {
            ShortMessage shortMessage = (ShortMessage) message;

            if (prevNote != null && tick != prevNote.getTick() && !notesInTheSameTick.isEmpty())
                newTick();

            switch (Note.getMessageType(shortMessage)) {
                case NOTE_START:
                    addNote(trackNumber, shortMessage);
                    break;
                case NOTE_STOP:
                    endNote(shortMessage);
                    break;
            }
        }
    }

    /**
     * Adds a note from the previous tick with the highest key value (last in the sorted set) and sets everything up
     * for the next available tick.
     */
    private void newTick() {
        Note noteToAdd = notesInTheSameTick.last();
        startedNotes.add(noteToAdd);
        this.add(noteToAdd);
        notesInTheSameTick.clear();
        prevTick = noteToAdd.getTick();
    }

    /**
     * Creates a note and adds it to the list of notes in current tick. It doesn't have to be added to NotesCollection.
     * Also sets distance from previous note of the first note to 0.
     *
     * @throws InvalidMidiDataException
     * @see NotesCollection#newTick()
     */
    private void addNote(int trackNumber, ShortMessage shortMessage) throws InvalidMidiDataException {
        long distanceFromPrev = prevTick == -1 ? 0 : tick - prevTick;
        Note thisNote = new Note(trackNumber, tick, shortMessage, distanceFromPrev);
        prevNote = thisNote;
        notesInTheSameTick.add(thisNote);
    }

    /**
     * Ends a note setting up its duration and removing it from startedNotes.
     *
     * @param endingMessage note-ending ShortMessage
     */
    private void endNote(ShortMessage endingMessage) {
        Note startingNote = findStartingNote(endingMessage);
        if (startingNote != null) {
            startingNote.setDuration(tick - startingNote.getTick());
            startedNotes.remove(startingNote);
        }
    }

    /**
     * Finds in startedNotes the note that is ought to be ended.
     *
     * @param endingMessage note-ending ShortMessage
     * @return starting note
     */
    public Note findStartingNote(ShortMessage endingMessage) {
        for (Note note : startedNotes) {
            if (note.getKey() == Note.getKey(endingMessage))
                return note;
        }
        return null;
    }

    @Override
    public synchronized List<Note> subList(int fromIndex, int toIndex) {
        NotesCollection ret = new NotesCollection();
        for (int i = fromIndex; i <= toIndex; i++) {
            ret.add(this.elementAt(i));
        }
        return ret;
    }
}
