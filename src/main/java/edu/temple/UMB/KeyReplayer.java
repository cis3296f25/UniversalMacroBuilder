package edu.temple.UMB;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KeyReplayer {
    LinkedHashMap<Long, AWTReplayEvent> awtEvents;
    public final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final long startTime = System.currentTimeMillis();
    private Robot robot;


    public KeyReplayer(LinkedHashMap<Long, AWTReplayEvent> awtEvents) {
        this.awtEvents = awtEvents;
    }

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

    private void executeEvent(AWTReplayEvent event) {
        System.out.println("Executing " + event.context + " with code " + event.event);
        if (event.context.equals("PRESSED")) {
            robot.keyPress(event.event);
        } else if (event.context.equals("RELEASED")) {
            robot.keyRelease(event.event);
        }
    }
}


