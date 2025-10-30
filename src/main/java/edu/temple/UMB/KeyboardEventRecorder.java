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
    private long now = System.currentTimeMillis();

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
        //long now = System.currentTimeMillis();
        if (firstEventTime == -1){
            firstEventTime = now;
        }
        long delta = now - firstEventTime;

        keyEvents.add(new KeyEvent(delta, e, "PRESSED"));

        // ✅ Print what the user types live in the terminal
        String keyText = NativeKeyEvent.getKeyText(e.getKeyCode());
        printKeyToTerminal("PRESSED: " + keyText);

        // Press ESC to stop recording
        if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
            try {
                System.out.println("\n[Recorder] ESC pressed — stopping...");
                GlobalScreen.unregisterNativeHook();
                recording = false;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override public void nativeKeyReleased(NativeKeyEvent e) {
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
                System.out.print(" ");
                break;
            case "Enter":
                System.out.println();
                break;
            case "Backspace":
                System.out.print("\b \b"); // erase last character
                break;
            case "Tab":
                System.out.print("\t");
                break;
            default:
                System.out.print(keyText);
        }
    }
}
