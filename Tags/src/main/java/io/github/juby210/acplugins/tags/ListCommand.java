/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins.tags;

import com.aliucord.api.CommandsAPI;
import com.aliucord.api.SettingsAPI;
import com.aliucord.entities.MessageEmbedBuilder;
import io.github.juby210.acplugins.Tags;

import java.util.Collections;
import java.util.HashMap;

public final class ListCommand {
    public static CommandsAPI.CommandResult execute(SettingsAPI sets) {
        HashMap<String, String> tags = sets.getObject("tags", null, Tags.tagsType);

        var embed = new MessageEmbedBuilder();
        if (tags == null || tags.size() == 0) embed.setTitle("You don't have any tags declared");
        else {
            var size = tags.size();
            embed.setTitle("You have " + size + " tag" + (size == 1 ? "" : "s") + " available:");
            var description = new StringBuilder();
            for (String tag : tags.keySet()) {
                if (description.length() > 0) description.append("\n");
                description.append("- ").append(tag);
            }
            embed.setDescription(description.toString());
        }

        return new CommandsAPI.CommandResult(null, Collections.singletonList(embed.build()), false);
    }
}
