package edu.temple.UMB;

import java.io.*;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Writes recorded events to a text file using a simple header/body/footer format.
 */
public class Writer {
    private static final Logger logger = LogManager.getLogger(Writer.class);

    /**
     * The category of events to write.
     */
    public enum Type { MOUSE, KEY }

    private final Type type;

    /**
     * Creates a writer for the given event {@link Type}.
     * @param type whether events are {@code KEY} or {@code MOUSE}
     */
    public Writer(Type type) {
        this.type = type;
    }

    /**
     * Appends the given events to the file at {@code path}.
     * The format is:
     * {@code START <TYPE> EVENTS}
     * one line per event from {@link Event#toString()}
     * {@code END <TYPE> EVENTS}
     * {@code EOF} is also written when {@code type} is {@code MOUSE} to mark end of file.
     * @param path destination file
     * @param events events to write
     * @throws IOException if writing fails
     */
    public void writeToFile(File path, List<? extends Event> events) throws IOException {
        logger.info("Writing {} {} events to {}", events.size(), type.name(), path.getAbsolutePath());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path, true))) {
            writer.write("START " + type.name() + " EVENTS\n");
            for (Event e : events) {
                writer.write(e.toString());
                writer.newLine();
            }
            writer.write("END " + type.name() + " EVENTS\n");
            if(type.name().equals("MOUSE")){
                writer.write("EOF\n");
            }
            logger.debug("Finished writing {} events of type {}", events.size(), type.name());
        } catch (IOException ex) {
            logger.error("Failed to write {} events of type {} to {}", events.size(), type.name(), path.getAbsolutePath(), ex);
            throw ex;
        }
    }
}
