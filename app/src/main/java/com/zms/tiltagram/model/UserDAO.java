package com.zms.tiltagram.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zms.tiltagram.controller.domain.User;

public class UserDAO extends SQLiteOpenHelper{

    private static final String DATABASE = "bd_user";
    private static final int VERSAO = 4;
    private static final String TABELA_USER = "user";

    public UserDAO(Context context) {
        super(context, DATABASE, null, VERSAO);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String sql_user = "CREATE TABLE " + TABELA_USER + "(" +
                "id TEXT PRIMARY KEY NOT NULL, " +
                "name TEXT NOT NULL, " +
                "facebookId TEXT NOT NULL, " +
                "profile_pic TEXT, " +
                "description TEXT" +
                ");";

        try {
            sqLiteDatabase.execSQL(sql_user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        String sql_user = "DROP TABLE IF EXISTS " + TABELA_USER;
        sqLiteDatabase.execSQL(sql_user);
        onCreate(sqLiteDatabase);
    }

    public void insert(User user) {
        ContentValues cv = new ContentValues();
        cv.put("id", user.getId());
        cv.put("name", user.getName());
        cv.put("profile_pic", user.getProfilePicture());
        cv.put("facebookId", user.getFacebookId());
        cv.put("description", user.getDescription());

        getWritableDatabase().insert(TABELA_USER, null, cv);
    }

    public User getUserbyID(String id) {

        String sql = "SELECT * FROM " + TABELA_USER + " WHERE facebookId = ?";
        String[] args = {id};
        final Cursor cursor = getReadableDatabase().rawQuery(sql, args);
        User user = new User();

        while(cursor.moveToNext()){

            user.setName(cursor.getString(cursor.getColumnIndex("name")));
            user.setFacebookId(cursor.getString(cursor.getColumnIndex("facebookId")));
            user.setProfilePicture(cursor.getString(cursor.getColumnIndex("profile_pic")));
            user.setDescription(cursor.getString(cursor.getColumnIndex("description")));
        }
        cursor.close();

        return user;
    }

    public boolean isUserCreated(String id) {
        String sql = "SELECT id FROM " + TABELA_USER + " WHERE id = ?;";
        String[] args = {String.valueOf(id)};
        final Cursor cursor = getReadableDatabase().rawQuery(sql, args);

        if(cursor.moveToFirst()){
            cursor.close();
            return true;
        }else{
            cursor.close();
            return false;
        }
    }
}
