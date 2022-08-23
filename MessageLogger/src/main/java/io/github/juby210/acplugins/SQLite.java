package io.github.juby210.acplugins;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.discord.models.deserialization.gson.InboundGatewayGsonParser;

import io.github.juby210.acplugins.messagelogger.MessageRecord;

public class SQLite extends SQLiteOpenHelper {

    private static final String DB_NAME = "message_logger";

    private static final int DB_VERSION = 1;

    private static final String TABLE_NAME = "deleted_messages";
    private static final String TABLE_NAME_EDITS = "edited_messages";

    public SQLite(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
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
    }

    public void addNewMessage(MessageRecord record) {
        SQLiteDatabase db = getWritableDatabase();
        String recordJson = InboundGatewayGsonParser.toJson(record);
        String deleteDataJson = InboundGatewayGsonParser.toJson(record.deleteData);
        String query = "INSERT INTO " + TABLE_NAME + " (id, delete_data, record) VALUES (?, ?, ?)";
        db.execSQL(query, new Object[]{record.message.getId(), deleteDataJson, recordJson});
    }

    public void addNewMessageEdit(MessageRecord record) {
        SQLiteDatabase db = getWritableDatabase();
        String recordJson = InboundGatewayGsonParser.toJson(record);
        String query = "SELECT * FROM " + TABLE_NAME_EDITS + " WHERE id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(record.message.getId())});
        if (cursor.getCount() > 0) {
            query = "UPDATE " + TABLE_NAME_EDITS + " SET record = ? WHERE id = ?";
            db.execSQL(query, new Object[]{recordJson, record.message.getId()});
        } else {
            query = "INSERT INTO " + TABLE_NAME_EDITS + " (id, record) VALUES (?, ?)";
            db.execSQL(query, new Object[]{record.message.getId(),recordJson});
        }
    }

    public void removeDeletedMessage(long id) {
        SQLiteDatabase db = getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        db.execSQL(query, new Object[]{id});
    }

    public void removeEditedMessage(long id) {
        SQLiteDatabase db = getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME_EDITS + " WHERE id = ?";
        db.execSQL(query, new Object[]{id});
    }

    public void clearEditedMessages() {
        SQLiteDatabase db = getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME_EDITS;
        db.execSQL(query);
    }

    public void clearDeletedMessages() {
        SQLiteDatabase db = getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME;
        db.execSQL(query);
    }

    public Boolean isMessageDeleted(long id) {
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        return cursor.getCount() > 0;
    }

    public Boolean isMessageEdited(long id) {
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME_EDITS + " WHERE id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        return cursor.getCount() > 0;
    }

    public Cursor getAllEditedMessages() {
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME_EDITS;
        return db.rawQuery(query, null);
    }

    public Cursor getAllMessageEdits(long id) {
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME_EDITS + " WHERE id = ?";
        return db.rawQuery(query, new String[]{String.valueOf(id)});
    }

    public Cursor getAllDeletedMessages() {
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        return db.rawQuery(query, null);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_EDITS);
        onCreate(db);
    }
}
