/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins.pronoundb;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;

import com.aliucord.Constants;
import com.aliucord.PluginManager;
import com.aliucord.Utils;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.views.Divider;
import com.discord.views.CheckedSetting;
import com.discord.views.RadioManager;
import com.lytefast.flexinput.R$h;

import java.util.Arrays;
import java.util.Objects;

@SuppressLint("SetTextI18n")
public final class PluginSettings extends SettingsPage {
    private static final String plugin = "PronounDB";

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setActionBarTitle(plugin);
    }

    @Override
    public void onViewBound(View view) {
        super.onViewBound(view);

        var sets = Objects.requireNonNull(PluginManager.plugins.get(plugin)).sets;
        var context = view.getContext();
        var layout = (LinearLayout) ((NestedScrollView) ((CoordinatorLayout) view).getChildAt(1)).getChildAt(0);

        var appearanceHeader = new TextView(context, null, 0, R$h.UiKit_Settings_Item_Header);
        appearanceHeader.setTypeface(ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold));
        appearanceHeader.setText("Pronouns appearance");
        layout.addView(appearanceHeader);

        var radios = Arrays.asList(
            Utils.createCheckedSetting(context, CheckedSetting.ViewType.RADIO, "All lowercase", "Pronouns are showed in lowercase."),
            Utils.createCheckedSetting(context, CheckedSetting.ViewType.RADIO, "Pascal case", "First letter is uppercased.")
        );

        var radioManager = new RadioManager(radios);
        int format = sets.getInt("format", 0);

        int j = radios.size();
        for (int i = 0; i < j; i++) {
            int k = i;
            var radio = radios.get(k);
            radio.e(e -> {
                sets.setInt("format", k);
                radioManager.a(radio);
            });
            layout.addView(radio);
            if (k == format) radioManager.a(radio);
        }

        var displayChat = Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Show pronouns in chat", null);
        displayChat.setChecked(sets.getBool("displayChat", true));
        displayChat.setOnCheckedListener(c -> {
            sets.setBool("displayChat", c);
            reloadPlugin();
        });
        layout.addView(new Divider(context));
        layout.addView(displayChat);

        var displayProfile = Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Show pronouns in profiles", null);
        displayProfile.setChecked(sets.getBool("displayProfile", true));
        displayProfile.setOnCheckedListener(c -> {
            sets.setBool("displayProfile", c);
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
