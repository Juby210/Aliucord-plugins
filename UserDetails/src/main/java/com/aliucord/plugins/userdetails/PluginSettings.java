/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins.userdetails;

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
import com.aliucord.widgets.LinearLayout;
import com.discord.app.AppBottomSheet;
import com.discord.utilities.color.ColorCompat;
import com.discord.views.CheckedSetting;
import com.lytefast.flexinput.R$b;

import java.util.Objects;

public final class PluginSettings extends AppBottomSheet {
    public int getContentViewResId() { return 0; }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        var sets = Objects.requireNonNull(PluginManager.plugins.get("UserDetails")).sets;
        var context = inflater.getContext();
        var layout = new LinearLayout(context);
        layout.setBackgroundColor(ColorCompat.getThemedColor(context, R$b.colorBackgroundPrimary));

        layout.addView(createSwitch(context, sets, "createdAt", "Display \"Created at\""));
        layout.addView(createSwitch(context, sets, "joinedAt", "Display \"Joined at\""));
        layout.addView(createSwitch(context, sets, "lastMessage", "Display \"Last message\""));
        return layout;
    }

    private CheckedSetting createSwitch(Context context, SettingsAPI sets, String key, String label) {
        var cs = Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, label, null);
        cs.setChecked(sets.getBool(key, true));
        cs.setOnCheckedListener(c -> sets.setBool(key, c));
        return cs;
    }
}
