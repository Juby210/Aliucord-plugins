/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePatchFn;
import com.lytefast.flexinput.fragment.FlexInputFragment;

@SuppressWarnings({"unused", "ConstantConditions"})
public class NoGiftButton extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() {
        var manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Hides useless gift button.";
        manifest.version = "1.0.3";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    @Override
    public void start(Context context) {
        patcher.patch(FlexInputFragment.d.class, "invoke", new Class<?>[]{ Object.class }, new PinePatchFn(callFrame -> {
            var fragment = (FlexInputFragment) ((FlexInputFragment.d) callFrame.thisObject).receiver;
            var binding = fragment.j();
            if (binding == null) return;
            binding.h.setVisibility(View.GONE); // hide expand button
            binding.n.setVisibility(View.GONE); // hide gift button
            binding.m.setVisibility(View.VISIBLE); // show gallery button
        }));
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
