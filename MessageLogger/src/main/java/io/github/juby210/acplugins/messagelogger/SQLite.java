/*
 * Copyright (c) 2021-2022 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins.messagelogger;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.aliucord.Utils;
import com.aliucord.wrappers.ChannelWrapper;
import com.discord.api.channel.Channel;
import com.discord.models.deserialization.gson.InboundGatewayGsonParser;

import java.io.File;
import java.io.IOException;

public final class SQLite extends SQLiteOpenHelper {

    private static final String DB_NAME = "message_logger.db";

    private static final int DB_VERSION = 1;

    private static final String TABLE_NAME = "deleted_messages";
    private static final String TABLE_NAME_EDITS = "edited_messages";
    private static final String TABLE_NAME_GUILDS = "guilds";
    private static final String TABLE_NAME_CHANNELS = "channels";
    private static final String TABLE_NAME_SETTINGS = "settings";

    private final SQLiteDatabase db;

    public SQLite(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" +
            "id LONG, " +
            "delete_data, " +
            "record TEXT)";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_EDITS + " (" +
            "id LONG, " +
            "record TEXT)";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_GUILDS + " (" +
            "id LONG)";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_CHANNELS + " (" +
            "id LONG)";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_SETTINGS + " (" +
            "name TEXT, " +
            "value TEXT)";
        db.execSQL(query);
    }

    public void addNewMessage(MessageRecord record) {
        String recordJson = InboundGatewayGsonParser.toJson(record);
        String deleteDataJson = InboundGatewayGsonParser.toJson(record.deleteData);
        String query = "INSERT INTO " + TABLE_NAME + " (id, delete_data, record) VALUES (?, ?, ?)";
        db.execSQL(query, new Object[]{record.message.getId(), deleteDataJson, recordJson});
    }

    public void addNewMessageEdit(MessageRecord record) {
        String recordJson = InboundGatewayGsonParser.toJson(record);
        String query = "UPDATE " + TABLE_NAME_EDITS + " SET record = ? WHERE id = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{ String.valueOf(record.message.getId()) })) {
            if (cursor.getCount() == 0) {
                query = "INSERT INTO " + TABLE_NAME_EDITS + " (id, record) VALUES (?, ?)";
                db.execSQL(query, new Object[]{ record.message.getId(), recordJson });
            }
        }
    }

    public void removeDeletedMessage(long id) {
        String query = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        db.execSQL(query, new Object[]{id});
    }

    public void removeEditedMessage(long id) {
        String query = "DELETE FROM " + TABLE_NAME_EDITS + " WHERE id = ?";
        db.execSQL(query, new Object[]{id});
    }

    public void clearEditedMessages() {
        String query = "DELETE FROM " + TABLE_NAME_EDITS;
        db.execSQL(query);
    }

    public void clearDeletedMessages() {
        String query = "DELETE FROM " + TABLE_NAME;
        db.execSQL(query);
    }

    public void clearGuilds() {
        String query = "DELETE FROM " + TABLE_NAME_GUILDS;
        db.execSQL(query);
    }

    public void clearChannels() {
        String query = "DELETE FROM " + TABLE_NAME_CHANNELS;
        db.execSQL(query);
    }

    public void setBoolSetting(String key, Boolean value) {
        String query = "UPDATE " + TABLE_NAME_SETTINGS + " SET value = ? WHERE name = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{key})) {
            if (cursor.getCount() == 0) {
                query = "INSERT INTO " + TABLE_NAME_SETTINGS + " (name, value) VALUES (?, ?)";
                db.execSQL(query, new Object[]{key, String.valueOf(value)});
            }
        }
    }

    public Boolean getBoolSetting(String key, Boolean defaultVal) {
        String query = "SELECT * FROM " + TABLE_NAME_SETTINGS + " WHERE name = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{ key })) {
            cursor.moveToFirst();
            return cursor.getCount() > 0 ? Boolean.parseBoolean(cursor.getString(1)) : defaultVal;
        }
    }

    public Boolean isGuildWhitelisted(long id) {
        if (getBoolSetting("ignoreMutedServers", true) && UtilsKt.isGuildMuted(id)) return false;
        String query = "SELECT * FROM " + TABLE_NAME_GUILDS + " WHERE id = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{ String.valueOf(id) })) {
            return getBoolSetting("whitelist", false) ? cursor.getCount() > 0 : cursor.getCount() == 0;
        }
    }

    public Boolean isChannelWhitelisted(Channel channel) {
        var id = ChannelWrapper.getId(channel);
        if (getBoolSetting("ignoreMutedChannels", true) && UtilsKt.isChannelMuted(ChannelWrapper.getGuildId(channel), id)) return false;
        String query = "SELECT * FROM " + TABLE_NAME_CHANNELS + " WHERE id = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{ String.valueOf(id) })) {
            return getBoolSetting("channelWhitelist", false) ? cursor.getCount() > 0 : cursor.getCount() == 0;
        }
    }

    public void addGuildToWhitelist(long id) {
        if (getBoolSetting("whitelist", false)) {
            String query = "INSERT INTO " + TABLE_NAME_GUILDS + " (id) VALUES (?)";
            db.execSQL(query, new Object[]{id});
        }else {
            String query = "DELETE FROM " + TABLE_NAME_GUILDS + " WHERE id = ?";
            db.execSQL(query, new Object[]{id});
        }
    }

    public void removeGuildFromWhitelist(long id) {
        if (!getBoolSetting("whitelist", false)) {
            String query = "INSERT INTO " + TABLE_NAME_GUILDS + " (id) VALUES (?)";
            db.execSQL(query, new Object[]{id});
        }else {
            String query = "DELETE FROM " + TABLE_NAME_GUILDS + " WHERE id = ?";
            db.execSQL(query, new Object[]{id});
        }
    }

    public void addChannelToWhitelist(long id) {
        if (getBoolSetting("channelWhitelist", true)) {
            String query = "INSERT INTO " + TABLE_NAME_CHANNELS + " (id) VALUES (?)";
            db.execSQL(query, new Object[]{id});
        }else {
            String query = "DELETE FROM " + TABLE_NAME_CHANNELS + " WHERE id = ?";
            db.execSQL(query, new Object[]{id});
        }
    }

    public void removeChannelFromWhitelist(long id) {
        if (!getBoolSetting("channelWhitelist", true)) {
            String query = "INSERT INTO " + TABLE_NAME_CHANNELS + " (id) VALUES (?)";
            db.execSQL(query, new Object[]{id});
        }else {
            String query = "DELETE FROM " + TABLE_NAME_CHANNELS + " WHERE id = ?";
            db.execSQL(query, new Object[]{id});
        }
    }

    public Boolean isMessageDeleted(long id) {
        String query = "SELECT EXISTS (SELECT 1 FROM " + TABLE_NAME + " WHERE id = ?)";
        try (Cursor cursor = db.rawQuery(query, new String[]{ String.valueOf(id) })) {
            cursor.moveToNext();
            return cursor.getInt(0) == 1;
        }
    }

    public Boolean isMessageEdited(long id) {
        String query = "SELECT EXISTS (SELECT 1 FROM " + TABLE_NAME_EDITS + " WHERE id = ?)";
        try (Cursor cursor = db.rawQuery(query, new String[]{ String.valueOf(id) })) {
            cursor.moveToNext();
            return cursor.getInt(0) == 1;
        }
    }

    public Cursor getAllEditedMessages() {
        String query = "SELECT * FROM " + TABLE_NAME_EDITS;
        return db.rawQuery(query, null);
    }

    public Cursor getAllMessageEdits(long id) {
        String query = "SELECT * FROM " + TABLE_NAME_EDITS + " WHERE id = ?";
        return db.rawQuery(query, new String[]{String.valueOf(id)});
    }

    public Cursor getAllDeletedMessages() {
        String query = "SELECT * FROM " + TABLE_NAME;
        return db.rawQuery(query, null);
    }

    public void importDatabase() {
        String exported = Environment.getExternalStorageDirectory() + "/Aliucord/message_logger.db";
        File exportedDB = new File(exported);
        if (!exportedDB.exists()) {
            Utils.showToast("Cannot import database from '" + exported + "' as it does not exist");
            return;
        }
        String query = "ATTACH DATABASE ? AS exportdb";
        db.execSQL(query, new Object[]{exported});
        query = "INSERT INTO " + TABLE_NAME + " SELECT * FROM exportdb." + TABLE_NAME;
        db.execSQL(query);
        query = "INSERT INTO " + TABLE_NAME_EDITS + " SELECT * FROM exportdb." + TABLE_NAME_EDITS;
        db.execSQL(query);
        query = "INSERT INTO " + TABLE_NAME_GUILDS + " SELECT * FROM exportdb." + TABLE_NAME_GUILDS;
        db.execSQL(query);
        query = "INSERT INTO " + TABLE_NAME_CHANNELS + " SELECT * FROM exportdb." + TABLE_NAME_CHANNELS;
        db.execSQL(query);
        query = "INSERT INTO " + TABLE_NAME_SETTINGS + " SELECT * FROM exportdb." + TABLE_NAME_SETTINGS;
        db.execSQL(query);
        Utils.showToast("Successfully imported database (restart required)");
    }

    public void exportDatabase() {
        String exported = Environment.getExternalStorageDirectory() + "/Aliucord/message_logger.db";
        try {
            File exportedDB = new File(exported);
            assert exportedDB.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String query = "ATTACH DATABASE ? AS exportdb";
        db.execSQL(query, new Object[]{exported});
        db.execSQL("DROP TABLE IF EXISTS exportdb." + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS exportdb." + TABLE_NAME_EDITS);
        db.execSQL("DROP TABLE IF EXISTS exportdb." + TABLE_NAME_GUILDS);
        db.execSQL("DROP TABLE IF EXISTS exportdb." + TABLE_NAME_CHANNELS);
        db.execSQL("DROP TABLE IF EXISTS exportdb." + TABLE_NAME_SETTINGS);
        query = "CREATE TABLE exportdb." + TABLE_NAME + " (" +
            "id LONG, " +
            "delete_data, " +
            "record TEXT)";
        db.execSQL(query);
        query = "CREATE TABLE exportdb." + TABLE_NAME_EDITS + " (" +
            "id LONG, " +
            "record TEXT)";
        db.execSQL(query);
        query = "CREATE TABLE exportdb." + TABLE_NAME_GUILDS + " (" +
            "id LONG)";
        db.execSQL(query);
        query = "CREATE TABLE exportdb." + TABLE_NAME_CHANNELS + " (" +
            "id LONG)";
        db.execSQL(query);
        query = "CREATE TABLE exportdb." + TABLE_NAME_SETTINGS + " (" +
            "name TEXT, " +
            "value TEXT)";
        db.execSQL(query);
        query = "INSERT INTO exportdb." + TABLE_NAME + " SELECT * FROM " + TABLE_NAME;
        db.execSQL(query);
        query = "INSERT INTO exportdb." + TABLE_NAME_EDITS + " SELECT * FROM " + TABLE_NAME_EDITS;
        db.execSQL(query);
        query = "INSERT INTO exportdb." + TABLE_NAME_GUILDS + " SELECT * FROM " + TABLE_NAME_GUILDS;
        db.execSQL(query);
        query = "INSERT INTO exportdb." + TABLE_NAME_CHANNELS + " SELECT * FROM " + TABLE_NAME_CHANNELS;
        db.execSQL(query);
        query = "INSERT INTO exportdb." + TABLE_NAME_SETTINGS + " SELECT * FROM " + TABLE_NAME_SETTINGS;
        db.execSQL(query);
        Utils.showToast("Successfully exported database to '" + exported + "'", true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_EDITS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_GUILDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_CHANNELS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_SETTINGS);
        onCreate(db);
    }
}
