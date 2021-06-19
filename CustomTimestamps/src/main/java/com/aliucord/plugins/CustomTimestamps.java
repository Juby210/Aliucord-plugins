/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;

import com.aliucord.Main;
import com.aliucord.PluginManager;
import com.aliucord.Utils;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePatchFn;
import com.aliucord.views.TextInput;
import com.aliucord.fragments.SettingsPage;
import com.discord.utilities.time.Clock;
import com.discord.utilities.time.TimeUtils;
import com.lytefast.flexinput.R$h;

import java.text.SimpleDateFormat;
import java.util.*;

@SuppressLint("SimpleDateFormat")
@SuppressWarnings("unused")
public class CustomTimestamps extends Plugin {
    public CustomTimestamps() {
        settings = new Settings(PluginSettings.class);
    }

    public static class PluginSettings extends SettingsPage {
        @Override
        @SuppressWarnings("ResultOfMethodCallIgnored")
        public void onViewBound(View view) {
            super.onViewBound(view);
            setActionBarTitle("CustomTimestamps");

            var padding = Utils.getDefaultPadding();
            var sets = Objects.requireNonNull(PluginManager.plugins.get("CustomTimestamps")).sets;

            var context = view.getContext();
            var layout = (LinearLayout) ((NestedScrollView) ((CoordinatorLayout) view).getChildAt(1)).getChildAt(0);

            var format = sets.getString("format", "dd.MM.yyyy, HH:mm:ss");
            var guide = new TextView(context, null, 0, R$h.UiKit_Settings_Item_SubText);
            setPreview(format, guide);
            guide.setMovementMethod(LinkMovementMethod.getInstance());

            var input = new TextInput(context);
            var params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(padding, padding, padding, 0);
            input.setLayoutParams(params);
            input.setHint("Custom Timestamp Format");
            var editText = input.getEditText();
            if (editText == null) return;
            editText.setMaxLines(1);
            editText.setText(format);
            editText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    var newFormat = s.toString();
                    sets.setString("format", newFormat);
                    setPreview(newFormat, guide);
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });
            layout.addView(input);
            layout.addView(guide);
        }

        private void setPreview(String formatStr, TextView view) {
            var s = new SpannableStringBuilder("Formatting guide\n\nPreview: ");
            s.append(format(formatStr, System.currentTimeMillis()));
            s.setSpan(new URLSpan("https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html"), 0, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            view.setText(s);
        }
    }

    @NonNull
    @Override
    public Manifest getManifest() {
        var manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Custom timestamps format everywhere.";
        manifest.version = "1.0.3";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    @Override
    public void start(Context context) throws Throwable {
        patcher.patch(TimeUtils.class.getDeclaredMethod("toReadableTimeString", Context.class, long.class, Clock.class), new PinePatchFn(callFrame ->
            callFrame.setResult(format(sets.getString("format", defaultFormat), (long) callFrame.args[1]))));
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }

    private static final String defaultFormat = "dd.MM.yyyy, HH:mm:ss";

    public static String format(String format, long time) {
        try {
            return new SimpleDateFormat(format).format(new Date(time));
        } catch (Throwable e) {
            Main.logger.info("Invalid format for CustomTimestamps, using default format");
            return new SimpleDateFormat(defaultFormat).format(new Date(time));
        }
    }
}
