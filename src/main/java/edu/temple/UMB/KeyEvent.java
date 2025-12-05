package edu.temple.UMB;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

/**
 * Recorded keyboard event with its {@link NativeKeyEvent} and context.
 * The {@code context} is {@code PRESSED} or {@code RELEASED} and {@code delta} is milliseconds since recording started.
 */
public class KeyEvent extends Event {
    private final NativeKeyEvent event;
    String context;

    /**
     * Creates a recorded key event.
     * @param delta milliseconds since the first recorded event
     * @param event the underlying {@link NativeKeyEvent}
     * @param context {@code PRESSED} or {@code RELEASED}
     */
    public KeyEvent(long delta, NativeKeyEvent event, String context) {
        super(delta);
        this.event = event;
        this.context = context;
    }

    /**
     * Returns the underlying {@link NativeKeyEvent}.
     * @return the native key event
     */
    public NativeKeyEvent getEvent() {
        return event;
    }

    /**
     * Serializes this event for file output.
     * @return a line of the form {@code <delta> <context> <keyCode>}
     */
    @Override
    public String toString() {
        return getDelta() + " " + context + " " + event.getKeyCode();
    }
}