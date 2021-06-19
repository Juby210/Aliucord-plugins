/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

// based on https://gitdab.com/distok/cutthecord/src/branch/master/patches/hideunusableemojis/1371.patch

package com.aliucord.plugins;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePrePatchFn;
import com.discord.models.domain.emoji.Emoji;

import java.util.*;

import kotlin.jvm.functions.Function1;

@SuppressWarnings({"unchecked", "unused"})
public class HideDisabledEmojis extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() {
        var manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Hides disabled emojis in emoji picker and autocomplete.";
        manifest.version = "1.0.2";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    @Override
    public void start(Context context) {
        patcher.patch(
            "com.discord.widgets.chat.input.emoji.EmojiPickerViewModel$Companion", "buildEmojiListItems",
            new Class<?>[]{ Collection.class, Function1.class, String.class, boolean.class, boolean.class, boolean.class },
            new PinePrePatchFn(callFrame -> {
                var emojis = (Collection<? extends Emoji>) callFrame.args[0];
                if (!(emojis instanceof ArrayList)) {
                    emojis = new ArrayList<>(emojis);
                    callFrame.args[0] = emojis;
                }
                emojis.removeIf(e -> !e.isUsable());
            })
        );
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
