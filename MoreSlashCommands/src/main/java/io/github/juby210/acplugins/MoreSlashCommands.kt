/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins

import android.content.Context
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.lytefast.flexinput.model.Attachment

@AliucordPlugin
@Suppress("unused")
class MoreSlashCommands : Plugin() {
  override fun start(context: Context?) {
    commands.registerCommand("lenny", "Appends ( ͡° ͜ʖ ͡°) to your message.", listOf(CommandsAPI.messageOption)) { ctx ->
        CommandsAPI.CommandResult(ctx.getStringOrDefault("message", "") + " ( ͡° ͜ʖ ͡°)")
    }

    commands.registerCommand("mock", "Mock a user", listOf(CommandsAPI.requiredMessageOption)) { ctx ->
        CommandsAPI.CommandResult(ctx
            .getRequiredString("message")
            .toCharArray()
            .mapIndexed { i, c -> if (i % 2 == 1) c.uppercaseChar() else c.lowercaseChar() }
            .joinToString(""))
    }

    commands.registerCommand("upper", "Makes text uppercase", listOf(CommandsAPI.requiredMessageOption)) { ctx ->
        CommandsAPI.CommandResult(ctx.getRequiredString("message").trim().uppercase())
    }

    commands.registerCommand("lower", "Makes text lowercase", listOf(CommandsAPI.requiredMessageOption)) { ctx ->
        CommandsAPI.CommandResult(ctx.getRequiredString("message").trim().lowercase())
    }

    commands.registerCommand("owo", "Owoify's your text", listOf(CommandsAPI.requiredMessageOption)) { ctx ->
        CommandsAPI.CommandResult(owoify(ctx.getRequiredString("message").trim()))
    }

    val displayName = Attachment::class.java.getDeclaredField("displayName").apply { isAccessible = true }
    commands.registerCommand("spoilerfiles", "Marks attachments as spoilers", listOf(CommandsAPI.messageOption)) { ctx ->
      for (a in ctx.attachments) displayName[a] = "SPOILER_" + a.displayName
        CommandsAPI.CommandResult(ctx.getStringOrDefault("message", ""))
    }

    commands.registerCommand("reverse", "Makes text reversed", listOf(CommandsAPI.requiredMessageOption)) { ctx ->
        CommandsAPI.CommandResult(ctx.getRequiredString("message").reversed())
    }
  }

  override fun stop(context: Context?) = commands.unregisterAll()

  private fun owoify(text: String): String {
    return text.replace("l", "w").replace("L", "W")
      .replace("r", "w").replace("R", "W")
      .replace("o", "u").replace("O", "U")
  }
}
