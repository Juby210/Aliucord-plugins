/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins.messagelogger;

import com.discord.models.message.Message;

import java.util.ArrayList;

public final class MessageRecord {
    public static final class DeleteData {
        public long time;

        public DeleteData(long time) {
            this.time = time;
        }
    }

    public static final class EditHistory {
        public String content;
        public long time;

        public EditHistory(String content, long time) {
            this.content = content;
            this.time = time;
        }
    }

    public Message message;
    public DeleteData deleteData;
    public ArrayList<EditHistory> editHistory = new ArrayList<>();
}
