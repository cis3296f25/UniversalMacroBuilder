package edu.temple.UMB;

import java.io.*;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Writer {
    private static final Logger logger = LogManager.getLogger(Writer.class);
    public enum Type { MOUSE, KEY }

    private final Type type;

    public Writer(Type type) {
        this.type = type;
    }

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
