package com.josepmir.germanincontext.derdiedas;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.josepmir.germanincontext.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by josep on 02.05.14.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "UNLIMITEDGERMAN";
    private static final int DATABASE_VERSION = 18;

    private static Context mContext;

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        createDictionary(db);
        fillDictionary(db);

    }

    public void deleteDictionary(SQLiteDatabase db) {
        try {
            db.delete("DICTIONARY", "", null);
        } catch (Exception ex) {
            Toast.makeText(mContext,ex.toString(), Toast.LENGTH_SHORT);
        }
    }
    public void dropDictionary(SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE IF EXISTS DICTIONARY");
        } catch (Exception ex) {
            Toast.makeText(mContext,ex.toString(), Toast.LENGTH_SHORT);
        }
    }
    public void createDictionary(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE DICTIONARY (" +
                    "wort TEXT, " +
                    "artikel TEXT, " +  //der, die, das
                    "relevance INTEGER, " +  //1 to 15.
                    "isfavorite INTEGER, " + //0 or 1
                    "artShown INTEGER, " + //number f times showned
                    "artBeforeLast INTEGER, " + //before last time it was questioned, was it right or wrong?
                    "artLast INTEGER, " +              //last time it was questioned, was it right or wrong?
                    "typ TEXT, " + //ADV, ADJ, SUB, VER <--pump here all words from langenscheidt
                    "plural TEXT, " +
                    "definition  TEXT); ");
        } catch (Exception ex) {
            Toast.makeText(mContext,ex.toString(), Toast.LENGTH_SHORT);
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


        if (oldVersion<18) {
            dropDictionary(db);
            createDictionary(db);
            fillDictionary(db);
        }

        if (oldVersion<3) {
            // do upgrade from 2 to 3, which will also cover 1->3,
            // since you just upgraded 1->2
        }

    }




    public void fillDictionary(SQLiteDatabase db) {
        //Add default records to animals
        ContentValues _Values = new ContentValues();
        //Get xml resource file
        Resources res = mContext.getResources();

        //Open xml file
        XmlResourceParser _xml = res.getXml(R.xml.substantives);
        try
        {
            //Check for end of document
            int eventType = _xml.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                //Search for record tags
                if ((eventType == XmlPullParser.START_TAG) &&(_xml.getName().equals("s"))){
                    //Record tag found, now get values and insert record

                    _Values.put("artikel", _xml.getAttributeValue(null, "a"));
                    _Values.put("wort", _xml.getAttributeValue(null, "root"));
                    _Values.put("relevance", _xml.getAttributeValue(null, "rel"));
                    _Values.put("artShown", 0);
                    _Values.put("artBeforeLast", 0);
                    _Values.put("artLast", 0);
                    _Values.put("typ", "noun");

                    db.insert("DICTIONARY", null, _Values);
                }
                eventType = _xml.next();
            }
        }
        //Catch errors
        catch (XmlPullParserException e)
        {
            //Log.e(TAG, e.getMessage(), e);
        }
        catch (IOException e)
        {
            //Log.e(TAG, e.getMessage(), e);

        }
        finally
        {
            //Close the xml file
            _xml.close();
        }


    }
}
