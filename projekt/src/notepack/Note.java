package notepack;

import javax.sound.midi.ShortMessage;

public class Note implements Comparable {

    private static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "H"};

    public enum messageType {
        NOTE_START,
        NOTE_STOP,
        OTHER
    }

    private ShortMessage message;
    private int trackNumber;
    private long tick;
    private long duration = -1;             //duration of a note in ticks
    private long distanceFromPrevious = -1; //czas w tickach od startu poprzedniej

    public Note(int trackNumber, long tick, ShortMessage message, long distanceFromPrevious) {
        this.trackNumber = trackNumber;
        this.tick = tick;
        this.message = message;
        this.distanceFromPrevious = distanceFromPrevious;
    }

    @Override
    public String toString() {
        if (getType() == messageType.OTHER)
            return "Command:" + message.getCommand();

        String ret;
        ret = "Track " + trackNumber + ", tick " + tick + ", ";
        ret += "note " + (message.getCommand() == ShortMessage.NOTE_ON ? "on, " : "off, ");
        ret += getNoteName() + getOctave() + ", key " + getKey() + ", velocity " + getVelocity() + ", duration " +
                duration + ", distance " + distanceFromPrevious;
        return ret;
    }

    @Override
    public int compareTo(Object o) {
        Note note = (Note) o;
        if (getKey() == note.getKey()
                && duration == note.getDuration()
                && distanceFromPrevious == note.getDistanceFromPrevious())
            return 0;
        int keyComparison = getKey().compareTo(note.getKey());
        if (keyComparison != 0)
            return keyComparison;

        int durationComparison = getDuration().compareTo(note.getDuration());
        if (durationComparison != 0)
            return durationComparison;

        int distanceComparison = getDistanceFromPrevious().compareTo(note.getDistanceFromPrevious());
        if (distanceComparison != 0)
            return distanceComparison;

        return 1;

    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setDistanceFromPrevious(long distanceFromPrevious) {
        this.distanceFromPrevious = distanceFromPrevious;
    }

    public Long getDuration() {
        return duration;
    }

    public Integer getKey() {
        return getKey(message);
    }

    public int getVelocity() {
        return getVelocity(message);
    }

    public messageType getType() {
        return getMessageType(message);
    }

    public int getValue() {
        return getKey() % 12;
    }

    public int getOctave() {
        return (getKey() / 12) - 1;
    }

    public long getTick() {
        return tick;
    }

    public Long getDistanceFromPrevious() {
        return distanceFromPrevious;
    }

    public String getNoteName() {
        return NOTE_NAMES[getKey() % 12];
    }

    public static messageType getMessageType(ShortMessage message) {
        //note-starting message
        if (message.getCommand() == ShortMessage.NOTE_ON && getVelocity(message) > 0)
            return messageType.NOTE_START;

            //note-ending message
        else if (message.getCommand() == ShortMessage.NOTE_OFF || getVelocity(message) == 0)
            return messageType.NOTE_STOP;

            //neither - other command message
        else
            return messageType.OTHER;
    }

    public static int getKey(ShortMessage message) {
        return message.getData1();
    }

    public static int getVelocity(ShortMessage message) {
        return message.getData2();
    }

    public ShortMessage getMessage() {
        return this.message;
    }

}