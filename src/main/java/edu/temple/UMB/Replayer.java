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

    private final int repeatCount;

    private LinkedHashMap<Long, String> loadedJNativeHookEvents = new LinkedHashMap<>();

    Loader l;
    KeyReplayer kr;

    /**
     * Constructs a new {@code Replayer} from the given file path.
     * This constructor immediately loads and translates recorded keyboard events
     * from the file, then initiates replay using {@link KeyReplayer}.
     * @param inPath the path to the input file containing recorded JNativeHook events.
     */
    public Replayer(String inPath, int repeatCount){
        File inFile = new File(inPath);
        this.repeatCount = repeatCount;

        logger.info("Initializing Replayer with file: {}", inFile.getAbsolutePath());
        logger.info("Repeat count set to: {}", repeatCount);

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
    }

    /**
     * Starts the replay of loaded events and waits briefly for completion.
     * Shuts down the {@link KeyReplayer#scheduler} after scheduling and awaits termination for up to one second.
     */
    public void start() {
        System.out.println("Starting Replayer. Press CTRL+C to exit Replayer early.");

        // add a shutdown hook to capture ctrl c and empty event queue (ensuring kr was actually initialized)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down Replayer early.");
            if (this.kr != null){
                this.kr.scheduler.shutdownNow();
                this.kr.releaseAllHeld();
            }
        }));


        if (repeatCount ==-1){
            logger.info("Infinite replay mode.");
            while (true){
                playOnce();
            }
        }
        else{
            logger.info("Replaying {} times.", repeatCount);
            for (int i = 0; i < repeatCount; i++){
                playOnce();
            }
        }
        logger.info("Replay finished.");
    }

    private void playOnce() {
        logger.info("Starting replay iteration.");

        this.kr = new KeyReplayer(loadedJNativeHookEvents);
        long timeNeeded = this.kr.start(); // TODO: when replaying mouse events as well ensure we start them both at the same time with scheduledexecutor

        try {
            this.kr.scheduler.awaitTermination(timeNeeded + 100, TimeUnit.MILLISECONDS);
            this.kr.releaseAllHeld(); // if for some reason a key is being held make sure it doesnt stay that way
        } catch (InterruptedException e) {
            logger.error("Replay interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
}
