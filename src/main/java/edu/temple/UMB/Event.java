package edu.temple.UMB;

/**
 * Base type for recorded input events.
 * Holds the elapsed time offset {@code delta} in milliseconds from the start of recording.
 */
public class Event {
    private final long delta;

    /**
     * Creates a new event with the given time offset.
     * @param delta milliseconds since the first recorded event
     */
    public Event(long delta) {
        this.delta = delta;
    }

    /**
     * Returns the time offset of this event in milliseconds.
     * @return the {@code delta} value
     */
    public long getDelta() {
        return delta;
    }
}
