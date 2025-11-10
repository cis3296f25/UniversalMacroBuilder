package edu.temple.UMB;


import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;

public class MouseEvent extends Event {
    private final NativeMouseEvent event;
    String context;

    public MouseEvent(long delta,  NativeMouseEvent event, String context) {
        super(delta);
        this.event = event;
        this.context = context;

    }

    public NativeMouseEvent getNativeMouseEvent() {return event;}

    @Override
    public String toString() { return getDelta() + " " + context + " " + event.getX() + "," + event.getY() + " " + event.getButton();}
}
