package edu.temple.UMB;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
            writeToFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeToFile() {
        try (FileWriter writer = new FileWriter(outPath)) {
            writer.write("START KEY EVENTS\n");

            for (KeyEvent event : keyboardRecorder.getEvents()) {
                long delta = event.getDelta(); // already time since first key
                String keyText = com.github.kwhat.jnativehook.keyboard.NativeKeyEvent.getKeyText(
                        event.getEvent().getKeyCode());
                writer.write(delta + " KEY_PRESSED " + keyText + "\n");
            }

            writer.write("END KEY EVENTS\nEOF\n");
            System.out.println("Saved recorded keys to: " + outPath.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
