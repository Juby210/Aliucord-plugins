package com.aliucord.plugins;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.aliucord.entities.Plugin;
import com.lytefast.flexinput.fragment.FlexInputFragment;

import java.util.*;

@SuppressWarnings("unused")
public class NoGiftButton extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() {
        Manifest manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Hides useless gift button.";
        manifest.version = "1.0.0";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }
    public static Map<String, List<String>> getClassesToPatch() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("com.lytefast.flexinput.fragment.FlexInputFragment$d", Collections.singletonList("invoke"));
        return map;
    }

    @Override
    public void start(Context context) {
        patcher.patch("com.lytefast.flexinput.fragment.FlexInputFragment$d", "invoke", (_this, args, ret) -> {
            FlexInputFragment fragment = (FlexInputFragment) ((FlexInputFragment.d) _this).receiver;
            f.b.a.d.a widgetBinding = fragment.i();
            if (widgetBinding != null) {
                widgetBinding.h.setVisibility(View.GONE); // hide expand button
                widgetBinding.n.setVisibility(View.GONE); // hide gift button
                widgetBinding.m.setVisibility(View.VISIBLE); // show gallery button
            }
            return ret;
        });
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
