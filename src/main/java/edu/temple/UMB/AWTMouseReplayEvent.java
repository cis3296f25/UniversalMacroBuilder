package edu.temple.UMB;

public class AWTMouseReplayEvent {
    String context;
    int event;
    int x;
    int y;
    AWTMouseReplayEvent(String context, int event, int x, int y) {
        this.context = context;
        this.event = event;
        this.x = x;
        this.y = y;
    }
}
