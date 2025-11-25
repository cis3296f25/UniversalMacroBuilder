package edu.temple.UMB;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

/**
 * Replays a sequence of translated keyboard {@link AWTReplayEvent} items at precise timestamps.
 * Backed by a single-threaded {@link ScheduledExecutorService} and a {@link Robot} for key emission.
 * Timing is computed relative to the instant {@link #start()} is invoked.
 * The executor schedules an automatic finish sequence: log completion, release any held keys, and shut down.
 * Callers typically invoke {@link #start()} and then, if needed, await termination on {@link #exec}.
 */
public class KeyReplayer {
    private static final Logger logger = LogManager.getLogger(KeyReplayer.class);
    private static final Map<Integer, Integer> jnativeToAwt = new HashMap<>();
    // ordered mapping of timestamps to AWTReplayEvents
    LinkedHashMap<Long, AWTReplayEvent> awtEvents = new LinkedHashMap<>();
    // the scheduler we will use to enable accurate playback. TODO: make this private and have this class auto terminate after last event
    public final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
    private final CountDownLatch startLatch = new CountDownLatch(1);
    private volatile long startNano;
    // system time when replay was started
    // the (lazy instantiation) of the Robot class to be used for actual replay
    private final Robot robot;
    // track keys currently pressed
    private final Set<Integer> keysDown = ConcurrentHashMap.newKeySet();
    long maxDelay = 0L;


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

        // schedule events now, on start just release latch
        for (Long key : awtEvents.keySet()) {
            long delay = key;
            if (delay < 0) delay = 0;
            maxDelay = Math.max(delay, maxDelay);
            exec.submit(() -> {
                try {
                    startLatch.await();
                    long target = startNano + TimeUnit.MILLISECONDS.toNanos(key);
                    long now;
                    while ((now = System.nanoTime()) < target) {
                        LockSupport.parkNanos(target - now);
                    }
                    executeEvent(awtEvents.get(key));
                } catch (InterruptedException ignored) {
                }
            });
        }
        exec.submit(() -> logger.info("Replay finished!"), maxDelay + 50);
        exec.submit(this::releaseAllHeld, maxDelay + 100);
        exec.submit(exec::shutdown, maxDelay + 150);
    }

    /**
     * Schedules and starts playback of the translated key events.
     * Each entry in {@code awtEvents} is executed relative to the instant this method is called.
     * If an entry's timestamp is already in the past, it will be executed immediately.
     *
     * @return the approximate number of milliseconds the caller should wait for all scheduled tasks to finish,
     *         including a small buffer to release any held keys and shut down the executor
     */
    public Long start() {
        startNano = System.nanoTime(); // reference point for event delays
        startLatch.countDown(); // release latch
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