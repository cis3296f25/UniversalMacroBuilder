package edu.temple.UMB;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Loads, translates, and replays recorded input events.
 * This class coordinates both keyboard and mouse replayers. Events are loaded from
 * a file using dedicated loader classes, translated into AWT-friendly representations,
 * and then scheduled for playback. The same input can be replayed multiple times
 * according to the configured repeat count.
 */
public class Replayer {
    private static final Logger logger = LogManager.getLogger(Replayer.class);
    private LinkedHashMap<Long, String> loadedJNativeHookEvents = new LinkedHashMap<>();
    private LinkedHashMap<Long, String> loadedJNativeHookMouseEvents = new LinkedHashMap<>();

    private final int repeatCount;

    Loader kl;
    MouseLoader ml;
    KeyReplayer kr;
    MouseReplayer mr;

    /**
     * Constructs a new replayer from the given file path.
     * Loads recorded keyboard and mouse events, translates them, and prepares
     * keyboard and mouse replayers for later execution.
     *
     * @param inPath path to the input file containing recorded JNativeHook events
     * @param repeatCount number of times to replay the macro; use -1 for infinite
     */
    public Replayer(String inPath, int repeatCount){
        File inFile = new File(inPath);
        this.repeatCount = repeatCount;

        logger.info("Initializing Replayer with file: {}", inFile.getAbsolutePath());
        logger.info("Repeat count set to: {}", repeatCount);

        kl = new Loader(inFile);

        // load events from file
        try {
            loadedJNativeHookEvents = kl.loadJNativeEventsFromFile();
            logger.info("Loaded {} raw key events from file {}", loadedJNativeHookEvents.size(), inFile.getAbsolutePath());
            for (Long key : loadedJNativeHookEvents.keySet()) {
                logger.debug("{} - {}", key, loadedJNativeHookEvents.get(key));
            }
        } catch (Exception ex) {
            logger.error("Failed to load key events from file {}", inFile.getAbsolutePath(), ex);
        }

        ml = new MouseLoader(inFile);

        try {
            loadedJNativeHookMouseEvents = ml.loadJNativeEventsFromFile();
            logger.info("Loaded {} raw mouse events from file {}", loadedJNativeHookMouseEvents.size(), inFile.getAbsolutePath());
            for (Long key : loadedJNativeHookMouseEvents.keySet()) {
                logger.debug("{} - {}", key, loadedJNativeHookMouseEvents.get(key));
            }
        } catch (Exception ex) {
            logger.error("Failed to load mouse events from file {}", inFile.getAbsolutePath(), ex);
        }

        kr = new KeyReplayer(loadedJNativeHookEvents);
        mr = new MouseReplayer(loadedJNativeHookMouseEvents);
    }

    /**
     * Starts the replay of loaded events and waits briefly for completion.
     * Shuts down the mouse and keyboard replayers execs after scheduling and awaits termination for up to one second.
     */
    public void start() {
        System.out.println("Starting Replayer. Press CTRL+C to exit Replayer early.");

        // add a shutdown hook to capture ctrl c and empty event queue (ensuring kr was actually initialized)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down Replayer early.");
            if (this.kr != null){
                this.kr.exec.shutdownNow();
                this.kr.releaseAllHeld();
                this.mr.exec.shutdownNow();
                // TODO: mouse replay release held?
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
        this.mr  = new MouseReplayer(loadedJNativeHookMouseEvents);

        ExecutorService exec = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);

        exec.submit(() -> {
            try {
                latch.await();  // both wait for signal
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            kr.start();
        });

        exec.submit(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            mr.start();
        });

        // synchronize start
        latch.countDown();

        try {
            this.kr.exec.awaitTermination(kr.getMaxDelay() + 100, TimeUnit.MILLISECONDS);
            this.mr.exec.awaitTermination(mr.getMaxDelay() + 100, TimeUnit.MILLISECONDS);
            this.kr.releaseAllHeld(); // if for some reason a key is being held make sure it doesnt stay that way
            // TODO: mouse replay release held?
        } catch (InterruptedException e) {
            logger.error("Replay interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
}
