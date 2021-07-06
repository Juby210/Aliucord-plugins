/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliucord.Utils;
import com.aliucord.api.SettingsAPI;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePatchFn;
import com.aliucord.widgets.LinearLayout;
import com.discord.api.channel.Channel;
import com.discord.app.AppBottomSheet;
import com.discord.models.member.GuildMember;
import com.discord.models.user.User;
import com.discord.utilities.color.ColorCompat;
import com.discord.utilities.user.UserUtils;
import com.discord.views.CheckedSetting;
import com.discord.views.RadioManager;
import com.lytefast.flexinput.R$b;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class CustomNicknameFormat extends Plugin {
    public CustomNicknameFormat() {
        settingsTab = new SettingsTab(PluginSettings.class, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings);
    }

    public static class PluginSettings extends AppBottomSheet {
        public int getContentViewResId() { return 0; }

        private final SettingsAPI settings;
        public PluginSettings(SettingsAPI settings) {
            this.settings = settings;
        }

        @Nullable
        @Override
        @SuppressLint("SetTextI18n")
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            var context = inflater.getContext();
            var layout = new LinearLayout(context);
            layout.setBackgroundColor(ColorCompat.getThemedColor(context, R$b.colorBackgroundPrimary));

            var radios = Arrays.asList(
                    Utils.createCheckedSetting(context, CheckedSetting.ViewType.RADIO, "Nickname (Username)", null),
                    Utils.createCheckedSetting(context, CheckedSetting.ViewType.RADIO, "Nickname (Tag)", null),
                    Utils.createCheckedSetting(context, CheckedSetting.ViewType.RADIO, "Username", null),
                    Utils.createCheckedSetting(context, CheckedSetting.ViewType.RADIO, "Username (Nickname)", null)
            );

            var radioManager = new RadioManager(radios);
            var format = Format.valueOf(settings.getString("format", Format.NICKNAME_USERNAME.name()));

            var j = radios.size();
            for (var i = 0; i < j; i++) {
                var k = i;
                var radio = radios.get(k);
                radio.e(e -> {
                    settings.setString("format", Format.values()[k].name());
                    radioManager.a(radio);
                });
                layout.addView(radio);
                if (k == format.ordinal()) radioManager.a(radio);
            }

            return layout;
        }
    }

    public enum Format { NICKNAME_USERNAME, NICKNAME_TAG, USERNAME, USERNAME_NICKNAME }

    @NonNull
    @Override
    public Manifest getManifest() {
        var manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Allows you to customize nickname format.";
        manifest.version = "1.0.2";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    @Override
    public void start(Context context) {
        patcher.patch(GuildMember.Companion.getClass(), "getNickOrUsername", new Class<?>[]{ User.class, GuildMember.class, Channel.class, List.class },
            new PinePatchFn(callFrame -> {
                var user = (User) callFrame.args[0];
                var username = user.getUsername();
                var res = (String) callFrame.getResult();
                if (res.equals(username)) return;

                callFrame.setResult(getFormatted(username, res, user));
            }
        ));
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }

    private String getFormatted(String username, String res, User user) {
        var format = Format.valueOf(settings.getString("format", Format.NICKNAME_USERNAME.name()));
        switch (format) {
            case NICKNAME_USERNAME:
                return res + " (" + username + ")";
            case NICKNAME_TAG:
                return res + " (" + username + UserUtils.INSTANCE.getDiscriminatorWithPadding(user) + ")";
            case USERNAME:
                return username;
            case USERNAME_NICKNAME:
                return username + " (" + res + ")";
        }
        return res;
    }
}
