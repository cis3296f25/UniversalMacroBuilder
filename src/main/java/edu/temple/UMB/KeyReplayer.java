package edu.temple.UMB;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Replays a sequence of translated keyboard {@link AWTReplayEvent}s at specific timestamps.
 * Uses a {@link ScheduledExecutorService} to schedule events and a {@link Robot} to emit key presses and releases.
 * Notes on timing and lifecycle:
 * Event delays are computed relative to when {@code start()} schedules tasks, based on each entry time in {@code awtEvents}.
 * This class does not block; callers are responsible for shutting down and awaiting the {@code scheduler} if needed.
 */
public class KeyReplayer {
    private static final Logger logger = LogManager.getLogger(KeyReplayer.class);
    private static final Map<Integer, Integer> jnativeToAwt = new HashMap<>();
    // ordered mapping of timestamps to AWTReplayEvents
    LinkedHashMap<Long, AWTReplayEvent> awtEvents = new LinkedHashMap<>();
    // the scheduler we will use to enable accurate playback. TODO: make this private and have this class auto terminate after last event
    public final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    // system time when replay was started
    // the (lazy instantiation) of the Robot class to be used for actual replay
    private final Robot robot;
    // track keys currently pressed
    private final Set<Integer> keysDown = ConcurrentHashMap.newKeySet();


