package edu.temple.UMB;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KeyLoader {
    private static final Logger logger = LogManager.getLogger(KeyLoader.class);
    private File inFile;

    public KeyLoader(File inFile) {
        this.inFile = inFile;
    }

    // Note that this function does not actually do any form of error handling.
    // It is expected that the checking of file for existence/permissions is done in Main.argChecks().
    // It returns a LinkedHashMap (for later iteration over), where the key is a long (timestamp) and the string is <action>_KEY.
    // It will need later parsing to work with java.awt.Robot. But that's a later problem.
    public LinkedHashMap<Long, String> loadJNativeEventsFromFile() throws FileNotFoundException {
        LinkedHashMap<Long, String> map = new LinkedHashMap<>();
        logger.debug("Loading JNativeHook events from file: {}", inFile.getAbsolutePath());
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
