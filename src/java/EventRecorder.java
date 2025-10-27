import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class EventRecorder {

    private BufferedWriter writer;
    private long lastEventTime;
    private boolean keySectionStarted = false;

    public EventRecorder(String filePath) throws IOException {
        writer = new BufferedWriter(new FileWriter(filePath));
        writer.write("START KEY EVENTS\n");
        lastEventTime = System.currentTimeMillis();
    }

    public void recordKeyEvent(String event) {
        try {
            long now = System.currentTimeMillis();
            long delta = now - lastEventTime;
            lastEventTime = now;

            writer.write(delta + " " + event + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void endKeyEvents() {
        try {
            writer.write("END KEY EVENTS\n");
            //currently only records key events
            //writer.write("START MOUSE EVENTS\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
