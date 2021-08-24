/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins

import android.annotation.SuppressLint
import android.content.Context
import com.aliucord.Main
import com.aliucord.entities.Plugin
import com.aliucord.patcher.PinePrePatchFn
import com.discord.models.member.GuildMember
import com.discord.models.user.CoreUser
import com.discord.models.user.User
import com.discord.stores.StoreMessageReplies
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.MessageEntry
import top.canyie.pine.callback.MethodReplacement

@Suppress("unused")
@SuppressLint("SetTextI18n")
class ShowReplyMention : Plugin() {
  override fun getManifest() = Manifest().apply {
    authors = arrayOf(Manifest.Author("Juby210", 324622488644616195L))
    description = "Fixes showing `@` in replies."
    version = "1.0.0"
    updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json"
  }

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

    patcher.patch(c, "configureReplyPreview", arrayOf(MessageEntry::class.java), PinePrePatchFn {
      try {
        if (replyHolder[it.thisObject] == null || replyLinkItem[it.thisObject] == null) return@PinePrePatchFn
        val messageEntry = it.args[0] as MessageEntry
        val replyData = messageEntry.replyData
        if (replyData == null || replyData.messageState !is StoreMessageReplies.MessageState.Loaded) return@PinePrePatchFn
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
      } catch (e: Throwable) { Main.logger.error(e) }
    })

    patcher.patch(
      c,
      "configureReplyAuthor",
      arrayOf(com.discord.models.user.User::class.java, GuildMember::class.java, MessageEntry::class.java),
      MethodReplacement.DO_NOTHING
    )
  }

  override fun stop(context: Context?) = patcher.unpatchAll()
}
