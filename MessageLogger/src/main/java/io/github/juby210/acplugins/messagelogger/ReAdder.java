/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins.messagelogger;

import com.aliucord.CollectionUtils;
import com.aliucord.api.PatcherAPI;
import com.aliucord.patcher.PreHook;
import com.discord.models.message.Message;
import com.discord.utilities.message.MessageUtils;

import java.util.*;

import io.github.juby210.acplugins.MessageLogger;

@SuppressWarnings("unchecked")
public final class ReAdder {
    private static final Comparator<Long> comparator = MessageUtils.getSORT_BY_IDS_COMPARATOR();

    private final MessageLogger _this;

    public ReAdder(MessageLogger main, PatcherAPI patcher) {
        _this = main;
        patcher.patch(
            "com.discord.stores.StoreMessagesLoader",
            "handleLoadedMessages",
            new Class<?>[]{ List.class, long.class, long.class, Long.class, Long.class },
            new PreHook(param -> {
                var messages = (List<Message>) param.args[0];
                var channelId = (Long) param.args[1];
                var jump = (long) param.args[2] != 0;
                var isBefore = param.args[3] != null;
                var isAfter = param.args[4] != null;
                var defaultLimit = messages.size() == 50;
                var hasMoreBefore = jump || defaultLimit && isBefore;
                var hasMoreAfter = jump || defaultLimit && isAfter;
                param.args[0] = reAddDeletedMessages(messages, channelId, !hasMoreAfter && !isBefore, !hasMoreBefore && !isAfter);
            })
        );
    }

    // ported from MLv2
    public List<Message> reAddDeletedMessages(List<Message> messages, Long channelId, boolean channelStart, boolean channelEnd) {
        var reAddAll = channelStart && channelEnd;
        var deletedMessages = _this.deletedMessagesRecord.get(channelId);
        if (messages == null || (messages.size() == 0 && !reAddAll) || deletedMessages == null || deletedMessages.size() == 0) return messages;
        var messageRecord = _this.messageRecord;
        var savedIDs = CollectionUtils.filter(deletedMessages, messageRecord::containsKey);
        if (savedIDs.size() == 0) return messages;
        savedIDs.sort(comparator);
        var IDs = CollectionUtils.map(messages, Message::getId);
        var lowestID = channelEnd ? 0 : IDs.get(IDs.size() - 1);
        var highestID = channelStart ? 0 : IDs.get(0);
        var lowestIDX = channelEnd ? 0 : CollectionUtils.findIndex(savedIDs, id -> id > lowestID);
        if (lowestIDX == -1) return messages;
        var highestIDX = channelStart ? savedIDs.size() - 1 : CollectionUtils.findLastIndex(savedIDs, id -> id < highestID);
        if (highestIDX == -1) return messages;

        var reAddIDs = savedIDs.subList(lowestIDX, highestIDX + 1);
        reAddIDs.addAll(IDs);
        reAddIDs.sort(comparator);
        if (!(messages instanceof ArrayList)) messages = new ArrayList<>(messages);
        var len = reAddIDs.size();
        for (var i = 0; i < len; i++) {
            var id = reAddIDs.get(i);
            if (!CollectionUtils.some(messages, m -> m.getId() == id)) //noinspection ConstantConditions
                messages.add(i, messageRecord.get(id).message);
        }
        return messages;
    }
}
