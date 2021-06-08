/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins.pronoundb;

import com.aliucord.Http;
import com.aliucord.Main;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

public final class Store {
    public static Map<Long, String> cache = new HashMap<>();

    private static final Type resType = TypeToken.getParameterized(Map.class, Long.class, String.class).getType();
    private static final List<Long> buffer = new ArrayList<>();
    private static Thread timerThread = new Thread(Store::runThread);
    public static void fetchPronouns(Long id) {
        var state = timerThread.getState();
        if (!timerThread.isAlive() && state != Thread.State.RUNNABLE) {
            if (state == Thread.State.TERMINATED) timerThread = new Thread(Store::runThread);
            try {
                timerThread.start();
            } catch (Throwable e) {
                Main.logger.error("Failed to start timerThread, State: " + state, e);
            }
        }
        if (!buffer.contains(id)) buffer.add(id);
        try {
            timerThread.join();
        } catch (Throwable ignored) {}
    }

    private static void runThread() {
        try {
            Thread.sleep(50);
            var bufferCopy = buffer.toArray(new Long[0]);
            buffer.clear();
            Map<Long, String> res = Http.simpleJsonGet(Constants.Endpoints.LOOKUP_BULK(bufferCopy), resType);
            cache.putAll(res);
            for (var id : bufferCopy) {
                if (!cache.containsKey(id)) cache.put(id, "unspecified");
            }
        } catch (Throwable e) {
            Main.logger.error("PronounDB error", e);
        }
    }
}
