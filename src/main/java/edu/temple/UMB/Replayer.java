package edu.temple.UMB;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;


/**
 * The {@code Replayer} class loads, translates, and replays recorded input events (currently keyboard events, with mouse support planned for future versions).
 */
public class Replayer {
    private File inFile;
    private LinkedHashMap<Long, String> loadedJNativeHookEvents = new LinkedHashMap<>();
    private static Map<Integer, Integer> jnativeToAwt = new HashMap<>();
    private LinkedHashMap<Long, AWTReplayEvent> AWTEvents = new LinkedHashMap<>();
    Loader l;
    KeyReplayer kr;

    /**
     * Constructs a new {@code Replayer} from the given file path.
     * This constructor immediately loads and translates recorded keyboard events
     * from the file, then initiates replay using {@link KeyReplayer}.
     * @param inPath the path to the input file containing recorded JNativeHook events.
     */
    public Replayer(String inPath) {
        this.inFile = new File(inPath);
        l = new Loader(inFile);

        // load events from file
        try {
            loadedJNativeHookEvents = l.loadJNativeEventsFromFile();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // translate those events to AWT events
        JNativeToAWT();

        // debug print
        System.out.println("Loaded JNativeHook events from file and translated to AWTEvents (below)");
        for (Long key : AWTEvents.keySet()) {
            System.out.println(key + " " + AWTEvents.get(key).context + " " + AWTEvents.get(key).event);
        }

        // instantiate the KeyReplayer and replay events
        kr = new KeyReplayer(AWTEvents);
        kr.start();
        kr.scheduler.shutdown();

        // wait for KeyReplayer thread to exit
        try {
            kr.scheduler.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        // Letters
        jnativeToAwt.put(NativeKeyEvent.VC_A, KeyEvent.VK_A);
        jnativeToAwt.put(NativeKeyEvent.VC_B, KeyEvent.VK_B);
        jnativeToAwt.put(NativeKeyEvent.VC_C, KeyEvent.VK_C);
        jnativeToAwt.put(NativeKeyEvent.VC_D, KeyEvent.VK_D);
        jnativeToAwt.put(NativeKeyEvent.VC_E, KeyEvent.VK_E);
        jnativeToAwt.put(NativeKeyEvent.VC_F, KeyEvent.VK_F);
        jnativeToAwt.put(NativeKeyEvent.VC_G, KeyEvent.VK_G);
        jnativeToAwt.put(NativeKeyEvent.VC_H, KeyEvent.VK_H);
        jnativeToAwt.put(NativeKeyEvent.VC_I, KeyEvent.VK_I);
        jnativeToAwt.put(NativeKeyEvent.VC_J, KeyEvent.VK_J);
        jnativeToAwt.put(NativeKeyEvent.VC_K, KeyEvent.VK_K);
        jnativeToAwt.put(NativeKeyEvent.VC_L, KeyEvent.VK_L);
        jnativeToAwt.put(NativeKeyEvent.VC_M, KeyEvent.VK_M);
        jnativeToAwt.put(NativeKeyEvent.VC_N, KeyEvent.VK_N);
        jnativeToAwt.put(NativeKeyEvent.VC_O, KeyEvent.VK_O);
        jnativeToAwt.put(NativeKeyEvent.VC_P, KeyEvent.VK_P);
        jnativeToAwt.put(NativeKeyEvent.VC_Q, KeyEvent.VK_Q);
        jnativeToAwt.put(NativeKeyEvent.VC_R, KeyEvent.VK_R);
        jnativeToAwt.put(NativeKeyEvent.VC_S, KeyEvent.VK_S);
        jnativeToAwt.put(NativeKeyEvent.VC_T, KeyEvent.VK_T);
        jnativeToAwt.put(NativeKeyEvent.VC_U, KeyEvent.VK_U);
        jnativeToAwt.put(NativeKeyEvent.VC_V, KeyEvent.VK_V);
        jnativeToAwt.put(NativeKeyEvent.VC_W, KeyEvent.VK_W);
        jnativeToAwt.put(NativeKeyEvent.VC_X, KeyEvent.VK_X);
        jnativeToAwt.put(NativeKeyEvent.VC_Y, KeyEvent.VK_Y);
        jnativeToAwt.put(NativeKeyEvent.VC_Z, KeyEvent.VK_Z);

        // Digits
        jnativeToAwt.put(NativeKeyEvent.VC_0, KeyEvent.VK_0);
        jnativeToAwt.put(NativeKeyEvent.VC_1, KeyEvent.VK_1);
        jnativeToAwt.put(NativeKeyEvent.VC_2, KeyEvent.VK_2);
        jnativeToAwt.put(NativeKeyEvent.VC_3, KeyEvent.VK_3);
        jnativeToAwt.put(NativeKeyEvent.VC_4, KeyEvent.VK_4);
        jnativeToAwt.put(NativeKeyEvent.VC_5, KeyEvent.VK_5);
        jnativeToAwt.put(NativeKeyEvent.VC_6, KeyEvent.VK_6);
        jnativeToAwt.put(NativeKeyEvent.VC_7, KeyEvent.VK_7);
        jnativeToAwt.put(NativeKeyEvent.VC_8, KeyEvent.VK_8);
        jnativeToAwt.put(NativeKeyEvent.VC_9, KeyEvent.VK_9);

        // Function keys
        for (int i = 1; i <= 24; i++) {
            try {
                int jn = NativeKeyEvent.class.getField("VC_F" + i).getInt(null);
                int awt = KeyEvent.class.getField("VK_F" + i).getInt(null);
                jnativeToAwt.put(jn, awt);
            } catch (Exception ignored) {
            }
        }

        // Modifiers
        jnativeToAwt.put(NativeKeyEvent.VC_SHIFT, KeyEvent.VK_SHIFT);
        jnativeToAwt.put(NativeKeyEvent.VC_CONTROL, KeyEvent.VK_CONTROL);
        jnativeToAwt.put(NativeKeyEvent.VC_ALT, KeyEvent.VK_ALT);
        jnativeToAwt.put(NativeKeyEvent.VC_META, KeyEvent.VK_META);

        // Common punctuation
        jnativeToAwt.put(NativeKeyEvent.VC_SPACE, KeyEvent.VK_SPACE);
        jnativeToAwt.put(NativeKeyEvent.VC_ENTER, KeyEvent.VK_ENTER);
        jnativeToAwt.put(NativeKeyEvent.VC_TAB, KeyEvent.VK_TAB);
        jnativeToAwt.put(NativeKeyEvent.VC_BACKSPACE, KeyEvent.VK_BACK_SPACE);
        jnativeToAwt.put(NativeKeyEvent.VC_COMMA, KeyEvent.VK_COMMA);
        jnativeToAwt.put(NativeKeyEvent.VC_PERIOD, KeyEvent.VK_PERIOD);
        jnativeToAwt.put(NativeKeyEvent.VC_SLASH, KeyEvent.VK_SLASH);
        jnativeToAwt.put(NativeKeyEvent.VC_SEMICOLON, KeyEvent.VK_SEMICOLON);
        jnativeToAwt.put(NativeKeyEvent.VC_QUOTE, KeyEvent.VK_QUOTE);
        jnativeToAwt.put(NativeKeyEvent.VC_OPEN_BRACKET, KeyEvent.VK_OPEN_BRACKET);
        jnativeToAwt.put(NativeKeyEvent.VC_CLOSE_BRACKET, KeyEvent.VK_CLOSE_BRACKET);
        jnativeToAwt.put(NativeKeyEvent.VC_BACK_SLASH, KeyEvent.VK_BACK_SLASH);
        jnativeToAwt.put(NativeKeyEvent.VC_MINUS, KeyEvent.VK_MINUS);
        jnativeToAwt.put(NativeKeyEvent.VC_EQUALS, KeyEvent.VK_EQUALS);
        jnativeToAwt.put(NativeKeyEvent.VC_BACKQUOTE, KeyEvent.VK_BACK_QUOTE);

        // Navigation
        jnativeToAwt.put(NativeKeyEvent.VC_UP, KeyEvent.VK_UP);
        jnativeToAwt.put(NativeKeyEvent.VC_DOWN, KeyEvent.VK_DOWN);
        jnativeToAwt.put(NativeKeyEvent.VC_LEFT, KeyEvent.VK_LEFT);
        jnativeToAwt.put(NativeKeyEvent.VC_RIGHT, KeyEvent.VK_RIGHT);
        jnativeToAwt.put(NativeKeyEvent.VC_HOME, KeyEvent.VK_HOME);
        jnativeToAwt.put(NativeKeyEvent.VC_END, KeyEvent.VK_END);
        jnativeToAwt.put(NativeKeyEvent.VC_PAGE_UP, KeyEvent.VK_PAGE_UP);
        jnativeToAwt.put(NativeKeyEvent.VC_PAGE_DOWN, KeyEvent.VK_PAGE_DOWN);
        jnativeToAwt.put(NativeKeyEvent.VC_INSERT, KeyEvent.VK_INSERT);
        jnativeToAwt.put(NativeKeyEvent.VC_DELETE, KeyEvent.VK_DELETE);
        jnativeToAwt.put(NativeKeyEvent.VC_ESCAPE, KeyEvent.VK_ESCAPE);
    }


    /**
     * Translates recorded JNativeHook key events into AWT-compatible key events.
     * This method creates a mapping between {@link NativeKeyEvent} key codes and
     * {@link KeyEvent} constants, then converts each loaded event into an
     * {@link AWTReplayEvent}. Unsupported or unmapped keys are logged to the console.
     */
    private void JNativeToAWT() {
        for (Long key : loadedJNativeHookEvents.keySet()) {
            String[] parts = loadedJNativeHookEvents.get(key).split("_");
            try {
                int code = Integer.parseInt(parts[1]);
                Integer awtCode = jnativeToAwt.get(code);
                if (awtCode == null) {
                    System.out.println("Key not found: " + code);
                    continue; // skip unknown keys
                }

                AWTReplayEvent event = new AWTReplayEvent(parts[0], awtCode);
                AWTEvents.put(key, event);
            } catch (Exception e) {
                System.out.println("Key not found: " + parts[1]);
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}