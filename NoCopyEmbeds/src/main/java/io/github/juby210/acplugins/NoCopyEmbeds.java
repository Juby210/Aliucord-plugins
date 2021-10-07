/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins;

import android.content.Context;
import android.widget.TextView;

import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;

import top.canyie.pine.callback.MethodReplacement;

@AliucordPlugin
@SuppressWarnings("unused")
public class NoCopyEmbeds extends Plugin {
    @Override
    public void start(Context context) {
        patcher.patch("com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemEmbed$1", "invoke", new Class<?>[]{ TextView.class }, MethodReplacement.DO_NOTHING);
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
