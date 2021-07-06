/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aliucord.Main;
import com.aliucord.api.CommandsAPI;
import com.aliucord.entities.Plugin;
import com.aliucord.utils.ReflectUtils;
import com.lytefast.flexinput.model.Attachment;

import java.util.Collections;

@SuppressWarnings("unused")
public class MoreSlashCommands extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() {
        var manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Adds more slash commands.";
        manifest.version = "0.0.5";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    @Override
    public void start(Context context) {
        commands.registerCommand(
            "lenny",
            "Appends ( ͡° ͜ʖ ͡°) to your message.",
            Collections.singletonList(CommandsAPI.messageOption),
            ctx -> {
                var msg = ctx.getString("message");
                return new CommandsAPI.CommandResult((msg == null ? "" : msg) + " ( ͡° ͜ʖ ͡°)");
            }
        );

        commands.registerCommand(
            "mock",
            "Mock a user",
            Collections.singletonList(CommandsAPI.requiredMessageOption),
            ctx -> {
                var msg = ctx.getRequiredString("message");
                var newMsg = new StringBuilder(msg.trim());
                var j = newMsg.length();
                for (var i = 1; i < j; i += 2) newMsg.setCharAt(i, Character.toUpperCase(newMsg.charAt(i)));
                return new CommandsAPI.CommandResult(newMsg.toString());
            }
        );

        commands.registerCommand(
            "upper",
            "Makes text uppercase",
            Collections.singletonList(CommandsAPI.requiredMessageOption),
            ctx -> {
                var msg = ctx.getRequiredString("message");
                return new CommandsAPI.CommandResult(msg.trim().toUpperCase());
            }
        );

        commands.registerCommand(
            "lower",
            "Makes text lowercase",
            Collections.singletonList(CommandsAPI.requiredMessageOption),
            ctx -> {
                var msg = ctx.getRequiredString("message");
                return new CommandsAPI.CommandResult(msg.trim().toLowerCase());
            }
        );

        commands.registerCommand(
            "owo",
            "Owoify's your text",
            Collections.singletonList(CommandsAPI.requiredMessageOption),
            ctx -> {
                var msg = ctx.getRequiredString("message");
                return new CommandsAPI.CommandResult(owoify(msg.trim()));
            }
        );

        commands.registerCommand(
            "spoilerfiles",
            "Marks attachments as spoilers",
            Collections.singletonList(CommandsAPI.messageOption),
            ctx -> {
                var c = Attachment.class;
                try {
                    for (var a : ctx.getAttachments())
                        ReflectUtils.setField(c, a, "displayName", "SPOILER_" + a.getDisplayName(), true);
                } catch (Throwable e) { Main.logger.error(e); }
                return new CommandsAPI.CommandResult(ctx.getStringOrDefault("message", ""));
            }
        );
    }

    @Override
    public void stop(Context context) {
        commands.unregisterAll();
    }

    public String owoify(final String text) {
        return text.replace("l", "w").replace("L", "W")
            .replace("r", "w").replace("R", "W")
            .replace("o", "u").replace("O", "U");
    }
}
