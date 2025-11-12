package com.rapicredit.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class TestState {
    private static final Map<String, String> state = new ConcurrentHashMap<>();

    public static void putMessage(String actorName, String message) {
        if (actorName == null) return;
        state.put(actorName, message);
    }

    public static String getMessage(String actorName) {
        if (actorName == null) return null;
        return state.get(actorName);
    }

    public static void clear(String actorName) {
        if (actorName == null) return;
        state.remove(actorName);
    }

    public static void clearAll() {
        state.clear();
    }
}

