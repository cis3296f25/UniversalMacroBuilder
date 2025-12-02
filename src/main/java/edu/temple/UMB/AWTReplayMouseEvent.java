package edu.temple.UMB;

public class AWTReplayMouseEvent {
    String context;
    int x;
    int y;
    int button;

    AWTReplayMouseEvent(String context, int x, int y, int button) {
        this.context = context;
        this.x = x;
        this.y = y;
        this.button = button;
    }
}
