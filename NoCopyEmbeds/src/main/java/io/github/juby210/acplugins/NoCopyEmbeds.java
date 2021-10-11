/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins;

import android.content.Context;
import android.widget.TextView;

import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.InsteadHook;
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemEmbed$1;

@AliucordPlugin
@SuppressWarnings("unused")
public final class NoCopyEmbeds extends Plugin {
    @Override
    public void start(Context context) {
        patcher.patch(WidgetChatListAdapterItemEmbed$1.class, "invoke", new Class<?>[]{ TextView.class }, InsteadHook.DO_NOTHING);
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
