package edu.temple.UMB;

import java.awt.*;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Loads recorded input events from disk and replays them.
 * This implementation currently replays keyboard events via {@link KeyReplayer}.
 * A negative repeat count enables continuous (infinite) playback.
 */
public class Replayer {
    private static final Logger logger = LogManager.getLogger(Replayer.class);

    private final int repeatCount;

    private LinkedHashMap<Long, String> loadedJNativeHookEvents = new LinkedHashMap<>();

    Loader l;
    KeyReplayer kr;

    /**
     * Constructs a new {@code Replayer} that loads recorded events from a file.
     * The constructor loads the raw JNativeHook events via {@link Loader} and prepares them for playback.
     * Actual replay is started by calling {@link #start()}.
     *
     * @param inPath the path to the input file containing recorded JNativeHook events
     * @param repeatCount the number of times to replay the sequence; use -1 for infinite replay
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
     * Starts the replay of loaded events.
     * For each iteration, this method creates a new {@link KeyReplayer}, calls {@link KeyReplayer#start()},
     * then waits for the scheduled tasks to complete before ensuring any held keys are released.
     */
    public void start() {
        System.out.println("Starting Replayer. Press CTRL+C to exit Replayer early.");

        // add a shutdown hook to capture ctrl c and empty event queue (ensuring kr was actually initialized)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down Replayer early.");
            if (this.kr != null){
                this.kr.exec.shutdownNow();
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
            this.kr.exec.awaitTermination(timeNeeded + 100, TimeUnit.MILLISECONDS);
            this.kr.releaseAllHeld(); // if for some reason a key is being held make sure it doesnt stay that way
        } catch (InterruptedException e) {
            logger.error("Replay interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
}
