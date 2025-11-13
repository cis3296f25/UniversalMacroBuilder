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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static java.lang.Thread.sleep;

public class InputEventRecorder implements NativeKeyListener, NativeMouseInputListener {
    private static final Logger logger = LogManager.getLogger(InputEventRecorder.class);
    private final List<KeyEvent> keyEvents = new ArrayList<>();
    private final List<MouseEvent> mouseEvents = new ArrayList<>();
    private boolean recording;
    private long firstEventTime = -1;
    private final int stopKeyCode;
    
    // Constructor that takes the stop key name as a parameter
    public InputEventRecorder(String stopKeyName) {
        this.stopKeyCode = keyTextToJNative(stopKeyName);
    }


    // Helper method to convert the stop key to JNativeHook key code
    private int keyTextToJNative(String keyText) {
        // Turn off JNativeHook's internal logging to keep the console clean
        java.util.logging.Logger jnhLogger =
                java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
        jnhLogger.setLevel(java.util.logging.Level.OFF);
        jnhLogger.setUseParentHandlers(false);

        try {
            return NativeKeyEvent.class.getField("VC_" + keyText.toUpperCase()).getInt(null);
        } catch (Exception e) {
            logger.warn("Invalid stop key '{}'. Defaulting to ESCAPE. See JNativeHook constants for valid names.", keyText, e);
            System.out.println("Stop key codes are inputted as strings and resolved according to https://javadoc.io/static/com.1stleg/jnativehook/2.0.3/constant-values.html#org.jnativehook.keyboard.NativeKeyEvent.VC_N. For example, an input of NUM_LOCK will properly resolve to VC_NUM_LOCK, whereas NUMLOCK will fail and default to VC_ESCAPE.");
            return NativeKeyEvent.VC_ESCAPE;
        }
    }

    public void startRecording() throws Exception {
        logger.info("Registering native hooks and starting input recording");
        GlobalScreen.registerNativeHook();
        GlobalScreen.addNativeKeyListener(this);
        GlobalScreen.addNativeMouseListener(this);
        GlobalScreen.addNativeMouseMotionListener(this);
        recording = true;
        firstEventTime = System.currentTimeMillis();
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        // Press ESC to stop recording (before adding it to array)
        if (e.getKeyCode() == stopKeyCode) {
            try {
                System.out.println("\n[Recorder] stop key pressed — stopping...");
                logger.info("Stop key pressed. Unregistering native hook and stopping recording.");
                GlobalScreen.unregisterNativeHook();
                recording = false;
                return;
            } catch (Exception ex) {
                logger.error("Failed to unregister native hook during stop", ex);
                return;
            }
        }

        long delta = System.currentTimeMillis() - firstEventTime;

        keyEvents.add(new KeyEvent(delta, e, "PRESSED"));

        // Print what the user types live in the terminal
        String keyText = NativeKeyEvent.getKeyText(e.getKeyCode());
        printKeyToTerminal("PRESSED: " + keyText);


    }

    @Override public void nativeKeyReleased(NativeKeyEvent e) {

        long delta = System.currentTimeMillis() - firstEventTime;
        if (e.getKeyCode() == NativeKeyEvent.VC_ENTER && delta > 10) {
            // assume this is the first enter key release due to keycode and timestamp
            return;
        }

        keyEvents.add(new KeyEvent(delta, e, "RELEASED"));

        String keyText = NativeKeyEvent.getKeyText(e.getKeyCode());
        printKeyToTerminal("RELEASED: " + keyText);
    }

    @Override public void nativeKeyTyped(NativeKeyEvent e) {}

    @Override
    public void nativeMousePressed(NativeMouseEvent e) {

        long delta = System.currentTimeMillis() - firstEventTime;

        mouseEvents.add(new MouseEvent(delta, e, "MOUSE PRESSED"));

        // Print what the user types live in the terminal
        String eventText = e.paramString();
        printMouseEventToTerminal("MOUSE PRESSED: " + eventText);
    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent e) {
        long delta = System.currentTimeMillis() - firstEventTime;

        mouseEvents.add(new MouseEvent(delta, e, "MOUSE RELEASED"));

        String eventText = e.paramString();
        printMouseEventToTerminal("MOUSE RELEASED: " + eventText);
    }

    @Override public void nativeMouseClicked(NativeMouseEvent e) {}

    @Override
    public void nativeMouseDragged(NativeMouseEvent e) {
        long delta = System.currentTimeMillis() - firstEventTime;
        mouseEvents.add(new MouseEvent(delta, e, "MOUSE DRAGGED"));
        String eventText = e.paramString();
        printMouseEventToTerminal("MOUSE DRAGGED: " + eventText);
    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent e) {
        long delta = System.currentTimeMillis() - firstEventTime;

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
