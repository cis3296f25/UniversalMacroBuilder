package edu.temple.UMB;

import java.util.LinkedHashMap;

public class KeyReplayer {
    LinkedHashMap<Long, Integer> awtEvents = new LinkedHashMap<>();

    public KeyReplayer(LinkedHashMap<Long, Integer> awtEvents) {
        this.awtEvents = awtEvents;
    }
}


