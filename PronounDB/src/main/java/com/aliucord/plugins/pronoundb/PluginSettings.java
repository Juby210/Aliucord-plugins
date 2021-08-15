/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins.pronoundb;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.aliucord.Constants;
import com.aliucord.*;
import com.aliucord.api.SettingsAPI;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.views.Divider;
import com.discord.views.CheckedSetting;
import com.discord.views.RadioManager;
import com.lytefast.flexinput.R;

import java.util.Arrays;

@SuppressLint("SetTextI18n")
public final class PluginSettings extends SettingsPage {
    private static final String plugin = "PronounDB";

    private final SettingsAPI settings;
    public PluginSettings(SettingsAPI settings) {
        this.settings = settings;
    }

    @Override
    public void onViewBound(View view) {
        super.onViewBound(view);
        setActionBarTitle(plugin);
        setPadding(0);

        var context = view.getContext();
        var layout = getLinearLayout();

        var appearanceHeader = new TextView(context, null, 0, R.h.UiKit_Settings_Item_Header);
        appearanceHeader.setTypeface(ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold));
        appearanceHeader.setText("Pronouns appearance");
        layout.addView(appearanceHeader);

        var radios = Arrays.asList(
            Utils.createCheckedSetting(context, CheckedSetting.ViewType.RADIO, "All lowercase", "Pronouns are showed in lowercase."),
            Utils.createCheckedSetting(context, CheckedSetting.ViewType.RADIO, "Pascal case", "First letter is uppercased.")
        );

        var radioManager = new RadioManager(radios);
        int format = settings.getInt("format", 0);

        int j = radios.size();
        for (int i = 0; i < j; i++) {
            int k = i;
            var radio = radios.get(k);
            radio.e(e -> {
                settings.setInt("format", k);
                radioManager.a(radio);
            });
            layout.addView(radio);
            if (k == format) radioManager.a(radio);
        }

        var displayChat = Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Show pronouns in chat", null);
        displayChat.setChecked(settings.getBool("displayChat", true));
        displayChat.setOnCheckedListener(c -> {
            settings.setBool("displayChat", c);
            reloadPlugin();
        });
        layout.addView(new Divider(context));
        layout.addView(displayChat);

        var displayProfile = Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Show pronouns in profiles", null);
        displayProfile.setChecked(settings.getBool("displayProfile", true));
        displayProfile.setOnCheckedListener(c -> {
            settings.setBool("displayProfile", c);
            reloadPlugin();
        });
        layout.addView(new Divider(context));
        layout.addView(displayProfile);
    }

    public void reloadPlugin() {
        PluginManager.stopPlugin(plugin);
        PluginManager.startPlugin(plugin);
    }
}
