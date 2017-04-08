package com.josepmir.germanincontext.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.josepmir.germanincontext.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;

/**
 * Created by josep on 12.05.14.
 */
//sounds

//OpenRawResource

//button +1

//demo/buy functions

public class Common {

    static public String OpenRawResource(Resources res, int id) {
        String result = "";
        try {
            InputStream in_s = res.openRawResource(id);

            byte[] b = new byte[in_s.available()];
            in_s.read(b);
            result = new String(b);
        } catch (Exception e) {
            // e.printStackTrace();
            result = "Error: can't load file." + e.toString();
        }
        return result;
    }


    static public void WriteToSDCard(Context ctx, String type, String filename, String text) {
        try {
            File file = new File(ctx.getExternalFilesDir(null) + "/" + type + "-" + filename);

            //If external storage is not currently mounted
            if (file == null)
                return;

            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(text);
            myOutWriter.close();
            fOut.close();
        } catch (Exception e) {
            //Toast.makeText(, e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    static public String[] getDirectoryFiles(Context ctx, String type) {

        File dir = new File(ctx.getExternalFilesDir(null) + "/" );
        File[] filelist = dir.listFiles();
        Log.v("Files",dir.exists()+"");
        Log.v("Files", dir.isDirectory() + "");
        String[] theNamesOfFiles = new String[filelist.length];
        for (int i = 0; i < theNamesOfFiles.length; i++) {
            theNamesOfFiles[i] = filelist[i].getName();
        }
        if (theNamesOfFiles.length==0) {
            theNamesOfFiles = new String[1];
            theNamesOfFiles[0] = "";
        }
        return theNamesOfFiles;
    }
}