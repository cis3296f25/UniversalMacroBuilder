package edu.temple.UMB;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static java.lang.Thread.sleep;

/**
 * The {@code Replayer} class loads, translates, and replays recorded input events (currently keyboard events, with mouse support planned for future versions).
 */
public class Replayer {
    private static final Logger logger = LogManager.getLogger(Replayer.class);
    private File inFile;
    private LinkedHashMap<Long, String> loadedJNativeHookEvents = new LinkedHashMap<>();


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
            logger.info("Loaded {} raw events from file {}", loadedJNativeHookEvents.size(), inFile.getAbsolutePath());
            for (Long key : loadedJNativeHookEvents.keySet()) {
                logger.debug("{} - {}", key, loadedJNativeHookEvents.get(key));
            }
        } catch (Exception ex) {
            logger.error("Failed to load events from file {}", inFile.getAbsolutePath(), ex);
        }

        kr = new KeyReplayer(loadedJNativeHookEvents);
    }

    /**
     * Starts the replay of loaded events and waits briefly for completion.
     * Shuts down the {@link KeyReplayer#scheduler} after scheduling and awaits termination for up to one second.
     */
    public void start() {
        System.out.println("Starting Replayer. Press CTRL+C to exit before completion.");
        kr.start(); // TODO: when replaying mouse events as well ensure we start them both at the same time with scheduledexecutor
        kr.scheduler.shutdown(); // TODO: why are we only waiting one second here? most likely causing bug where macros over a second arent really working

        // wait for KeyReplayer thread to exit
        try {
            kr.scheduler.awaitTermination(1, TimeUnit.SECONDS);
            logger.info("Replay finished");
        } catch (InterruptedException e) {
            logger.error("Replay interrupted", e);
            throw new RuntimeException(e);
        }
    }
}
