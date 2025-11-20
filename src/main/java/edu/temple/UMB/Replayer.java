package edu.temple.UMB;

import java.awt.*;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The {@code Replayer} class loads, translates, and replays recorded input events (currently keyboard events, with mouse support planned for future versions).
 */
public class Replayer {
    private static final Logger logger = LogManager.getLogger(Replayer.class);
    Loader l;
    KeyReplayer kr;

    /**
     * Constructs a new {@code Replayer} from the given file path.
     * This constructor immediately loads and translates recorded keyboard events
     * from the file, then initiates replay using {@link KeyReplayer}.
     * @param inPath the path to the input file containing recorded JNativeHook events.
     */
    public Replayer(String inPath) {
        File inFile = new File(inPath);
        l = new Loader(inFile);

        // load events from file
        LinkedHashMap<Long, String> loadedJNativeHookEvents = new LinkedHashMap<>();
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
        System.out.println("Starting Replayer. Press CTRL+C to exit Replayer early.");
        long timeNeeded = kr.start(); // TODO: when replaying mouse events as well ensure we start them both at the same time with scheduledexecutor

        // wait for KeyReplayer thread to exit
        try {
            kr.scheduler.awaitTermination(timeNeeded + 100, TimeUnit.MILLISECONDS);
            logger.info("Replay finished");
        } catch (InterruptedException e) {
            logger.error("Replay interrupted", e);
            throw new RuntimeException(e);
        }
    }
}
