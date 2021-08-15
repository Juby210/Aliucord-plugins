/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.aliucord.Main;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePatchFn;
import com.discord.databinding.WidgetSettingsBinding;
import com.discord.widgets.settings.WidgetSettings;

import top.canyie.pine.callback.MethodReplacement;

@SuppressWarnings("unused")
public class Experiments extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() {
        var manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Shows hidden Developer Options tab with Experiments.";
        manifest.version = "1.0.3";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    @Override
    public void start(Context context) throws Throwable {
        patcher.patch("com.discord.stores.StoreExperiments$getExperimentalAlpha$1", "invoke", new Class<?>[0], MethodReplacement.returnConstant(true));

        var settingsClass = WidgetSettings.class;
        var getBinding = settingsClass.getDeclaredMethod("getBinding");
        getBinding.setAccessible(true);

        patcher.patch(settingsClass, "configureUI", new Class<?>[]{ WidgetSettings.Model.class }, new PinePatchFn(callFrame -> {
            try {
                var binding = (WidgetSettingsBinding) getBinding.invoke(callFrame.thisObject);
                if (binding == null) return;
                binding.n.setVisibility(View.VISIBLE);
                binding.o.setVisibility(View.VISIBLE);
                binding.m.setVisibility(View.VISIBLE);
            } catch (Throwable e) { Main.logger.error(e); }
        }));
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
