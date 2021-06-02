/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins.tags;

import com.aliucord.api.CommandsAPI;
import com.aliucord.api.SettingsAPI;
import com.aliucord.entities.MessageEmbed;
import com.aliucord.plugins.Tags;

import java.util.Collections;
import java.util.HashMap;

public final class ListCommand {
    public static CommandsAPI.CommandResult execute(SettingsAPI sets) {
        HashMap<String, String> tags = sets.getObject("tags", null, Tags.tagsType);

        MessageEmbed embed = new MessageEmbed();
        if (tags == null || tags.size() == 0) embed.setTitle("You don't have any tags declared");
        else {
            int size = tags.size();
            embed.setTitle("You have " + size + " tag" + (size == 1 ? "" : "s") + " available:");
            StringBuilder description = new StringBuilder();
            for (String tag : tags.keySet()) {
                if (description.length() > 0) description.append("\n");
                description.append("- ").append(tag);
            }
            embed.setDescription(description.toString());
        }

        return new CommandsAPI.CommandResult(null, Collections.singletonList(embed.embed), false);
    }
}
