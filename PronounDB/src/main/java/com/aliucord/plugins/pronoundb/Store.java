package com.aliucord.plugins.pronoundb;

import com.aliucord.Main;
import com.aliucord.Utils;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public final class Store {
    public static Map<Long, String> cache = new HashMap<>();

    private static final Type resType = TypeToken.getParameterized(Map.class, Long.class, String.class).getType();
    private static final List<Long> buffer = new ArrayList<>();
    private static Thread timerThread = new Thread(Store::runThread);
    public static void fetchPronouns(Long id) {
        if (!timerThread.isAlive()) {
            if (timerThread.getState() == Thread.State.TERMINATED) timerThread = new Thread(Store::runThread);
            timerThread.start();
        }
        if (!buffer.contains(id)) buffer.add(id);
        try {
            timerThread.join();
        } catch (Throwable ignored) {}
    }

    private static void runThread() {
        try {
            Thread.sleep(50);
            Long[] bufferCopy = buffer.toArray(new Long[0]);
            buffer.clear();
            Map<Long, String> res = Utils.fromJson(request(Constants.Endpoints.LOOKUP_BULK(bufferCopy)), resType);
            cache.putAll(res);
            for (Long id : bufferCopy) {
                if (!cache.containsKey(id)) cache.put(id, "unspecified");
            }
        } catch (Throwable e) {
            Main.logger.error("PronounDB error", e);
        }
    }

    private static String request(String url) throws Throwable {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestProperty("User-Agent", "Aliucord");

        String ln;
        StringBuilder res = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        while ((ln = reader.readLine()) != null) res.append(ln);
        reader.close();

        return res.toString().trim();
    }
}
