package edu.temple.UMB;

import java.io.File;

public class Recorder {
    private final File outPath;
    private final KeyboardEventRecorder keyboardRecorder;

    public Recorder(File outPath) {
        this.outPath = outPath;
        this.keyboardRecorder = new KeyboardEventRecorder();
    }

    public void start() {
        try {
            System.out.println("Recording started. Press ESC to stop...");
            keyboardRecorder.startRecording();

            // Keep the main thread alive while recording
            while (keyboardRecorder.isRecording()) {
                Thread.sleep(200);
            }

            System.out.println("Recording stopped, saving file...");

            Writer writer = new Writer(Writer.Type.KEY);
            writer.writeToFile(outPath, keyboardRecorder.getEvents());

            System.out.println("Saved recorded keys to: " + outPath.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

