/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliucord.Utils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Hook;
import com.aliucord.patcher.InsteadHook;
import com.aliucord.widgets.LinearLayout;
import com.discord.app.AppBottomSheet;
import com.discord.utilities.color.ColorCompat;
import com.discord.views.CheckedSetting;
import com.discord.widgets.media.WidgetMedia;
import com.lytefast.flexinput.R;

@AliucordPlugin
@SuppressWarnings("unused")
public final class RemoveZoomLimit extends Plugin {
    public RemoveZoomLimit() {
        settingsTab = new SettingsTab(PluginSettings.class, SettingsTab.Type.BOTTOM_SHEET).withArgs(this);
    }

    public static class PluginSettings extends AppBottomSheet {
        public int getContentViewResId() { return 0; }

        private final RemoveZoomLimit plugin;
        public PluginSettings(RemoveZoomLimit plugin) {
            this.plugin = plugin;
        }

        @Nullable
        @Override
        @SuppressLint("SetTextI18n")
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            var context = inflater.getContext();
            var layout = new LinearLayout(context);
            layout.setBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorBackgroundPrimary));

            var cs = Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Disable max resolution limit", "Warning: It can cause crashes on some devices");
            cs.setChecked(plugin.settings.getBool("removeMaxRes", false));
            cs.setOnCheckedListener(c -> {
                plugin.settings.setBool("removeMaxRes", c);
                plugin.removeMaxRes();
            });
            layout.addView(cs);

            return layout;
        }
    }

    @Override
    public void start(Context context) throws Throwable {
        // load full resolution to see details while zooming
        patcher.patch(WidgetMedia.class, "getFormattedUrl", new Class<?>[]{ Context.class, Uri.class }, new Hook(param -> {
            var res = (String) param.getResult();
            if (res.contains(".discordapp.net/")) {
                var arr = res.split("\\?");
                param.setResult(arr[0] + (arr[1].contains("format=") ? "?format=" + arr[1].split("format=")[1] : ""));
            }
        }));

        // com.facebook.samples.zoomable.DefaultZoomableController limitScale
        // https://github.com/facebook/fresco/blob/master/samples/zoomable/src/main/java/com/facebook/samples/zoomable/DefaultZoomableController.java#L474-L495
        patcher.patch(b.f.l.b.c.class.getDeclaredMethod("f", Matrix.class, float.class, float.class, int.class), InsteadHook.returnConstant(false));

        removeMaxRes();
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }

    private Runnable maxResUnpatch;

    public void removeMaxRes() {
        if (settings.getBool("removeMaxRes", false)) {
            // Remove max resolution limit in image loader
            // Gets method by param types because the method is in heavily obfuscated class
            var f = b.f.j.d.f.class;
            var e = b.f.j.d.e.class;
            var i = int.class;
            for (var m : b.c.a.a0.d.class.getDeclaredMethods()) {
                var params = m.getParameterTypes();
                if (params.length == 4 && params[0] == f && params[1] == e && params[3] == i) {
                    logger.debug("Found obfuscated method to limit resolution: " + m.getName());
                    maxResUnpatch = patcher.patch(m, InsteadHook.returnConstant(1));
                    break;
                }
            }
        } else if (maxResUnpatch != null) {
            maxResUnpatch.run();
            maxResUnpatch = null;
        }
    }
}
