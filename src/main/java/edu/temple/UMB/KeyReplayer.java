package edu.temple.UMB;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This {@code KeyReplayer} class is responsible for replaying a sequence of {@link AWTReplayEvent}s at designated timestamps.
 * It uses {@link ScheduledExecutorService} to schedule the events before they are executed, and each event is then
 * executed with {@link Robot}.
 */
public class KeyReplayer {
    // ordered mapping of timestamps to AWTReplayEvents
    LinkedHashMap<Long, AWTReplayEvent> awtEvents;
    // the scheduler we will use to enable accurate playback. TODO: make this private and have this class auto terminate after last event
    public final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    // system time when replay was started
    private final long startTime = System.currentTimeMillis();
    // the (lazy instantiation) of the Robot class to be used for actual replay
    private Robot robot;

    /**
     * Constructs an instance of {@code KeyReplayer} with the specified event sequence.
     * @param awtEvents a {@link LinkedHashMap} that contains an ordered mapping of timestamps from recording started to
     *                 the {@link AWTReplayEvent} to be replayed.
     */
    public KeyReplayer(LinkedHashMap<Long, AWTReplayEvent> awtEvents) {
        this.awtEvents = awtEvents;
    }

    /**
     * Schedules and starts playback of specified event sequence. Initializes a {@link Robot} instance, then schedules
     * each event in {@code awtEvents} to execute at the appropriate time relative to when playback begins.
     * If an event's timestamp is in the past, it will be executed immediately.
     * @throws RuntimeException if the {@link Robot} cannot be created (e.g., if the environment does not support AWT operations).
     */
    public void start() {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
        for (Long key : awtEvents.keySet()) {
            long delay = key - (System.currentTimeMillis() - startTime);
            if (delay < 0) delay = 0; // skip past events
            scheduler.schedule(() -> executeEvent(awtEvents.get(key)), delay, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Executes a single {@link AWTReplayEvent}.
     * Depending on the {@code context} of the event ("PRESSED" or "RELEASED"),
     * this method will call {@link Robot#keyPress(int)} or {@link Robot#keyRelease(int)}.
     * @param event the {@link AWTReplayEvent} to execute
     */
    private void executeEvent(AWTReplayEvent event) {
        System.out.println("Executing " + event.context + " with code " + event.event);
        if (event.context.equals("PRESSED")) {
            robot.keyPress(event.event);
        } else if (event.context.equals("RELEASED")) {
            robot.keyRelease(event.event);
        }
    }
}