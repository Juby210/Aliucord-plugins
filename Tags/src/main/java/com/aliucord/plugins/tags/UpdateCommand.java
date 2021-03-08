package com.aliucord.plugins.tags;

import com.aliucord.CollectionUtils;
import com.aliucord.api.CommandsAPI;
import com.aliucord.api.SettingsAPI;
import com.aliucord.entities.MessageEmbed;
import com.aliucord.plugins.Tags;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UpdateCommand {
    public static CommandsAPI.CommandResult execute(Map<String, ?> args, SettingsAPI sets, Tags main) {
        String name = (String) args.get("name");
        String msg = (String) args.get("message");

        MessageEmbed embed = new MessageEmbed();
        if (name == null || name.equals("") || msg == null || msg.equals("")) {
            embed.setTitle("Missing required arguments");
        } else {
            embed.setTitle("Successfully updated tag");
            embed.setColor(0xFF00FF00);
            embed.setFields(Arrays.asList(new MessageEmbed.Field("Name", name, false), new MessageEmbed.Field("Value", msg, false)));
            HashMap<String, String> tags = sets.getObject("tags", new HashMap<>(), Tags.tagsType);
            tags.put(name, msg);
            sets.setObject("tags", tags);
            CollectionUtils.removeIf(main.existingTags, tag -> tag.a().equals(name));
            CollectionUtils.removeIf(main.subcommands, option -> option.getName().equals(name));
            main.registerTag(name, msg);
        }

        return new CommandsAPI.CommandResult(null, Collections.singletonList(embed), false);
    }
}
