package edu.temple.UMB;

public class AWTMouseReplayEvent {
    String context;
    int x;
    int y;
    int button;
    AWTMouseReplayEvent(String context, int x, int y, int button) {
        this.context = context;
        this.x = x;
        this.y = y;
        this.button = button;
    }
}
