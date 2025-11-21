package edu.temple.UMB;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Loads recorded JNativeHook key events from a text file into an ordered map.
 */
public class Loader {
    private static final Logger logger = LogManager.getLogger(Loader.class);
    private File inFile;

    /**
     * Creates a loader for the given input file.
     * @param inFile file containing recorded events
     */
    public Loader(File inFile) {
        this.inFile = inFile;
    }

    /**
     * Parses the input file into a {@link LinkedHashMap} of timestamps to event strings.
     * The returned value maps {@code <timestamp>} to {@code <ACTION>_<KEYCODE>} as read from the file.
     * This method assumes basic file correctness; callers should validate existence and permissions in {@link Main#argChecks(String[])}.
     * @return ordered map of timestamps to event tokens
     * @throws FileNotFoundException if the input file cannot be opened
     */
    public LinkedHashMap<Long, String> loadJNativeEventsFromFile() throws FileNotFoundException {
        LinkedHashMap<Long, String> map = new LinkedHashMap<>();
        logger.debug("Loading JNativeHook key events from file: {}", inFile.getAbsolutePath());
        Scanner sc = new Scanner(inFile);
        int lineCount = 0;
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            lineCount++;
            if (line.equals("START KEY EVENTS")) {
                // should be first line of file. skip
                continue;
            } else if (line.equals("END KEY EVENTS")) {
                // finished keyEvents. our job here is done
                sc.close();
                break;
            }
            // parse line and add to map
            String[] parts = line.split(" ");
            map.put(Long.parseLong(parts[0]), parts[1] + "_" + parts[2]);
        }

        logger.info("Loaded {} key events from file {} ({} lines read)", map.size(), inFile.getAbsolutePath(), lineCount);
        return map;
    }

}
