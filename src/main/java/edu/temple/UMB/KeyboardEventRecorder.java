package edu.temple.UMB;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.util.ArrayList;
import java.util.List;

public class KeyboardEventRecorder implements NativeKeyListener {
    private final List<KeyEvent> keyEvents = new ArrayList<>();
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
        GlobalScreen.addNativeKeyListener(this);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        long now = System.currentTimeMillis();
        if (firstEventTime == -1){
            firstEventTime = now;
        }
        // Press ESC to stop recording (before adding it to array)
        if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
            try {
                System.out.println("\n[Recorder] ESC pressed — stopping...");
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

    public List<KeyEvent> getEvents() {
        return keyEvents;
    }

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
}
