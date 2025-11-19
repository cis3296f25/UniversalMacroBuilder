package edu.temple.UMB;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;

public class MouseReplayer {
    private static final Logger logger = LogManager.getLogger(MouseReplayer.class);
    private static Map<Integer, Integer> jnativeToAwtMouse = new HashMap<>();
    // ordered mapping of timestamps to AWTReplayEvents
    LinkedHashMap<Long, AWTReplayMouseEvent> awtMouseEvents = new LinkedHashMap<>();
    // the scheduler we will use to enable accurate playback. TODO: make this private and have this class auto terminate after last event
    public final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    // system time when replay was started
    // the (lazy instantiation) of the Robot class to be used for actual replay
    private Robot robot;

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
            robot = new Robot();
            logger.info("Key replay started with {} events", awtMouseEvents.size());
        } catch (AWTException e) {
            logger.fatal("Failed to initialize Robot for mouse replay", e);
            throw new RuntimeException(e);
        }
    }

    public void start() {
        for (Long key : awtMouseEvents.keySet()) {
            long delay = key;
            if (delay < 0) delay = 0;
            scheduler.schedule(() -> executeEvent(awtMouseEvents.get(key)), delay, TimeUnit.MILLISECONDS);
        }
    }

    private void executeEvent(AWTReplayMouseEvent event) {
        logger.debug("Executing {} with code {}", event.context, event.button);
        if (event.context.equals("MOUSE_MOVED") || event.context.equals("MOUSE_DRAGGED")) {
            robot.mouseMove(event.x, event.y);
        }
        else if (event.context.equals("MOUSE_PRESSED")) {
            robot.mousePress(event.button);
        }
        else if (event.context.equals("MOUSE_RELEASED")) {
            robot.mouseRelease(event.button);
        }
    }



    static{
        jnativeToAwtMouse.put(NativeMouseEvent.NOBUTTON, MouseEvent.NOBUTTON);
        jnativeToAwtMouse.put(NativeMouseEvent.BUTTON1, MouseEvent.BUTTON1);
        jnativeToAwtMouse.put(NativeMouseEvent.BUTTON2, MouseEvent.BUTTON2);
        jnativeToAwtMouse.put(NativeMouseEvent.BUTTON3, MouseEvent.BUTTON3);
    }

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




