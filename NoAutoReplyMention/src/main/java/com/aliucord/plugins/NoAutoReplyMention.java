/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePrePatchFn;
import com.discord.api.channel.Channel;
import com.discord.api.message.Message;

@SuppressWarnings("unused")
public class NoAutoReplyMention extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() {
        var manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Disables reply mention by default.";
        manifest.version = "0.0.3";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    @Override
    public void start(Context context) {
        patcher.patch(
            "com.discord.stores.StorePendingReplies", "onCreatePendingReply",
            new Class<?>[]{ Channel.class, Message.class, boolean.class, boolean.class },
            new PinePrePatchFn(callFrame -> {
                callFrame.args[2] = false; // mention
                callFrame.args[3] = true;  // showMentionToggle
            })
        );
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
