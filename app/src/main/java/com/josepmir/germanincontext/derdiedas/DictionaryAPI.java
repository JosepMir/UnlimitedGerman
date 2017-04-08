package com.josepmir.germanincontext.derdiedas;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.Random;

/**
 * Created by josep on 12.05.14.
 */

public class DictionaryAPI {
    private static DictionaryAPI objDictionary;
    private static android.content.Context mContext;


    public static DictionaryAPI getInstance(android.content.Context appContext) {

        if (objDictionary == null) {
            mContext = appContext;
            objDictionary = new DictionaryAPI(appContext);
        }
        return objDictionary;
    }


    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;


    /**
     * @param context
     */
    public DictionaryAPI(Context context) {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();

    }

    String[] cols = new String[]{"artikel", "wort", "relevance", "typ"};


    public String getArtikel(String wort) {
        String result="";
        Cursor c = database.query(true, "DICTIONARY", cols, "wort like '" + wort + "'", null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
            result = c.getString(0);
        }
        c.close();
        return result;

    }

    int[] buckets = null;

    private void fillBuckets() {
        try {
            buckets = new int[20];

            Cursor c = database.rawQuery("select count(*) FROM DICTIONARY", null); //nombre d'entrades
            c.moveToFirst();
            int bucket_size = c.getInt(0) / buckets.length;  //ho dividim per 10 i guardem a BUCKET. sabrem quantes entrades ha de tenir cada nivell

            buckets[0] = 50000;
            for (int i = 0; i < buckets.length - 1; i++) {
                //els ultims numeros de cada bucket els anem guardant a un int[10].
                c = database.rawQuery("select * from DICTIONARY WHERE relevance < " + buckets[i] + " ORDER BY relevance DESC LIMIT " + bucket_size, null);// WHERE relevance < " + buckets[i] , null); //+ " ORDER BY relevance LIMIT "+bucket_size +" ", null);
                c.moveToLast();
                if (c.getCount() != 0) {
                    buckets[i + 1] = c.getInt(2);
                } else {
                    buckets[i + 1] = -1;
                    break;
                }
            }
            c.close();
        } catch (Exception ex) {
            Toast.makeText(mContext, ex.toString(), Toast.LENGTH_LONG);
        }
    }

    Random r = new Random();

    public class Question {
        String artikel;
        String wort;
        String relevance;
    }
    public Question getQuestionByDifficulty(String lastWord, int difficulty) {
        if (buckets == null) fillBuckets();

        int high = buckets[difficulty];
        int low = buckets[difficulty + 1];

        //ja tenim el array. ara podrem fer consutles random.
        String relevance = " relevance < " + high + " AND relevance > " + low + " ";
        if (high == -1)
            relevance = " relevance=-1 ";

        Question result = getLeastShown(relevance, lastWord);

       /* if (r.nextInt(3) == 0) { //1 out of 3 times, ask a wrongly answered question (in case there are!)
            Question incorrectAnswered = getIncorrectAnswered(relevance);
            if (incorrectAnswered != null && lastWord != incorrectAnswered.wort)
                result = incorrectAnswered;
        }*/
        return result;
    }

    private int getLeastShown2(String relevanceCondition) {
        Cursor c = database.rawQuery("select MIN(artShown) FROM DICTIONARY WHERE " + relevanceCondition, null);
        c.moveToFirst();
        return c.getInt(0);
    }

    private Question getLeastShown(String relevanceCondition, String except) {
        int artShown =  getLeastShown2(relevanceCondition);
        Cursor c = database.rawQuery("select * FROM DICTIONARY " +
                "WHERE wort!='"+except + "' "
                + " AND artShown=" + artShown
                + " AND " + relevanceCondition
                + " ORDER BY RANDOM() LIMIT 1", null);
        c.moveToFirst();

        Question q = new Question();
        q.wort = c.getString(0);
        q.artikel = c.getString(1);
        q.relevance = c.getString(2);

        c.close();
        return q;
    }

    private Question getIncorrectAnswered(String relevance) {
        Cursor c = database.rawQuery("select * FROM DICTIONARY WHERE " + relevance + " AND ( (artShown > 1 AND (artBeforeLast==0 OR artLast==0))    OR  (artShown==1 AND artLast==0) ) ORDER BY RANDOM() LIMIT 1", null);
        c.moveToFirst();

        Question q = new Question();
        q.wort = c.getString(0);
        q.artikel = c.getString(1);
        q.relevance = c.getString(2);

        c.close();
        if (c.getCount() == 0)  //if it was 0, amazing, it means all answers correctly done for the last two times! and returns null
            return null;

        return q;
    }

    public void updateShown(String word) {
        database.execSQL("UPDATE DICTIONARY SET artShown = artShown + 1 WHERE wort='" + word + "'");
    }



    public void updateWasCorrect(String word, boolean wasCorrect) {
        database.execSQL("UPDATE DICTIONARY SET artShown = artShown + 1, artBeforeLast= artLast, artLast= " + (wasCorrect ? "1":"0") + " WHERE wort='" + word + "'");
    }











}