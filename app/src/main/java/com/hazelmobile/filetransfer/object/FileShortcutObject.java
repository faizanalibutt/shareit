package com.hazelmobile.filetransfer.object;

import android.content.ContentValues;
import android.net.Uri;

import com.genonbeta.android.database.CursorItem;
import com.genonbeta.android.database.DatabaseObject;
import com.genonbeta.android.database.SQLQuery;
import com.genonbeta.android.database.SQLiteDatabase;

public class FileShortcutObject implements DatabaseObject<Object> {
    public String title;
    public Uri path;

    public FileShortcutObject() {
    }

    public FileShortcutObject(Uri path) {
        this.path = path;
    }

    public FileShortcutObject(String title, Uri path) {
        this(path);
        this.title = title;
    }

    @Override
    public SQLQuery.Select getWhere() {
        return null;/*new SQLQuery.Select(AccessDatabase.TABLE_FILEBOOKMARK).setWhere(
                String.format("%s = ?", AccessDatabase.FIELD_FILEBOOKMARK_PATH), path.toString());*/
    }

    // TODO: 9/24/2019 fileshortcut dialog is commented. #13
    @Override
    public ContentValues getValues() {
        ContentValues contentValues = new ContentValues();

        /*contentValues.put(AccessDatabase.FIELD_FILEBOOKMARK_TITLE, title);
        contentValues.put(AccessDatabase.FIELD_FILEBOOKMARK_PATH, path.toString());*/

        return contentValues;
    }

    @Override
    public void reconstruct(CursorItem item) {
/*        this.title = item.getString(AccessDatabase.FIELD_FILEBOOKMARK_TITLE);
        this.path = Uri.parse(item.getString(AccessDatabase.FIELD_FILEBOOKMARK_PATH));*/
    }

    @Override
    public void onCreateObject(android.database.sqlite.SQLiteDatabase dbInstance, SQLiteDatabase database, Object parent) {

    }

    @Override
    public void onUpdateObject(android.database.sqlite.SQLiteDatabase dbInstance, SQLiteDatabase database, Object parent) {

    }

    @Override
    public void onRemoveObject(android.database.sqlite.SQLiteDatabase dbInstance, SQLiteDatabase database, Object parent) {

    }
}

