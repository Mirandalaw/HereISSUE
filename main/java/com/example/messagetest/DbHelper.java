package com.example.messagetest;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.messagetest.ContactModel.*;

import java.util.ArrayList;
import java.util.List;
//앱이 필요할 때마다 사용할 수 있도록 사용자가 선택한 모든 연락처를 DB에 저장
public class DbHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "contactdata.db";

    // Country table name
    private static final String TABLE_NAME= "contacts";

    // Country Table Columns names
    private static final String KEY_ID = "id";
    private static final String NAME = "Name";
    private static final String PHONENO = "PhoneNo";


    public DbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // create the table for the first time
        String CREATE_COUNTRY_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + NAME + " TEXT,"
                + PHONENO + " TEXT" + ")";
        db.execSQL(CREATE_COUNTRY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    // 연락처 생성 메소드
    public void addcontact(ContactModel contact){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues c=new ContentValues();
        c.put(NAME,contact.getName());
        c.put(PHONENO,contact.getPhoneNo());
        db.insert(TABLE_NAME,null,c);
        db.close();
    }

    // 목록의 모든 연락처 검색 메소드
    public List<ContactModel> getAllContacts(){
        List<ContactModel> list=new ArrayList<>();
        String query="SELECT * FROM "+TABLE_NAME;
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor c=db.rawQuery(query,null);
        if(c.moveToFirst()) {
            do {

                list.add(new ContactModel(c.getInt(0),c.getString(1),c.getString(2)));

            } while (c.moveToNext());
        }
        return list;
    }

    // 데이터를 받아오기 위해 사용자의 동의
    // 5개 이상의 연락처 추가 x
    public int count(){
        int count=0;
        String query="SELECT COUNT(*) FROM "+TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c=db.rawQuery(query,null);
        if(c.getCount()>0){
            c.moveToFirst();
            count=c.getInt(0);
        }
        c.close();
        return count;
    }

    // 단일 삭제 메소드
    public void deleteContact(ContactModel contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        int i=db.delete(TABLE_NAME,KEY_ID + " = ?",
                new String[] { String.valueOf(contact.getId()) });

        db.close();
    }
}