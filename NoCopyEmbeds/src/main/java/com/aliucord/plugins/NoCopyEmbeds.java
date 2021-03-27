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
public class NoCopyEmbeds extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() {
        Manifest manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Disables copying text from embeds.";
        manifest.version = "1.0.1";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    private static final String className = "com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemEmbed$1";
    public static Map<String, List<String>> getClassesToPatch() {
        Map<String, List<String>> map = new HashMap<>();
        map.put(className, Collections.singletonList("invoke"));
        return map;
    }

    @Override
    public void start(Context context) {
        patcher.prePatch(className, "invoke", (_this, args) -> new PrePatchRes(args, null));
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
