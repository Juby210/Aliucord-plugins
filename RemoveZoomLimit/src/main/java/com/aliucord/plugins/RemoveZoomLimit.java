/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins;

import android.content.Context;
import android.graphics.Matrix;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.airbnb.lottie.parser.AnimatableValueParser;
import com.aliucord.Utils;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePatchFn;

import top.canyie.pine.callback.MethodReplacement;

@SuppressWarnings("unused")
public class RemoveZoomLimit extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() {
        Manifest manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Removes maximum zoom limit.";
        manifest.version = "1.0.4";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    @Override
    public void start(Context context) {
        // load full resolution to see details while zooming
        patcher.patch("com.discord.widgets.media.WidgetMedia", "getFormattedUrl", new Class<?>[]{ Context.class, Uri.class }, new PinePatchFn(callFrame -> {
            var res = (String) callFrame.getResult();
            if (res.contains(".discordapp.net/")) {
                var arr = res.split("\\?");
                callFrame.setResult(arr[0] + (arr[1].contains("format=") ? "?format=" + arr[1].split("format=")[1] : ""));
            }
        }));

        // com.facebook.samples.zoomable.DefaultZoomableController limitScale
        // https://github.com/facebook/fresco/blob/master/samples/zoomable/src/main/java/com/facebook/samples/zoomable/DefaultZoomableController.java#L474-L495
        patcher.patch("c.f.l.b.c", "f", new Class<?>[]{ Matrix.class, float.class, float.class, int.class }, MethodReplacement.returnConstant(false));

        // Remove max resolution limit in image loader
        // Gets method by param types because the method is in heavily obfuscated class
        for (var m : AnimatableValueParser.class.getDeclaredMethods()) {
            var params = m.getParameterTypes();
            if (params.length == 4 && params[0] == c.f.j.d.f.class && params[1] == c.f.j.d.e.class && params[3] == int.class) {
                Utils.log("[RemoveZoomLimit] Found obfuscated method to limit resolution: " + m.getName());
                patcher.patch(m, MethodReplacement.returnConstant(1));
                break;
            }
        }
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
