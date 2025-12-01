package edu.temple.UMB;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;

/**
 * Replays translated mouse events using AWT Robot.
 * Events are scheduled relative to the time start is called. The class translates
 * JNativeHook mouse records into AWT friendly events, schedules them on a single
 * thread, and drives the system cursor and buttons via Robot.
 */
public class MouseReplayer {
    private static final Logger logger = LogManager.getLogger(MouseReplayer.class);
    private static final Map<Integer, Integer> jnativeToAwtMouse = new HashMap<>();
    // ordered mapping of timestamps to AWTReplayEvents
    LinkedHashMap<Long, AWTReplayMouseEvent> awtMouseEvents = new LinkedHashMap<>();
    public final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
    private final CountDownLatch startLatch = new CountDownLatch(1);
    private volatile long startNano;
    // system time when replay was started
    // the (lazy instantiation) of the Robot class to be used for actual replay
    private final Robot robot;
    double scaleFactor = Toolkit.getDefaultToolkit().getScreenResolution() / 96.0;
    long maxDelay = 0L;

    /**
         * Creates a mouse replayer from raw JNativeHook mouse events.
         * Translates the input into AWT events, initializes a Robot, and schedules
         * execution of each event relative to the time start is called.
         * @param loadedJNativeHookEvents timestamp to raw event mapping as loaded by MouseLoader
         * @throws RuntimeException when Robot cannot be initialized
         */
        public MouseReplayer(LinkedHashMap<Long, String> loadedJNativeHookEvents) throws RuntimeException {
        // translate those events to AWT events
        JNativeToAWT(loadedJNativeHookEvents);

        // debug print
        logger.info("Translated to {} AWT events", awtMouseEvents.size());
        for (Long key : awtMouseEvents.keySet()) {
            logger.debug("{} {} {}", key, awtMouseEvents.get(key).context, awtMouseEvents.get(key).button);
        }

        // initialize robot so now so we have minimal overhead later
        try {
            GraphicsDevice defaultScreen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            robot = new Robot(defaultScreen);
            logger.info("Mouse replay started with {} events", awtMouseEvents.size());
        } catch (AWTException e) {
            logger.fatal("Failed to initialize Robot for mouse replay", e);
            throw new RuntimeException(e);
        }

        // schedule events now, on start just release latch
        for (Long key : awtMouseEvents.keySet()) {
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
                    executeEvent(awtMouseEvents.get(key));
                } catch (InterruptedException ignored) {
                }
            });
        }
        exec.submit(() -> logger.info("Mouse replay finished!"), maxDelay + 50);
        exec.submit(exec::shutdown, maxDelay + 150);
    }

    /**
     * Begins playback of all scheduled mouse events.
     * The schedule is evaluated relative to the instant this method is called.
     */
    public void start() {
        startNano = System.nanoTime(); // reference point for event delays
        startLatch.countDown(); // release latch
    }

/**
     * Executes a single translated mouse event.
     * Moves the cursor when needed and presses or releases buttons according to the event context.
     * @param event the mouse event to execute
     */
    private void executeEvent(AWTReplayMouseEvent event) {
        logger.debug("Executing {} with code {}", event.context, event.button);
        switch (event.context) {
            case "MOUSE_MOVED", "MOUSE_DRAGGED" ->
                    robot.mouseMove((int) (event.x / scaleFactor), (int) (event.y / scaleFactor));
            case "MOUSE_PRESSED" -> {
                robot.mouseMove((int) (event.x / scaleFactor), (int) (event.y / scaleFactor));
                robot.mousePress(event.button);
            }
            case "MOUSE_RELEASED" -> {
                robot.mouseMove((int) (event.x / scaleFactor), (int) (event.y / scaleFactor));
                robot.mouseRelease(event.button);
            }
        }
    }

    /**
     * Returns the maximum scheduled delay among mouse events.
     * This indicates approximately how long playback will take from start
     * until the last event is executed.
     * @return delay in milliseconds from start to the last scheduled event
     */
    public Long getMaxDelay() {
        return  maxDelay;
    }

    static{
        jnativeToAwtMouse.put(NativeMouseEvent.NOBUTTON, MouseEvent.NOBUTTON);
        jnativeToAwtMouse.put(NativeMouseEvent.BUTTON1, MouseEvent.BUTTON1_MASK);
        jnativeToAwtMouse.put(NativeMouseEvent.BUTTON2, MouseEvent.BUTTON3_MASK);
        jnativeToAwtMouse.put(NativeMouseEvent.BUTTON3, MouseEvent.BUTTON2_MASK);
    }

/**
     * Translates recorded JNativeHook mouse events into AWT-compatible events.
     * Each input entry is parsed into context, coordinates, and button value,
     * then converted into an {@link AWTReplayMouseEvent} for scheduling.
     * Unmapped codes are skipped with a warning.
     * @param loadedJNativeHookMouseEvents timestamp to raw event mapping as loaded by MouseLoader
     */
    private void JNativeToAWT(LinkedHashMap<Long, String> loadedJNativeHookMouseEvents) {
        for (Long key : loadedJNativeHookMouseEvents.keySet()) {
            String[] parts = loadedJNativeHookMouseEvents.get(key).split("-");
            try {
                int code = Integer.parseInt(parts[2]);
                Integer awtCode = jnativeToAwtMouse.get(code);
                if (awtCode == null) {
                    logger.warn("Unmapped key code encountered: {}", code);
                    continue; // skip unknown keys
                }
                int x = Integer.parseInt(parts[1].split(",", 2)[0]);
                int y = Integer.parseInt(parts[1].split(",", 2)[1]);

                AWTReplayMouseEvent event = new AWTReplayMouseEvent(parts[0], x, y, awtCode);
                awtMouseEvents.put(key, event);
            } catch (Exception e) {
                logger.error("Failed to translate key: {}", parts.length > 1 ? parts[2] : "<unknown>", e);
            }
        }
    }
}




