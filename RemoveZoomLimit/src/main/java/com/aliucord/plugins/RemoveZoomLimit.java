package com.aliucord.plugins;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PrePatchRes;

import java.util.*;

@SuppressWarnings("unused")
public class RemoveZoomLimit extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() {
        Manifest manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Removes maximum zoom limit.";
        manifest.version = "1.0.1";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    private static final String className = "c.f.l.b.c";
    public static Map<String, List<String>> getClassesToPatch() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("com.discord.widgets.media.WidgetMedia", Collections.singletonList("getFormattedUrl"));
        map.put(className, Collections.singletonList("f"));
        return map;
    }

    @Override
    public void start(Context context) {
        // load full resolution to see details while zooming
        patcher.patch("com.discord.widgets.media.WidgetMedia", "getFormattedUrl", (_this, args, ret) -> {
            String[] arr = ((String) ret).split("\\?");
            return arr[0] + (arr[1].contains("format=") ? "?format=" + arr[1].split("format=")[1] : "");
        });

        // com.facebook.samples.zoomable.DefaultZoomableController limitScale
        // https://github.com/facebook/fresco/blob/master/samples/zoomable/src/main/java/com/facebook/samples/zoomable/DefaultZoomableController.java#L474-L495
        patcher.prePatch(className, "f", (_this, args) -> new PrePatchRes(args, false));
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
