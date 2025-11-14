package edu.temple.UMB;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MouseLoader {
    private static final Logger logger = LogManager.getLogger(MouseLoader.class);
    private File inFile;

    public MouseLoader(File inFile) {
        this.inFile = inFile;
    }

    public LinkedHashMap<Long, String> loadJNativeEventsFromFile() throws FileNotFoundException {
        LinkedHashMap<Long, String> map = new LinkedHashMap<>();
        logger.debug("Loading JNativeHook events from file: {}", inFile.getAbsolutePath());
        Scanner sc = new Scanner(inFile);
        int lineCount = 0;
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            lineCount++;
            if(!line.contains("MOUSE")){
                continue;
            } else if (line.equals("START MOUSE EVENTS")) {
                // should be first line of file. skip
                continue;
            } else if (line.equals("END MOUSE EVENTS")) {
                // finished keyEvents. our job here is done
                break;
            }
            // parse line and add to map
            String[] parts = line.split(" ");
            map.put(Long.parseLong(parts[0]), parts[1] + "-" + parts[2] + "-" + parts[3]);
        }
        logger.info("Loaded {} mouse events from file {} ({} lines read)", map.size(), inFile.getAbsolutePath(), lineCount);
        return map;
    }


}
