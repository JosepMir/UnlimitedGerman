package com.josepmir.germanincontext.adjektiv;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by josep on 12.05.14.
 */

public class AdjektivAPI {
    private static AdjektivAPI objDictionary;
    private static Context mContext;

 /*
    getWordArticle
            getWordRelevance
    getRandomword()
    getRandomwordByRelevance()
*/


    public static AdjektivAPI getInstance(Context appContext) {

        if (objDictionary == null) {
            mContext = appContext;
            objDictionary = new AdjektivAPI(appContext);
        }
        return objDictionary;
    }


    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase database;


    /**
     * @param context
     */
    public AdjektivAPI(Context context) {
        dbHelper = new MyDatabaseHelper(context);
        database = dbHelper.getWritableDatabase();

    }

    String[] cols = new String[]{"wort", "relevance"};


    public boolean isAdjective(String wort) {
        boolean result = false;

        Cursor cursor = database.query(true, "ADJECTIVES", cols, "wort = '" + wort + "'", null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0)
                result = true;
        }
        cursor.close();
        return result;
    }
/*
    public void updateShown(String word, int wasCorrect) {
        if (wasCorrect == 2)
            return;
        database.execSQL("UPDATE DICTIONARY SET artShown = artShown + 1, artBeforeLast= artLast, artLast= " + wasCorrect + " WHERE wort='" + word + "'");
    }*/
}