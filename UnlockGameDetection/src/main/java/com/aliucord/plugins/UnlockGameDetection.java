package com.aliucord.plugins;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PrePatchRes;

import java.util.*;

@SuppressWarnings("unused")
public class UnlockGameDetection extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() {
        Manifest manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Unlocks game detection on all devices.";
        manifest.version = "1.0.0";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    private static final String className = "com.discord.utilities.games.GameDetectionHelper";
    public static Map<String, List<String>> getClassesToPatch() {
        Map<String, List<String>> map = new HashMap<>();
        map.put(className, Collections.singletonList("isGameDetectionSupported"));
        return map;
    }

    @Override
    public void start(Context context) {
        patcher.prePatch(className, "isGameDetectionSupported", (_this, args) -> new PrePatchRes(args, true));
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
