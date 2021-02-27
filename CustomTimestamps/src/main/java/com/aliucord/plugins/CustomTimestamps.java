package com.aliucord.plugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;

import com.aliucord.PluginManager;
import com.aliucord.Utils;
import com.aliucord.api.SettingsAPI;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PrePatchRes;
import com.aliucord.views.TextInput;
import com.aliucord.fragments.SettingsPage;

import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings("unused")
public class CustomTimestamps extends Plugin {
    public CustomTimestamps() {
        settings = new Settings(PluginSettings.class);
    }

    public static class PluginSettings extends SettingsPage {
        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            setActionBarTitle("CustomTimestamps");
        }

        @Override
        public void onViewBound(View view) {
            super.onViewBound(view);

            int padding = Utils.getDefaultPadding();
            SettingsAPI sets = PluginManager.plugins.get("CustomTimestamps").sets;

            TextInput input = new TextInput(view.getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(padding, padding, padding, padding);
            input.setLayoutParams(params);
            input.setHint("Custom Timestamp Format");
            EditText editText = input.getEditText();
            editText.setMaxLines(1);
            editText.setText(sets.getString("format", "dd.MM.yyyy, HH:mm:ss"));
            editText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    sets.setString("format", s.toString());
                    // TODO: add preview
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });
            ((LinearLayout) ((NestedScrollView) ((CoordinatorLayout) view).getChildAt(1)).getChildAt(0)).addView(input);
        }
    }

    @NonNull
    @Override
    public Manifest getManifest() {
        Manifest manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Custom timestamps format everywhere.";
        manifest.version = "1.0.0";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }
    public static Map<String, List<String>> getClassesToPatch() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("com.discord.utilities.time.TimeUtils", Collections.singletonList("toReadableTimeString"));
        return map;
    }

    @Override
    @SuppressLint("SimpleDateFormat")
    public void start(Context context) {
        patcher.prePatch("com.discord.utilities.time.TimeUtils", "toReadableTimeString", (_this, args) -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat(sets.getString("format", "dd.MM.yyyy, HH:mm:ss"));
            return new PrePatchRes(args, dateFormat.format(new Date((long) args.get(1))));
        });
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
