/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins;

import android.annotation.SuppressLint;
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
import com.aliucord.widgets.LinearLayout;
import com.discord.app.AppBottomSheet;
import com.discord.models.user.User;
import com.discord.utilities.color.ColorCompat;
import com.discord.utilities.user.UserUtils;
import com.discord.views.CheckedSetting;
import com.discord.views.RadioManager;
import com.lytefast.flexinput.R$b;

import java.util.*;

@SuppressWarnings("unused")
public class CustomNicknameFormat extends Plugin {
    public static class PluginSettings extends AppBottomSheet {
        public int getContentViewResId() { return 0; }

        @Nullable
        @Override
        @SuppressLint("SetTextI18n")
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            Context context = inflater.getContext();
            LinearLayout layout = new LinearLayout(context);
            layout.setBackgroundColor(ColorCompat.getThemedColor(context, R$b.colorBackgroundPrimary));

            List<CheckedSetting> radios = Arrays.asList(
                    Utils.createCheckedSetting(context, CheckedSetting.ViewType.RADIO, "Nickname (Username)", null),
                    Utils.createCheckedSetting(context, CheckedSetting.ViewType.RADIO, "Nickname (Tag)", null),
                    Utils.createCheckedSetting(context, CheckedSetting.ViewType.RADIO, "Username", null),
                    Utils.createCheckedSetting(context, CheckedSetting.ViewType.RADIO, "Username (Nickname)", null)
            );

            RadioManager radioManager = new RadioManager(radios);
            SettingsAPI sets = PluginManager.plugins.get("CustomNicknameFormat").sets;
            Format format = Format.valueOf(sets.getString("format", Format.NICKNAME_USERNAME.name()));

            int j = radios.size();
            for (int i = 0; i < j; i++) {
                int k = i;
                CheckedSetting radio = radios.get(k);
                radio.e(e -> {
                    sets.setString("format", Format.values()[k].name());
                    radioManager.a(radio);
                });
                layout.addView(radio);
                if (k == format.ordinal()) radioManager.a(radio);
            }

            return layout;
        }
    }

    public CustomNicknameFormat() {
        settings = new Settings(PluginSettings.class, Settings.Type.BOTTOMSHEET);
    }

    @NonNull
    @Override
    public Manifest getManifest() {
        Manifest manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Allows you to customize nickname format.";
        manifest.version = "1.0.0";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }
    public static Map<String, List<String>> getClassesToPatch() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("com.discord.models.member.GuildMember$Companion", Collections.singletonList("getNickOrUsername"));
        return map;
    }

    @Override
    public void start(Context context) {
        patcher.patch("com.discord.models.member.GuildMember$Companion", "getNickOrUsername", (_this, args, ret) -> {
            if (args.size() != 4) return ret;

            User user = (User) args.get(0);
            String username = user.getUsername();
            if (ret.equals(username)) return ret;

            Format format = Format.valueOf(sets.getString("format", Format.NICKNAME_USERNAME.name()));
            switch (format) {
                case NICKNAME_USERNAME:
                    return ret + " (" + username + ")";
                case NICKNAME_TAG:
                    return ret + " (" + username + UserUtils.INSTANCE.getDiscriminatorWithPadding(user) + ")";
                case USERNAME:
                    return username;
                case USERNAME_NICKNAME:
                    return username + " (" + ret + ")";
            }
            return ret;
        });
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }

    public enum Format { NICKNAME_USERNAME, NICKNAME_TAG, USERNAME, USERNAME_NICKNAME }
}
