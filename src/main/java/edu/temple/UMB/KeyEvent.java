package edu.temple.UMB;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

public class KeyEvent extends Event {
    private final NativeKeyEvent event;
    String context;

    public KeyEvent(long delta, NativeKeyEvent event, String context) {
        super(delta);
        this.event = event;
        this.context = context;
    }

    public NativeKeyEvent getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return getDelta() + " " + context + " " + NativeKeyEvent.getKeyText(event.getKeyCode());
    }
}