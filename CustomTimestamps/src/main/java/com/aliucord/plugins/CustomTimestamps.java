package com.aliucord.plugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;

import com.aliucord.Main;
import com.aliucord.PluginManager;
import com.aliucord.Utils;
import com.aliucord.api.SettingsAPI;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PrePatchRes;
import com.aliucord.views.TextInput;
import com.aliucord.fragments.SettingsPage;
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
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            setActionBarTitle("CustomTimestamps");
        }

        @Override
        public void onViewBound(View view) {
            super.onViewBound(view);

            int padding = Utils.getDefaultPadding();
            SettingsAPI sets = PluginManager.plugins.get("CustomTimestamps").sets;

            Context context = view.getContext();
            LinearLayout layout = (LinearLayout) ((NestedScrollView) ((CoordinatorLayout) view).getChildAt(1)).getChildAt(0);

            String format = sets.getString("format", "dd.MM.yyyy, HH:mm:ss");
            TextView guide = new TextView(context, null, 0, R$h.UiKit_Settings_Item_SubText);
            setPreview(format, guide);
            guide.setMovementMethod(LinkMovementMethod.getInstance());

            TextInput input = new TextInput(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(padding, padding, padding, 0);
            input.setLayoutParams(params);
            input.setHint("Custom Timestamp Format");
            EditText editText = input.getEditText();
            if (editText == null) return;
            editText.setMaxLines(1);
            editText.setText(format);
            editText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    String newFormat = s.toString();
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
            SpannableStringBuilder s = new SpannableStringBuilder("Formatting guide\n\nPreview: ");
            s.append(format(formatStr, System.currentTimeMillis()));
            s.setSpan(new URLSpan("https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html"), 0, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            view.setText(s);
        }
    }

    @NonNull
    @Override
    public Manifest getManifest() {
        Manifest manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Custom timestamps format everywhere.";
        manifest.version = "1.0.1";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    private static final String className = "com.discord.utilities.time.TimeUtils";
    public static Map<String, List<String>> getClassesToPatch() {
        Map<String, List<String>> map = new HashMap<>();
        map.put(className, Collections.singletonList("toReadableTimeString"));
        return map;
    }

    @Override
    public void start(Context context) {
        patcher.patch(className, "toReadableTimeString", (_this, args, ret) ->
                format(sets.getString("format", defaultFormat), (long) args.get(1)));
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
