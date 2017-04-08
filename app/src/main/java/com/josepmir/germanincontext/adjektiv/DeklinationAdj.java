package com.josepmir.germanincontext.adjektiv;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class DeklinationAdj {
    static String[] deklination_wurzel = {"e", "er", "em", "en", "es"};

    static public String IsAdjective(String current_word_trimmed, Context appContext) {
        if (current_word_trimmed.length() < 4) //break if "die", "sie", "nie", "gut", "so", "in"....
            return "";

        if (Character.isUpperCase(current_word_trimmed.charAt(0)))
            return "";

        for (String wurz : deklination_wurzel) {
            if (current_word_trimmed.endsWith(wurz)) { //obviem adjectius ""no declinats"" (ends with "e", "er", "em", "en", "es")

                if (current_word_trimmed.startsWith("meine") || current_word_trimmed.startsWith("sein") || current_word_trimmed.startsWith("dein"))
                    return "";


                //müde, orange, mager, geheuer, sicher, eben, offen, zufrieden, bequem,

                AdjektivAPI tempApi = AdjektivAPI.getInstance(appContext);
                if (tempApi.isAdjective(current_word_trimmed))
                    return current_word_trimmed;

                if (tempApi.isAdjective(current_word_trimmed.substring(0,current_word_trimmed.length() - 1)))
                    return current_word_trimmed.substring(0,current_word_trimmed.length() - 1);
                if (tempApi.isAdjective(current_word_trimmed.substring(0,current_word_trimmed.length() - 2)))
                    return current_word_trimmed.substring(0,current_word_trimmed.length() - 2);

            }
        }

        return "";
    }


    /// <summary>
    ///
    /// </summary>
    /// <param name="word">meinem</param>
    /// <returns></returns>
    public static List<String> GetSolutions(String root, String word) {
        List<String> combinations = new ArrayList<String>();

        if (root.endsWith("e")) {
            combinations.add(root.toLowerCase());
            combinations.add(root.toLowerCase() + "r");
            combinations.add(root.toLowerCase() + "n");
            combinations.add(root.toLowerCase() + "m");
            combinations.add(root.toLowerCase() + "s");

            if (root.equals("eure")) combinations.add("euer"); //irregular case
        } else {

            combinations.add(root.toLowerCase());
            combinations.add(root.toLowerCase() + "es");
            combinations.add(root.toLowerCase() + "e");
            combinations.add(root.toLowerCase() + "er");
            combinations.add(root.toLowerCase() + "en");
            combinations.add(root.toLowerCase() + "em");
        }

        if (!combinations.contains(word.toLowerCase())) {
            //this should never happen! means solution not included
            String ja = "ja";
        }

        return combinations;
    }


}