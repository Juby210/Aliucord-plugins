/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

// based on https://gitdab.com/distok/cutthecord/src/branch/master/patches/embedlinks/1387.patch

package com.aliucord.plugins;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PrePatchRes;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class RestoreImageLinks extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() {
        Manifest manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Restores image links in chat.";
        manifest.version = "1.0.0";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }
    public static Map<String, List<String>> getClassesToPatch() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("com.discord.utilities.textprocessing.MessagePreprocessor", Collections.singletonList("stripSimpleEmbedLink"));
        return map;
    }

    @Override
    public void start(Context context) {
        patcher.prePatch("com.discord.utilities.textprocessing.MessagePreprocessor", "stripSimpleEmbedLink",
                (_this, args) -> new PrePatchRes(null));
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
