package edu.temple.UMB;

import java.io.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * High-level controller that records keyboard and mouse input and writes it to a file.
 */
public class Recorder {
    private static final Logger logger = LogManager.getLogger(Recorder.class);
    private final File outPath;
    private final InputEventRecorder inputEventRecorder;

    /**
     * Creates a recorder that writes to {@code outPath} and stops on the given key.
     * @param outPath destination file
     * @param stopKey key name such as {@code ESCAPE}
     */
    public Recorder(File outPath, String stopKey) {
        this.outPath = outPath;
        this.inputEventRecorder = new InputEventRecorder(stopKey);
    }

    /**
     * Starts recording until the stop key is pressed, then writes events to {@code outPath}.
     */
    public void start() {
        try {
            System.out.println("Recording started. Press your specified stopkey to stop (default is ESC)...");
            logger.info("Recording started. Waiting for stop key...");
            inputEventRecorder.startRecording();

            // Keep the main thread alive while recording
            while (inputEventRecorder.isRecording()) {
                Thread.sleep(200);
            }

            System.out.println("Recording stopped, saving file...");
            logger.info("Recording stopped. Saving to file: {}", outPath.getAbsolutePath());

            Writer keyWriter = new Writer(Writer.Type.KEY);
            keyWriter.writeToFile(outPath, inputEventRecorder.getKeyEvents());
            Writer mouseWriter = new Writer(Writer.Type.MOUSE);
            mouseWriter.writeToFile(outPath, inputEventRecorder.getMouseEvents());

            System.out.println("Saved recorded events to: " + outPath.getAbsolutePath());
            logger.info("Saved recorded events to: {}", outPath.getAbsolutePath());

        } catch (Exception e) {
            logger.error("Error during recording or writing to file {}", outPath.getAbsolutePath(), e);
        }
    }
}

