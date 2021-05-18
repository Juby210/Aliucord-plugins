package com.aliucord.plugins;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliucord.PluginManager;
import com.aliucord.Utils;
import com.aliucord.api.SettingsAPI;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PrePatchRes;
import com.aliucord.widgets.LinearLayout;
import com.discord.app.AppBottomSheet;
import com.discord.utilities.color.ColorCompat;
import com.discord.views.CheckedSetting;
import com.lytefast.flexinput.R$b;

import java.util.*;

@SuppressWarnings("unused")
public class StickerTweaks extends Plugin {
    public static final class PluginSettings extends AppBottomSheet {
        public int getContentViewResId() { return 0; }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            SettingsAPI sets = PluginManager.plugins.get("StickerTweaks").sets;
            Context context = inflater.getContext();
            LinearLayout layout = new LinearLayout(context);
            layout.setBackgroundColor(ColorCompat.getThemedColor(context, R$b.colorBackgroundPrimary));

            layout.addView(createSwitch(context, sets, "enabled", "Enable stickers"));
            layout.addView(createSwitch(context, sets, "autocomplete", "Enable stickers autocomplete"));
            return layout;
        }

        private CheckedSetting createSwitch(Context context, SettingsAPI sets, String key, String label) {
            CheckedSetting cs = Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, label, null);
            cs.setChecked(sets.getBool(key, true));
            cs.setOnCheckedListener(c -> sets.setBool(key, c));
            return cs;
        }
    }

    public StickerTweaks() {
        settings = new Settings(PluginSettings.class, Settings.Type.BOTTOMSHEET);
    }

    @NonNull
    @Override
    public Manifest getManifest() {
        Manifest manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Adds option to enable/disable stickers and stickers autocomplete.";
        manifest.version = "1.0.0";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    private static final String className = "com.discord.widgets.chat.input.sticker.StickerPickerFeatureFlag";
    private static final String chatInputMappingFunctions = "com.discord.widgets.chat.input.applicationcommands.WidgetChatInputModelMappingFunctions";
    public static Map<String, List<String>> getClassesToPatch() {
        Map<String, List<String>> map = new HashMap<>();
        map.put(className, Collections.singletonList("isEnabled"));
        map.put(chatInputMappingFunctions, Collections.singletonList("getStickerMatches"));
        return map;
    }

    @Override
    public void start(Context context) {
        patcher.prePatch(className, "isEnabled", (_this, args) -> new PrePatchRes(sets.getBool("enabled", true)));

        patcher.patch(chatInputMappingFunctions, "getStickerMatches", (_this, args, ret) -> {
            if (!sets.getBool("autocomplete", true)) return Collections.EMPTY_LIST;
            return ret;
        });
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
