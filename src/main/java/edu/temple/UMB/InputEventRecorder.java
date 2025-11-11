package edu.temple.UMB;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;

import java.lang.annotation.Native;
import java.security.Key;    
import java.util.ArrayList;
import java.util.List;

public class InputEventRecorder implements NativeKeyListener, NativeMouseInputListener {
    private final List<KeyEvent> keyEvents = new ArrayList<>();
    private final List<MouseEvent> mouseEvents = new ArrayList<>();
    private boolean recording = true;
    private long firstEventTime = -1;
    private final int stopKeyCode;
    
    // Constructor that takes the stop key name as a parameter
    public InputEventRecorder(String stopKeyName) {
        this.stopKeyCode = keyTextToJNative(stopKeyName);
    }


    // Helper method to convert the stop key to JNativeHook key code
    private int keyTextToJNative(String keyText) {
        try {
            return NativeKeyEvent.class.getField("VC_" + keyText.toUpperCase()).getInt(null);
        } catch (Exception e) {
            System.out.println("Stop key codes are inputted as strings and resolved according to https://javadoc.io/static/com.1stleg/jnativehook/2.0.3/constant-values.html#org.jnativehook.keyboard.NativeKeyEvent.VC_N. For example, an input of NUM_LOCK will properly resolve to VC_NUM_LOCK, whereas NUMLOCK will fail and default to VC_ESCAPE.");
            return NativeKeyEvent.VC_ESCAPE;
        }
    }

    public void startRecording() throws Exception {
        // Turn off JNativeHook's internal logging to keep the console clean
        java.util.logging.Logger logger =
                java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(java.util.logging.Level.OFF);
        logger.setUseParentHandlers(false);

        // Register the global key hook
        GlobalScreen.registerNativeHook();
        GlobalScreen.addNativeKeyListener(this);
        GlobalScreen.addNativeMouseListener(this);
        GlobalScreen.addNativeMouseMotionListener(this);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        long now = System.currentTimeMillis();
        if (firstEventTime == -1){
            firstEventTime = now;
        }
        // Press ESC to stop recording (before adding it to array)
        if (e.getKeyCode() == stopKeyCode) {
            try {
                System.out.println("\n[Recorder] stop key pressed — stopping...");
                GlobalScreen.unregisterNativeHook();
                recording = false;
                return;
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        }

        long delta = now - firstEventTime;

        keyEvents.add(new KeyEvent(delta, e, "PRESSED"));

        // ✅ Print what the user types live in the terminal
        String keyText = NativeKeyEvent.getKeyText(e.getKeyCode());
        printKeyToTerminal("PRESSED: " + keyText);


    }

    @Override public void nativeKeyReleased(NativeKeyEvent e) {
        long now = System.currentTimeMillis();
        if (firstEventTime == -1){
            firstEventTime = now;
            if (e.getKeyCode() == NativeKeyEvent.VC_ENTER) {
                // we actually just skip this here because it will almost always be the user releasing the enter key
                return;
            }
        }
        long delta = now - firstEventTime;

        keyEvents.add(new KeyEvent(delta, e, "RELEASED"));

        String keyText = NativeKeyEvent.getKeyText(e.getKeyCode());
        printKeyToTerminal("RELEASED: " + keyText);
    }

    @Override public void nativeKeyTyped(NativeKeyEvent e) {}

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
        printMouseEventToTerminal("MOUSE PRESSED: " + eventText);


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
        printMouseEventToTerminal("MOUSE RELEASED: " + eventText);
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
        printMouseEventToTerminal("MOUSE DRAGGED: " + eventText);
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
        printMouseEventToTerminal("MOUSE MOVED: " + eventText);
    }


    public List<KeyEvent> getKeyEvents() { return keyEvents; }

    public List<MouseEvent> getMouseEvents() { return mouseEvents; }

    public boolean isRecording() {
        return recording;
    }

    // Helper function to make terminal output look like typing
    private void printKeyToTerminal(String keyText) {
        switch (keyText) {
            case "Space":
                System.out.println(" ");
                break;
            case "Enter":
                System.out.println();
                break;
            case "Backspace":
                System.out.println("\b \b"); // erase last character
                break;
            case "Tab":
                System.out.println("\t");
                break;
            default:
                System.out.println(keyText);
        }
    }

    private void printMouseEventToTerminal(String eventText) {
        System.out.println(eventText);
    }
}
