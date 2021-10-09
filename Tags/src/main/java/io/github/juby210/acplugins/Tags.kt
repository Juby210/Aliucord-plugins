/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins

import android.content.Context
import com.aliucord.Utils.createCommandChoice
import com.aliucord.Utils.createCommandOption
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.discord.api.commands.ApplicationCommandType
import com.discord.api.commands.CommandChoice
import com.discord.models.commands.ApplicationCommandOption
import com.google.gson.reflect.TypeToken
import io.github.juby210.acplugins.tags.*
import java.lang.reflect.Type
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

@AliucordPlugin
class Tags : Plugin() {
    override fun start(context: Context) {
        val tagName = createCommandOption(
            name = "name",
            description = "Tag name",
            required = true,
            default = true
        )
        val existingTag = createCommandOption(
            name = "name",
            description = "Tag name",
            required = true,
            default = true,
            choices = existingTags
        )

        subcommands.add(createCommandOption(
            type = ApplicationCommandType.SUBCOMMAND,
            name = "add",
            description = "Create a tag",
            subCommandOptions = listOf(tagName, CommandsAPI.requiredMessageOption)
        ))
        subcommands.add(createCommandOption(
            type = ApplicationCommandType.SUBCOMMAND,
            name = "delete",
            description = "Delete a tag",
            subCommandOptions = listOf(existingTag)
        ))
        subcommands.add(createCommandOption(
            type = ApplicationCommandType.SUBCOMMAND,
            name = "list",
            description = "Shows list with tag names"
        ))
        subcommands.add(createCommandOption(
            type = ApplicationCommandType.SUBCOMMAND,
            name = "rename",
            description = "Rename a tag",
            subCommandOptions = listOf(
                existingTag,
                createCommandOption(
                    name = "newName",
                    description = "New tag name",
                    required = true,
                    default = true
                )
            )
        ))
        subcommands.add(createCommandOption(
            type = ApplicationCommandType.SUBCOMMAND,
            name = "update",
            description = "Update a tag",
            subCommandOptions = listOf(existingTag, CommandsAPI.requiredMessageOption)
        ))

        val tags = settings.getObject<HashMap<String, String>?>("tags", null, tagsType)
        if (tags != null) for ((key, value) in tags) registerTag(key, value)
        commands.registerCommand(
            "tag",
            "Send and manage tags",
            subcommands
        ) { ctx ->
            return@registerCommand if (ctx.containsArg("add")) AddCommand.execute(ctx.getSubCommandArgs("add"), settings, this)
            else if (ctx.containsArg("delete")) DeleteCommand.execute(ctx.getSubCommandArgs("delete"), settings, this)
            else if (ctx.containsArg("list")) ListCommand.execute(settings)
            else if (ctx.containsArg("rename")) RenameCommand.execute(ctx.getSubCommandArgs("rename"), settings, this)
            else if (ctx.containsArg("update")) UpdateCommand.execute(ctx.getSubCommandArgs("update"), settings, this)
            else {
                settings.getObject<HashMap<String, String>?>("tags", null, tagsType)?.run {
                    for ((key, value) in this)
                        if (ctx.containsArg(key)) return@registerCommand CommandsAPI.CommandResult(runTag(value, ctx.getSubCommandArgs(key)))
                }
                CommandsAPI.CommandResult()
            }
        }
    }

    override fun stop(context: Context) {
        commands.unregisterAll()
        existingTags.clear()
        subcommands.clear()
    }

    private val argPattern = Pattern.compile("\\[(\\w+)]")
    @JvmField
    val existingTags = ArrayList<CommandChoice>()
    @JvmField
    val subcommands = ArrayList<ApplicationCommandOption>()

    fun registerTag(name: String, message: String) {
        val args = ArrayList<ApplicationCommandOption>()
        val matcher = argPattern.matcher(message)
        while (matcher.find()) {
            val argName = matcher.group(1)
            args.add(createCommandOption(
                name = argName!!,
                description = "$argName content"
            ))
        }
        existingTags.add(createCommandChoice(name, name))
        subcommands.add(createCommandOption(
            type = ApplicationCommandType.SUBCOMMAND,
            name = name,
            description = "Tag: " + if (message.length > 150) message.substring(0, 150) + "…" else message,
            subCommandOptions = args
        ))
    }

    private fun runTag(value: String, args: Map<String?, *>?): String {
        var ret = value
        val matcher = argPattern.matcher(ret)
        while (matcher.find()) {
            val v = args!![matcher.group(1)] as String?
            ret = ret.replace(matcher.group(), v ?: "")
        }
        return ret
    }

    companion object {
        @JvmField
        val tagsType: Type = TypeToken.getParameterized(HashMap::class.java, String::class.java, String::class.java).getType()
    }
}
