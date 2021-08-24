/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins

import android.content.Context
import com.aliucord.api.CommandsAPI
import com.aliucord.api.CommandsAPI.CommandResult
import com.aliucord.entities.Plugin
import com.lytefast.flexinput.model.Attachment

@Suppress("unused")
class MoreSlashCommands : Plugin() {
  override fun getManifest() = Manifest().apply {
    authors = arrayOf(Manifest.Author("Juby210", 324622488644616195L))
    description = "Adds more slash commands."
    version = "0.0.5"
    updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json"
  }

  override fun start(context: Context?) {
    commands.registerCommand("lenny", "Appends ( ͡° ͜ʖ ͡°) to your message.", listOf(CommandsAPI.messageOption)) { ctx ->
      CommandResult(ctx.getStringOrDefault("message", "") + " ( ͡° ͜ʖ ͡°)") }

    commands.registerCommand("mock", "Mock a user", listOf(CommandsAPI.requiredMessageOption)) { ctx ->
      CommandResult(ctx
        .getRequiredString("message")
        .toCharArray()
        .mapIndexed { i, c -> if (i % 2 == 1) c.uppercaseChar() else c.lowercaseChar() }
        .joinToString(""))
    }

    commands.registerCommand("upper", "Makes text uppercase", listOf(CommandsAPI.requiredMessageOption)) { ctx ->
      CommandResult(ctx.getRequiredString("message").trim().uppercase()) }

    commands.registerCommand("lower", "Makes text lowercase", listOf(CommandsAPI.requiredMessageOption)) { ctx ->
      CommandResult(ctx.getRequiredString("message").trim().lowercase()) }

    commands.registerCommand("owo", "Owoify's your text", listOf(CommandsAPI.requiredMessageOption)) { ctx ->
      CommandResult(owoify(ctx.getRequiredString("message").trim())) }

    val displayName = Attachment::class.java.getDeclaredField("displayName").apply { isAccessible = true }
    commands.registerCommand("spoilerfiles", "Marks attachments as spoilers", listOf(CommandsAPI.messageOption)) { ctx ->
      for (a in ctx.attachments) displayName[a] = "SPOILER_" + a.displayName
      CommandResult(ctx.getStringOrDefault("message", ""))
    }
  }

  override fun stop(context: Context?) = commands.unregisterAll()

  private fun owoify(text: String): String {
    return text.replace("l", "w").replace("L", "W")
      .replace("r", "w").replace("R", "W")
      .replace("o", "u").replace("O", "U")
  }
}
