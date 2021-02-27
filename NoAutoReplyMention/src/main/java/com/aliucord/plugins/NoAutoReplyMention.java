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
public class NoAutoReplyMention extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() {
        Manifest manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Disables reply mention by default.";
        manifest.version = "0.0.1";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    private static final String className = "com.discord.stores.StorePendingReplies";
    public static Map<String, List<String>> getClassesToPatch() {
        Map<String, List<String>> map = new HashMap<>();
        map.put(className, Collections.singletonList("onCreatePendingReply"));
        return map;
    }

    @Override
    public void start(Context context) {
        patcher.prePatch(className, "onCreatePendingReply", (_this, args) -> {
            args.set(2, false);
            args.set(3, true);
            return new PrePatchRes(args);
        });
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
