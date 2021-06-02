/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aliucord.Utils;
import com.aliucord.api.CommandsAPI;
import com.aliucord.entities.Plugin;
import com.aliucord.plugins.tags.*;
import com.discord.api.commands.ApplicationCommandType;
import com.discord.api.commands.CommandChoice;
import com.discord.models.commands.ApplicationCommandOption;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tags extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() {
        Manifest manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "Allows you to send custom tags.";
        manifest.version = "1.0.5";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void start(Context context) {
        existingTags = new ArrayList<>();
        subcommands = new ArrayList<>();

        ApplicationCommandOption tagName = new ApplicationCommandOption(ApplicationCommandType.STRING, "name", "Tag name",
                null, true, true, null, null);
        ApplicationCommandOption existingTag = new ApplicationCommandOption(ApplicationCommandType.STRING, "name", "Tag name",
                null, true, true, existingTags, null);

        subcommands.add(new ApplicationCommandOption(
                ApplicationCommandType.SUBCOMMAND,
                "add",
                "Create a tag",
                null,
                false,
                false,
                null,
                Arrays.asList(tagName, CommandsAPI.requiredMessageOption)
        ));
        subcommands.add(new ApplicationCommandOption(
                ApplicationCommandType.SUBCOMMAND,
                "delete",
                "Delete a tag",
                null,
                false,
                false,
                null,
                Collections.singletonList(existingTag)
        ));
        subcommands.add(new ApplicationCommandOption(
                ApplicationCommandType.SUBCOMMAND,
                "list",
                "Shows list with tag names",
                null,
                false,
                false,
                null,
                Collections.emptyList()
        ));
        subcommands.add(new ApplicationCommandOption(
                ApplicationCommandType.SUBCOMMAND,
                "rename",
                "Rename a tag",
                null,
                false,
                false,
                null,
                Arrays.asList(
                        existingTag,
                        new ApplicationCommandOption(ApplicationCommandType.STRING, "newName", "New tag name",
                                null, true, true, null, null)
                )
        ));
        subcommands.add(new ApplicationCommandOption(
                ApplicationCommandType.SUBCOMMAND,
                "update",
                "Update a tag",
                null,
                false,
                false,
                null,
                Arrays.asList(existingTag, CommandsAPI.requiredMessageOption)
        ));

        HashMap<String, String> _tags = sets.getObject("tags", null, tagsType);
        if (_tags != null) for (Map.Entry<String, String> tag : _tags.entrySet()) registerTag(tag.getKey(), tag.getValue());

        commands.registerCommand(
                "tag",
                "Send and manage tags",
                subcommands,
                args -> {
                    if (args.containsKey("add")) return AddCommand.execute((Map<String, ?>) args.get("add"), sets, this);
                    if (args.containsKey("delete")) return DeleteCommand.execute((Map<String, ?>) args.get("delete"), sets, this);
                    if (args.containsKey("list")) return ListCommand.execute(sets);
                    if (args.containsKey("rename")) return RenameCommand.execute((Map<String, ?>) args.get("rename"), sets, this);
                    if (args.containsKey("update")) return UpdateCommand.execute((Map<String, ?>) args.get("update"), sets, this);

                    HashMap<String, String> tags = sets.getObject("tags", null, tagsType);
                    if (tags != null) for (Map.Entry<String, String> tag : tags.entrySet()) {
                        if (args.containsKey(tag.getKey()))
                            return new CommandsAPI.CommandResult(runTag(tag.getValue(), (Map<String, ?>) args.get(tag.getKey())));
                    }

                    return new CommandsAPI.CommandResult();
                }
        );
    }

    @Override
    public void stop(Context context) {
        commands.unregisterAll();
    }

    public static final Type tagsType = TypeToken.getParameterized(HashMap.class, String.class, String.class).getType();

    public final Pattern argPattern = Pattern.compile("\\[(\\w+)]");
    public List<CommandChoice> existingTags;
    public List<ApplicationCommandOption> subcommands;

    public void registerTag(String name, String message) {
        ArrayList<ApplicationCommandOption> args = new ArrayList<>();
        Matcher matcher = argPattern.matcher(message);
        while (matcher.find()) {
            String argName = matcher.group(1);
            args.add(new ApplicationCommandOption(
                    ApplicationCommandType.STRING,
                    argName,
                    argName + " content",
                    null,
                    false,
                    false,
                    null,
                    null
            ));
        }

        existingTags.add(Utils.createCommandChoice(name, name));
        subcommands.add(new ApplicationCommandOption(
                ApplicationCommandType.SUBCOMMAND,
                name,
                "Tag: " + (message.length() > 150 ? message.substring(0, 150) + "…" : message),
                null,
                false,
                false,
                null,
                args
        ));
    }

    public String runTag(String value, Map<String, ?> args) {
        Matcher matcher = argPattern.matcher(value);
        while (matcher.find()) {
            String v = (String) args.get(matcher.group(1));
            value = value.replace(matcher.group(), v == null ? "" : v);
        }
        return value;
    }
}
