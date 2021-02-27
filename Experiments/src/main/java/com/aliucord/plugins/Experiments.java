package com.aliucord.plugins;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.aliucord.Main;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PrePatchFunction;
import com.aliucord.patcher.PrePatchRes;
import com.discord.databinding.WidgetSettingsBinding;
import com.discord.widgets.settings.WidgetSettings;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class Experiments extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() {
        Manifest manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Shows hidden Developer Options tab with Experiments.";
        manifest.version = "1.0.0";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }
    public static Map<String, List<String>> getClassesToPatch() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("com.discord.stores.StoreExperiments$getExperimentalAlpha$1", Collections.singletonList("*"));
        map.put("com.discord.widgets.settings.WidgetSettings", Collections.singletonList("configureUI"));
        return map;
    }

    @Override
    public void start(Context context) {
        PrePatchFunction patch = (_this, args) -> new PrePatchRes(args, true);
        patcher.prePatch("com.discord.stores.StoreExperiments$getExperimentalAlpha$1", "invoke", patch);

        try {
            Method getBinding = WidgetSettings.class.getDeclaredMethod("getBinding");
            getBinding.setAccessible(true);

            patcher.patch("com.discord.widgets.settings.WidgetSettings", "configureUI", (_this, args, ret) -> {
                try {
                    WidgetSettingsBinding binding = (WidgetSettingsBinding) getBinding.invoke(_this);
                    if (binding == null) return ret;
                    binding.l.setVisibility(View.VISIBLE);
                    binding.m.setVisibility(View.VISIBLE);
                    binding.k.setVisibility(View.VISIBLE);
                } catch (Throwable e) { Main.logger.error(e); }
                return ret;
            });
        } catch (Throwable e) { Main.logger.error(e); }
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
