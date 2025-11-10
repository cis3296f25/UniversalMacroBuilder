package edu.temple.UMB;

import java.io.*;
import java.util.List;

public class Writer {
    public enum Type { MOUSE, KEY }

    private final Type type;

    public Writer(Type type) {
        this.type = type;
    }

    public void writeToFile(File path, List<? extends Event> events) throws IOException {
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

        }
    }
}
