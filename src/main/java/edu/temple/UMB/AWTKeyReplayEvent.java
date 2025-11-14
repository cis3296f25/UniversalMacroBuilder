package edu.temple.UMB;

/**
 * This class contains a context (which should only be "PRESSED" or "RELEASED") and the corresponding AWTEvent code for that action.
 */
public class AWTKeyReplayEvent {
    String context;
    int event;
    AWTKeyReplayEvent(String context, int event) {
        this.context = context;
        this.event = event;
    }
}
