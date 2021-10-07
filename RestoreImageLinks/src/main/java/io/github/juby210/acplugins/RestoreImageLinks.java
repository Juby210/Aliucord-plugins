/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

// based on https://gitdab.com/distok/cutthecord/src/branch/master/patches/embedlinks/1387.patch

package io.github.juby210.acplugins;

import android.content.Context;

import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.discord.utilities.textprocessing.MessagePreprocessor;

import java.util.Collection;

import top.canyie.pine.callback.MethodReplacement;

@AliucordPlugin
@SuppressWarnings("unused")
public class RestoreImageLinks extends Plugin {
    @Override
    public void start(Context context) {
        patcher.patch(MessagePreprocessor.class, "stripSimpleEmbedLink", new Class<?>[]{ Collection.class }, MethodReplacement.DO_NOTHING);
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
