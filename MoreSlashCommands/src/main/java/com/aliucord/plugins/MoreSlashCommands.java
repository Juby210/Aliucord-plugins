package com.aliucord.plugins;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aliucord.api.CommandsAPI;
import com.aliucord.entities.Plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class MoreSlashCommands extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() {
        Manifest manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Adds more slash commands.";
        manifest.version = "0.0.1";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }
    public static Map<String, List<String>> getClassesToPatch() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("com.discord.utilities.attachments.AttachmentUtilsKt", Collections.singletonList("getSanitizedFileName"));
        map.put("com.discord.widgets.chat.MessageManager$sendMessage$2", Collections.singletonList("invoke"));
        return map;
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
                    String msg = (String) args.get("message");
                    return new CommandsAPI.CommandResult((msg == null ? "" : msg) + " ( ͡° ͜ʖ ͡°)");
                }
        );

        commands.registerCommand(
                "mock",
                "Mock a user",
                Collections.singletonList(CommandsAPI.requiredMessageOption),
                args -> {
                    String msg = (String) args.get("message");
                    if (msg == null) return new CommandsAPI.CommandResult(msg);
                    StringBuilder newMsg = new StringBuilder(msg.trim());
                    int j = newMsg.length();
                    for (int i = 1; i < j; i += 2) newMsg.setCharAt(i, Character.toUpperCase(newMsg.charAt(i)));
                    return new CommandsAPI.CommandResult(newMsg.toString());
                }
        );

        // spilerfiles command
        patcher.patch("com.discord.widgets.chat.MessageManager$sendMessage$2", "invoke", (_this, args, ret) -> {
            markAttachmentsAsSpoiler = false;
            return ret;
        });

        patcher.patch("com.discord.utilities.attachments.AttachmentUtilsKt", "getSanitizedFileName",
                (_this, args, ret) -> markAttachmentsAsSpoiler && markAttachmentsAsSpoilerTime - 100 < System.currentTimeMillis() ? "SPOILER_" + ret : ret);

        commands.registerCommand(
                "spoilerfiles",
                "Marks attachments as spoilers",
                Collections.singletonList(CommandsAPI.messageOption),
                args -> {
                    markAttachmentsAsSpoiler = true;
                    markAttachmentsAsSpoilerTime = System.currentTimeMillis();
                    return new CommandsAPI.CommandResult((String) args.get("message"));
                }
        );
    }

    @Override
    public void stop(Context context) {
        commands.unregisterAll();
        patcher.unpatchAll();
    }
}
