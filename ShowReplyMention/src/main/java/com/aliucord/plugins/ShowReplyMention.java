/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import com.aliucord.CollectionUtils;
import com.aliucord.Main;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePrePatchFn;
import com.discord.models.member.GuildMember;
import com.discord.models.user.CoreUser;
import com.discord.models.user.User;
import com.discord.stores.StoreMessageReplies;
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage;
import com.discord.widgets.chat.list.entries.MessageEntry;

import top.canyie.pine.callback.MethodReplacement;

@SuppressLint("SetTextI18n")
@SuppressWarnings("unused")
public class ShowReplyMention extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() {
        var manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Fixes showing `@` in replies.";
        manifest.version = "1.0.0";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    @Override
    public void start(Context context) throws Throwable {
        var c = WidgetChatListAdapterItemMessage.class;
        var configureReplyAvatar = c.getDeclaredMethod("configureReplyAvatar", User.class, GuildMember.class);
        configureReplyAvatar.setAccessible(true);
        var configureReplyName = c.getDeclaredMethod("configureReplyName", String.class, int.class, boolean.class);
        configureReplyName.setAccessible(true);
        var getAuthorTextColor = c.getDeclaredMethod("getAuthorTextColor", GuildMember.class);
        getAuthorTextColor.setAccessible(true);
        var replyHolder = c.getDeclaredField("replyHolder");
        replyHolder.setAccessible(true);
        var replyLinkItem = c.getDeclaredField("replyLinkItem");
        replyLinkItem.setAccessible(true);

        patcher.patch(c, "configureReplyPreview", new Class<?>[]{ MessageEntry.class }, new PinePrePatchFn(callFrame -> {
            try {
                if (replyHolder.get(callFrame.thisObject) == null || replyLinkItem.get(callFrame.thisObject) == null) return;
                var messageEntry = (MessageEntry) callFrame.args[0];
                var replyData = messageEntry.getReplyData();
                if (replyData == null || !(replyData.getMessageState() instanceof StoreMessageReplies.MessageState.Loaded)) return;
                var refEntry = replyData.getMessageEntry();
                var refAuthor = new CoreUser(refEntry.getMessage().getAuthor());
                var refAuthorMember = refEntry.getAuthor();
                configureReplyAvatar.invoke(callFrame.thisObject, refAuthor, refAuthorMember);
                var refAuthorId = refAuthor.getId();
                var name = refEntry.getNickOrUsernames().get(refAuthorId);
                configureReplyName.invoke(
                    callFrame.thisObject,
                    name == null ? refAuthor.getUsername() : name,
                    getAuthorTextColor.invoke(callFrame.thisObject, refAuthorMember),
                    CollectionUtils.some(messageEntry.getMessage().getMentions(), u -> new CoreUser(u).getId() == refAuthorId)
                );
            } catch (Throwable e) {
                Main.logger.error(e);
            }
        }));

        patcher.patch(c, "configureReplyAuthor", new Class<?>[]{ User.class, GuildMember.class, MessageEntry.class }, MethodReplacement.DO_NOTHING);
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
