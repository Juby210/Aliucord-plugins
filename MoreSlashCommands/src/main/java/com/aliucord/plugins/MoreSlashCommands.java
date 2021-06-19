/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.aliucord.api.CommandsAPI;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePatchFn;

import java.util.Collections;

@SuppressWarnings("unused")
public class MoreSlashCommands extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() {
        var manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Adds more slash commands.";
        manifest.version = "0.0.4";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    public static boolean markAttachmentsAsSpoiler = false;
    public static long markAttachmentsAsSpoilerTime = 0;

    @Override
    public void start(Context context) {
        commands.registerCommand(
            "lenny",
            "Appends ( ͡° ͜ʖ ͡°) to your message.",
            Collections.singletonList(CommandsAPI.messageOption),
            args -> {
                var msg = (String) args.get("message");
                return new CommandsAPI.CommandResult((msg == null ? "" : msg) + " ( ͡° ͜ʖ ͡°)");
            }
        );

        commands.registerCommand(
            "mock",
            "Mock a user",
            Collections.singletonList(CommandsAPI.requiredMessageOption),
            args -> {
                var msg = (String) args.get("message");
                if (msg == null) return new CommandsAPI.CommandResult(msg);
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
            args -> {
                var msg = (String) args.get("message");
                if (msg == null) return new CommandsAPI.CommandResult(msg);
                return new CommandsAPI.CommandResult(msg.trim().toUpperCase());
            }
        );

        commands.registerCommand(
            "lower",
            "Makes text lowercase",
            Collections.singletonList(CommandsAPI.requiredMessageOption),
            args -> {
                var msg = (String) args.get("message");
                if (msg == null) return new CommandsAPI.CommandResult(msg);
                return new CommandsAPI.CommandResult(msg.trim().toLowerCase());
            }
        );

        commands.registerCommand(
            "owo",
            "Owoify's your text",
            Collections.singletonList(CommandsAPI.requiredMessageOption),
            args -> {
                var msg = (String) args.get("message");
                if (msg == null) return new CommandsAPI.CommandResult(msg);
                return new CommandsAPI.CommandResult(owoify(msg.trim()));
            }
        );

        // spoilerfiles command
        patcher.patch("com.discord.widgets.chat.MessageManager$sendMessage$2", "invoke", new Class<?>[]{ Object.class },
            new PinePatchFn(callFrame -> markAttachmentsAsSpoiler = false));

        patcher.patch("com.discord.utilities.attachments.AttachmentUtilsKt", "getSanitizedFileName", new Class<?>[]{ String.class, Bitmap.CompressFormat.class },
            new PinePatchFn(callFrame -> {
                if (markAttachmentsAsSpoiler && markAttachmentsAsSpoilerTime - 100 < System.currentTimeMillis()) callFrame.setResult("SPOILER_" + callFrame.getResult());
            })
        );

        commands.registerCommand(
            "spoilerfiles",
            "Marks attachments as spoilers",
            Collections.singletonList(CommandsAPI.messageOption),
            args -> {
                markAttachmentsAsSpoiler = true;
                markAttachmentsAsSpoilerTime = System.currentTimeMillis();
                var msg = (String) args.get("message");
                return new CommandsAPI.CommandResult(msg == null ? "" : msg);
            }
        );
    }

    @Override
    public void stop(Context context) {
        commands.unregisterAll();
        patcher.unpatchAll();
    }

    public String owoify(final String text) {
        return text.replace("l", "w").replace("L", "W")
            .replace("r", "w").replace("R", "W")
            .replace("o", "u").replace("O", "U");
    }
}
