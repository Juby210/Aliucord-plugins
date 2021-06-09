/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins.tags;

import com.aliucord.api.CommandsAPI;
import com.aliucord.api.SettingsAPI;
import com.aliucord.entities.MessageEmbedBuilder;
import com.aliucord.plugins.Tags;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class AddCommand {
    public static CommandsAPI.CommandResult execute(Map<String, ?> args, SettingsAPI sets, Tags main) {
        var name = (String) args.get("name");
        if (name != null) name = name.replaceAll(" ", "");
        var msg = (String) args.get("message");

        var embed = new MessageEmbedBuilder();
        if (name == null || name.equals("") || msg == null || msg.equals("")) {
            embed.setTitle("Missing required arguments");
        } else {
            HashMap<String, String> tags = sets.getObject("tags", new HashMap<>(), Tags.tagsType);
            if (tags.containsKey(name)) {
                embed.setTitle("Tag already declared");
            } else {
                embed
                    .setTitle("Successfully created tag")
                    .setColor(0xFF00FF00)
                    .addField("Name", name, false)
                    .addField("Value", msg, false);
                tags.put(name, msg);
                sets.setObject("tags", tags);
                main.registerTag(name, msg);
            }
        }

        return new CommandsAPI.CommandResult(null, Collections.singletonList(embed.build()), false);
    }
}
