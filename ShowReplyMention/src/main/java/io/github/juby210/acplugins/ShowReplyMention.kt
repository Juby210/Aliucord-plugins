/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins

import android.annotation.SuppressLint
import android.content.Context
import com.aliucord.Main
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.InsteadHook
import com.aliucord.patcher.PreHook
import com.discord.models.member.GuildMember
import com.discord.models.user.CoreUser
import com.discord.models.user.User
import com.discord.stores.StoreMessageReplies
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.MessageEntry

@AliucordPlugin
@Suppress("unused")
@SuppressLint("SetTextI18n")
class ShowReplyMention : Plugin() {
    override fun start(context: Context?) {
        val c = WidgetChatListAdapterItemMessage::class.java
        val configureReplyAvatar = c.getDeclaredMethod("configureReplyAvatar", User::class.java,
            GuildMember::class.java).apply { isAccessible = true }
        val configureReplyName = c.getDeclaredMethod("configureReplyName", String::class.java,
            Int::class.javaPrimitiveType, Boolean::class.javaPrimitiveType).apply { isAccessible = true }
        val getAuthorTextColor = c.getDeclaredMethod("getAuthorTextColor", GuildMember::class.java).apply {
            isAccessible = true }
        val replyHolder = c.getDeclaredField("replyHolder").apply { isAccessible = true }
        val replyLinkItem = c.getDeclaredField("replyLinkItem").apply { isAccessible = true }

        patcher.patch(c, "configureReplyPreview", arrayOf(MessageEntry::class.java), PreHook {
            try {
                if (replyHolder[it.thisObject] == null || replyLinkItem[it.thisObject] == null) return@PreHook
                val messageEntry = it.args[0] as MessageEntry
                val replyData = messageEntry.replyData
                if (replyData == null || replyData.messageState !is StoreMessageReplies.MessageState.Loaded) return@PreHook
                val refEntry = replyData.messageEntry
                val refAuthor = CoreUser(refEntry.message.author)
                val refAuthorMember = refEntry.author
                configureReplyAvatar.invoke(it.thisObject, refAuthor, refAuthorMember)
                val refAuthorId = refAuthor.id
                configureReplyName.invoke(
                    it.thisObject,
                    refEntry.nickOrUsernames[refAuthorId] ?: refAuthor.username,
                    getAuthorTextColor.invoke(it.thisObject, refAuthorMember),
                    messageEntry.message.mentions.any { u -> CoreUser(u).id == refAuthorId }
                )
            } catch (e: Throwable) {
                Main.logger.error(e)
            }
        })

        patcher.patch(
            c,
            "configureReplyAuthor",
            arrayOf(User::class.java, GuildMember::class.java, MessageEntry::class.java),
            InsteadHook.DO_NOTHING
        )
    }

    override fun stop(context: Context?) = patcher.unpatchAll()
}
