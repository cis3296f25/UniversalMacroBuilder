package edu.temple.UMB;

import com.github.kwhat.jnativehook.GlobalScreen;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;

import java.util.ArrayList;
import java.util.List;

public class MouseEventRecorder implements NativeMouseInputListener {
    private final List<MouseEvent> mouseEvents = new ArrayList<>();
    private boolean recording = true;
    private long firstEventTime = -1;

    public void startRecording() throws Exception {
        // Turn off JNativeHook's internal logging to keep the console clean
        java.util.logging.Logger logger =
                java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(java.util.logging.Level.OFF);
        logger.setUseParentHandlers(false);

        // Register the global key hook
        GlobalScreen.registerNativeHook();
        GlobalScreen.addNativeMouseListener(this);
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent e) {
        long now = System.currentTimeMillis();
        if (firstEventTime == -1){
            firstEventTime = now;
        }
        // Press ESC to stop recording (before adding it to array)


        long delta = now - firstEventTime;

        mouseEvents.add(new MouseEvent(delta, e, "MOUSE PRESSED"));

        // ✅ Print what the user types live in the terminal
        String eventText = e.paramString();
        printEventToTerminal("MOUSE PRESSED: " + eventText);


    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent e) {
        long now = System.currentTimeMillis();
        if (firstEventTime == -1){
            firstEventTime = now;
        }
        long delta = now - firstEventTime;

        mouseEvents.add(new MouseEvent(delta, e, "MOUSE RELEASED"));

        String eventText = e.paramString();
        printEventToTerminal("MOUSE RELEASED: " + eventText);
    }

    @Override public void nativeMouseClicked(NativeMouseEvent e) {}

    @Override
    public void nativeMouseDragged(NativeMouseEvent e) {
        long now = System.currentTimeMillis();
        if (firstEventTime == -1){
            firstEventTime = now;
        }
        long delta = now - firstEventTime;

        mouseEvents.add(new MouseEvent(delta, e, "MOUSE DRAGGED"));
        String eventText = e.paramString();
        printEventToTerminal("MOUSE DRAGGED: " + eventText);
    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent e) {
        long now = System.currentTimeMillis();
        if (firstEventTime == -1){
            firstEventTime = now;
        }
        long delta = now - firstEventTime;

        mouseEvents.add(new MouseEvent(delta, e, "MOUSE MOVED"));
        String eventText = e.paramString();
        printEventToTerminal("MOUSE MOVED: " + eventText);
    }

    public List<MouseEvent> getEvents() {
        return mouseEvents;
    }

    public boolean isRecording() {
        return recording;
    }

    // Helper function to make terminal output look like typing
    private void printEventToTerminal(String eventText) {
        System.out.println(eventText);
    }
}
