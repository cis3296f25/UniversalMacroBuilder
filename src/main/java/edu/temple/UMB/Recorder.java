package edu.temple.UMB;

import java.io.File;

public class Recorder {
    private final File outPath;
    //private final KeyboardEventRecorder keyboardRecorder;
    private final InputEventRecorder inputEventRecorder;

    public Recorder(File outPath) {
        this.outPath = outPath;
        this.inputEventRecorder = new InputEventRecorder();
    }

    public void start() {
        try {
            System.out.println("Recording started. Press ESC to stop...");
            inputEventRecorder.startRecording();

            // Keep the main thread alive while recording
            while (inputEventRecorder.isRecording()) {
                Thread.sleep(200);
            }

            System.out.println("Recording stopped, saving file...");

            Writer keyWriter = new Writer(Writer.Type.KEY);
            keyWriter.writeToFile(outPath, inputEventRecorder.getKeyEvents());
            Writer mouseWriter = new Writer(Writer.Type.MOUSE);
            mouseWriter.writeToFile(outPath, inputEventRecorder.getMouseEvents());

            System.out.println("Saved recorded events to: " + outPath.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

