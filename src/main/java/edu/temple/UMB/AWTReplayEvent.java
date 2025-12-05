package edu.temple.UMB;

/**
 * Immutable data holder for a translated AWT replay event.
 * The {@code context} should be either {@code "PRESSED"} or {@code "RELEASED"}, and {@code event} is the AWT {@code KeyEvent} code.
 */
public class AWTReplayEvent {
    String context;
    int event;

    /**
     * Creates an AWT replay event with the given context and key code.
     * @param context {@code PRESSED} or {@code RELEASED}
     * @param event AWT {@code KeyEvent} code
     */
    AWTReplayEvent(String context, int event) {
        this.context = context;
        this.event = event;
    }
}
