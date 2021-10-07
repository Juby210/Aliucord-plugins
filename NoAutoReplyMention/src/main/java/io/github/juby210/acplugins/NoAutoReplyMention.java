/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins;

import android.content.Context;

import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePrePatchFn;
import com.discord.api.channel.Channel;
import com.discord.models.message.Message;

@AliucordPlugin
@SuppressWarnings("unused")
public class NoAutoReplyMention extends Plugin {
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
