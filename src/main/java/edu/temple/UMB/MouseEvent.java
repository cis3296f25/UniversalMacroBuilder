package edu.temple.UMB;

import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;

/**
 * Recorded mouse event with its {@link NativeMouseEvent} and context.
 * The {@code context} describes the action such as {@code MOUSE PRESSED}, {@code MOUSE RELEASED}, {@code MOUSE MOVED}, or {@code MOUSE DRAGGED}.
 */
public class MouseEvent extends Event {
    private final NativeMouseEvent event;
    String context;

    /**
     * Creates a recorded mouse event.
     * @param delta milliseconds since the first recorded event
     * @param event the underlying {@link NativeMouseEvent}
     * @param context a short description of the mouse action
     */
    public MouseEvent(long delta,  NativeMouseEvent event, String context) {
        super(delta);
        this.event = event;
        this.context = context;

    }

    /**
     * Returns the underlying {@link NativeMouseEvent}.
     * @return the native mouse event
     */
    public NativeMouseEvent getNativeMouseEvent() {return event;}

    /**
     * Serializes this event for file output.
     * @return a line of the form {@code <delta> <context> <x>,<y> <button>}
     */
    @Override
    public String toString() { return getDelta() + " " + context + " " + event.getX() + "," + event.getY() + " " + event.getButton();}
}