    /**
     * Constructor for {@link KeyReplayer}.
     * @param loadedJNativeHookEvents The JNativeHook events returned by {@link Loader}.
     * @throws RuntimeException if the {@link Robot} cannot be created (e.g., if the environment does not support AWT operations).
     */
    public KeyReplayer(LinkedHashMap<Long, String> loadedJNativeHookEvents) throws RuntimeException {
        // translate those events to AWT events
        JNativeToAWT(loadedJNativeHookEvents);

        // debug print
        logger.info("Translated to {} AWT events", awtEvents.size());
        for (Long key : awtEvents.keySet()) {
            logger.debug("{} {} {}", key, awtEvents.get(key).context, awtEvents.get(key).event);
        }

        // initialize robot so now so we have minimal overhead later
        try {
            robot = new Robot();
            logger.info("Key replay started with {} events", awtEvents.size());
        } catch (AWTException e) {
            logger.fatal("Failed to initialize Robot for key replay", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Schedules and starts playback of specified event sequence. Schedules each event in {@code awtEvents} to execute
     * at the appropriate time relative to when playback begins. If an event's timestamp is in the past, it will be executed immediately.
     */
    public Long start() {
        long maxDelay = 0L;
        for (Long key : awtEvents.keySet()) {
            long delay = key;
            if (delay < 0) delay = 0;
            maxDelay = Math.max(delay, maxDelay);
            scheduler.schedule(() -> executeEvent(awtEvents.get(key)), delay, TimeUnit.MILLISECONDS);
        }
        scheduler.schedule(() -> logger.info("Replay finished!"), maxDelay + 50, TimeUnit.MILLISECONDS);
        scheduler.schedule(this::releaseAllHeld, maxDelay + 100, TimeUnit.MILLISECONDS);
        scheduler.schedule(scheduler::shutdown, maxDelay + 150, TimeUnit.MILLISECONDS);
        return maxDelay + 150;
    }

    /**
     * Executes a single {@link AWTReplayEvent}.
     * Depending on the {@code context} of the event ("PRESSED" or "RELEASED"),
     * this method will call {@link Robot#keyPress(int)} or {@link Robot#keyRelease(int)}.
     *
     * @param event the {@link AWTReplayEvent} to execute
     */
    private void executeEvent(AWTReplayEvent event) {
        logger.debug("Executing {} with code {}", event.context, event.event);
        if (event.context.equals("PRESSED")) {
            keysDown.add(event.event);
            robot.keyPress(event.event);
        } else if (event.context.equals("RELEASED")) {
            keysDown.remove(event.event);
            robot.keyRelease(event.event);
        }
    }

    public void releaseAllHeld() {
        for (Integer key : keysDown) {
            try {
                logger.warn("Key {} is still being held! Trying to release...", key);
                robot.keyRelease(key);
            } catch (Exception ignored) {}
        }
        keysDown.clear();
    }

    static {
        // Letters
        jnativeToAwt.put(NativeKeyEvent.VC_A, java.awt.event.KeyEvent.VK_A);
        jnativeToAwt.put(NativeKeyEvent.VC_B, java.awt.event.KeyEvent.VK_B);
        jnativeToAwt.put(NativeKeyEvent.VC_C, java.awt.event.KeyEvent.VK_C);
        jnativeToAwt.put(NativeKeyEvent.VC_D, java.awt.event.KeyEvent.VK_D);
        jnativeToAwt.put(NativeKeyEvent.VC_E, java.awt.event.KeyEvent.VK_E);
        jnativeToAwt.put(NativeKeyEvent.VC_F, java.awt.event.KeyEvent.VK_F);
        jnativeToAwt.put(NativeKeyEvent.VC_G, java.awt.event.KeyEvent.VK_G);
        jnativeToAwt.put(NativeKeyEvent.VC_H, java.awt.event.KeyEvent.VK_H);
        jnativeToAwt.put(NativeKeyEvent.VC_I, java.awt.event.KeyEvent.VK_I);
        jnativeToAwt.put(NativeKeyEvent.VC_J, java.awt.event.KeyEvent.VK_J);
        jnativeToAwt.put(NativeKeyEvent.VC_K, java.awt.event.KeyEvent.VK_K);
        jnativeToAwt.put(NativeKeyEvent.VC_L, java.awt.event.KeyEvent.VK_L);
        jnativeToAwt.put(NativeKeyEvent.VC_M, java.awt.event.KeyEvent.VK_M);
        jnativeToAwt.put(NativeKeyEvent.VC_N, java.awt.event.KeyEvent.VK_N);
        jnativeToAwt.put(NativeKeyEvent.VC_O, java.awt.event.KeyEvent.VK_O);
        jnativeToAwt.put(NativeKeyEvent.VC_P, java.awt.event.KeyEvent.VK_P);
        jnativeToAwt.put(NativeKeyEvent.VC_Q, java.awt.event.KeyEvent.VK_Q);
        jnativeToAwt.put(NativeKeyEvent.VC_R, java.awt.event.KeyEvent.VK_R);
        jnativeToAwt.put(NativeKeyEvent.VC_S, java.awt.event.KeyEvent.VK_S);
        jnativeToAwt.put(NativeKeyEvent.VC_T, java.awt.event.KeyEvent.VK_T);
        jnativeToAwt.put(NativeKeyEvent.VC_U, java.awt.event.KeyEvent.VK_U);
        jnativeToAwt.put(NativeKeyEvent.VC_V, java.awt.event.KeyEvent.VK_V);
        jnativeToAwt.put(NativeKeyEvent.VC_W, java.awt.event.KeyEvent.VK_W);
        jnativeToAwt.put(NativeKeyEvent.VC_X, java.awt.event.KeyEvent.VK_X);
        jnativeToAwt.put(NativeKeyEvent.VC_Y, java.awt.event.KeyEvent.VK_Y);
        jnativeToAwt.put(NativeKeyEvent.VC_Z, java.awt.event.KeyEvent.VK_Z);

        // Digits
        jnativeToAwt.put(NativeKeyEvent.VC_0, java.awt.event.KeyEvent.VK_0);
        jnativeToAwt.put(NativeKeyEvent.VC_1, java.awt.event.KeyEvent.VK_1);
        jnativeToAwt.put(NativeKeyEvent.VC_2, java.awt.event.KeyEvent.VK_2);
        jnativeToAwt.put(NativeKeyEvent.VC_3, java.awt.event.KeyEvent.VK_3);
        jnativeToAwt.put(NativeKeyEvent.VC_4, java.awt.event.KeyEvent.VK_4);
        jnativeToAwt.put(NativeKeyEvent.VC_5, java.awt.event.KeyEvent.VK_5);
        jnativeToAwt.put(NativeKeyEvent.VC_6, java.awt.event.KeyEvent.VK_6);
        jnativeToAwt.put(NativeKeyEvent.VC_7, java.awt.event.KeyEvent.VK_7);
        jnativeToAwt.put(NativeKeyEvent.VC_8, java.awt.event.KeyEvent.VK_8);
        jnativeToAwt.put(NativeKeyEvent.VC_9, java.awt.event.KeyEvent.VK_9);

        // Function keys
        for (int i = 1; i <= 24; i++) {
            try {
                int jn = NativeKeyEvent.class.getField("VC_F" + i).getInt(null);
                int awt = java.awt.event.KeyEvent.class.getField("VK_F" + i).getInt(null);
                jnativeToAwt.put(jn, awt);
            } catch (Exception ignored) {
            }
        }

        // Modifiers
        jnativeToAwt.put(NativeKeyEvent.VC_SHIFT, java.awt.event.KeyEvent.VK_SHIFT);
        jnativeToAwt.put(NativeKeyEvent.VC_CONTROL, java.awt.event.KeyEvent.VK_CONTROL);
        jnativeToAwt.put(NativeKeyEvent.VC_ALT, java.awt.event.KeyEvent.VK_ALT);
        jnativeToAwt.put(NativeKeyEvent.VC_META, java.awt.event.KeyEvent.VK_META);

        // Common punctuation
        jnativeToAwt.put(NativeKeyEvent.VC_SPACE, java.awt.event.KeyEvent.VK_SPACE);
        jnativeToAwt.put(NativeKeyEvent.VC_ENTER, java.awt.event.KeyEvent.VK_ENTER);
        jnativeToAwt.put(NativeKeyEvent.VC_TAB, java.awt.event.KeyEvent.VK_TAB);
        jnativeToAwt.put(NativeKeyEvent.VC_BACKSPACE, java.awt.event.KeyEvent.VK_BACK_SPACE);
        jnativeToAwt.put(NativeKeyEvent.VC_COMMA, java.awt.event.KeyEvent.VK_COMMA);
        jnativeToAwt.put(NativeKeyEvent.VC_PERIOD, java.awt.event.KeyEvent.VK_PERIOD);
        jnativeToAwt.put(NativeKeyEvent.VC_SLASH, java.awt.event.KeyEvent.VK_SLASH);
        jnativeToAwt.put(NativeKeyEvent.VC_SEMICOLON, java.awt.event.KeyEvent.VK_SEMICOLON);
        jnativeToAwt.put(NativeKeyEvent.VC_QUOTE, java.awt.event.KeyEvent.VK_QUOTE);
        jnativeToAwt.put(NativeKeyEvent.VC_OPEN_BRACKET, java.awt.event.KeyEvent.VK_OPEN_BRACKET);
        jnativeToAwt.put(NativeKeyEvent.VC_CLOSE_BRACKET, java.awt.event.KeyEvent.VK_CLOSE_BRACKET);
        jnativeToAwt.put(NativeKeyEvent.VC_BACK_SLASH, java.awt.event.KeyEvent.VK_BACK_SLASH);
        jnativeToAwt.put(NativeKeyEvent.VC_MINUS, java.awt.event.KeyEvent.VK_MINUS);
        jnativeToAwt.put(NativeKeyEvent.VC_EQUALS, java.awt.event.KeyEvent.VK_EQUALS);
        jnativeToAwt.put(NativeKeyEvent.VC_BACKQUOTE, java.awt.event.KeyEvent.VK_BACK_QUOTE);

        // Navigation
        jnativeToAwt.put(NativeKeyEvent.VC_UP, java.awt.event.KeyEvent.VK_UP);
        jnativeToAwt.put(NativeKeyEvent.VC_DOWN, java.awt.event.KeyEvent.VK_DOWN);
        jnativeToAwt.put(NativeKeyEvent.VC_LEFT, java.awt.event.KeyEvent.VK_LEFT);
        jnativeToAwt.put(NativeKeyEvent.VC_RIGHT, java.awt.event.KeyEvent.VK_RIGHT);
        jnativeToAwt.put(NativeKeyEvent.VC_HOME, java.awt.event.KeyEvent.VK_HOME);
        jnativeToAwt.put(NativeKeyEvent.VC_END, java.awt.event.KeyEvent.VK_END);
        jnativeToAwt.put(NativeKeyEvent.VC_PAGE_UP, java.awt.event.KeyEvent.VK_PAGE_UP);
        jnativeToAwt.put(NativeKeyEvent.VC_PAGE_DOWN, java.awt.event.KeyEvent.VK_PAGE_DOWN);
        jnativeToAwt.put(NativeKeyEvent.VC_INSERT, java.awt.event.KeyEvent.VK_INSERT);
        jnativeToAwt.put(NativeKeyEvent.VC_DELETE, java.awt.event.KeyEvent.VK_DELETE);
        jnativeToAwt.put(NativeKeyEvent.VC_ESCAPE, java.awt.event.KeyEvent.VK_ESCAPE);
    }


    /**
     * Translates recorded JNativeHook key events into AWT-compatible key events.
     * This method creates a mapping between {@link NativeKeyEvent} key codes and
     * {@link KeyEvent} constants, then converts each loaded event into an
     * {@link AWTReplayEvent}. Unsupported or unmapped keys are logged to the console.
     */
    private void JNativeToAWT(LinkedHashMap<Long, String> loadedJNativeHookEvents) {
        for (Long key : loadedJNativeHookEvents.keySet()) {
            String[] parts = loadedJNativeHookEvents.get(key).split("_");
            try {
                int code = Integer.parseInt(parts[1]);
                Integer awtCode = jnativeToAwt.get(code);
                if (awtCode == null) {
                    logger.warn("Unmapped key code encountered: {}", code);
                    continue; // skip unknown keys
                }

                AWTReplayEvent event = new AWTReplayEvent(parts[0], awtCode);
                awtEvents.put(key, event);
            } catch (Exception e) {
                logger.error("Failed to translate key: {}", parts.length > 1 ? parts[1] : "<unknown>", e);
            }
        }
    }
}


