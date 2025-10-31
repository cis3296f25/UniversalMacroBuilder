package edu.temple.UMB;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Loader {
    private File inFile;

    public Loader(File inFile) {
        this.inFile = inFile;
    }

    // Note that this function does not actually do any form of error handling.
    // It is expected that the checking of file for existence/permissions is done in Main.argChecks().
    // It returns a LinkedHashMap (for later iteration over), where the key is a long (timestamp) and the string is <action>_KEY.
    // It will need later parsing to work with java.awt.Robot. But that's a later problem.
    public LinkedHashMap<Long, String> loadJNativeEventsFromFile() throws FileNotFoundException {
        LinkedHashMap<Long, String> map = new LinkedHashMap<>();
        Scanner sc = new Scanner(inFile);
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
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
        return map;
    }

}
