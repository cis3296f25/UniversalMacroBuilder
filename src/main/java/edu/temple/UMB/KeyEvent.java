package edu.temple.UMB;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

public class KeyEvent extends Event {
    private final NativeKeyEvent event;
    boolean pressedOrReleased;

    public KeyEvent(long delta, NativeKeyEvent event,  boolean pressedOrReleased) {
        super(delta);
        this.event = event;
        this.pressedOrReleased = pressedOrReleased;
    }

    public NativeKeyEvent getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return getDelta() + " KEY_PRESSED " + NativeKeyEvent.getKeyText(event.getKeyCode());
    }
}
