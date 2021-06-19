/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.aliucord.entities.Plugin;

import top.canyie.pine.callback.MethodReplacement;

@SuppressWarnings("unused")
public class NoCopyEmbeds extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() {
        var manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Disables copying text from embeds.";
        manifest.version = "1.0.3";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    @Override
    public void start(Context context) {
        patcher.patch("com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemEmbed$1", "invoke", new Class<?>[]{ TextView.class }, MethodReplacement.DO_NOTHING);
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
