package com.josepmir.germanincontext.deklination;

import java.util.*;

public class ProcessTextHelper1 {
    static String[] deklination_wurzel = {"mein", "dein", "sein", "ihr", "unser", "eure", "kein", "irgendein", "ein", "jede", "manche", "dies", "welche"};
    static String[] endings = {"", "e", "er", "en", "em", "s", "es"};

    static public String IsDeklinabel(String current_word_trimmed) {

        if (current_word_trimmed.toLowerCase().equals("euer"))
            return "eure"; //irregular case. (to avoid accepting euere, eueren... which do not exist)

        for (String wurz : deklination_wurzel) {
            if (current_word_trimmed.toLowerCase().startsWith(wurz)) {

                if (current_word_trimmed.length() <= wurz.length() + 2) {
                    int excess = current_word_trimmed.length() - wurz.length();
                    if (wurz.endsWith("e")) excess++;
                    String excess_string = current_word_trimmed.substring(current_word_trimmed.length() - excess);
                    if (excess_string.equals("es") || excess_string.equals("em") || excess_string.equals("en") || excess_string.equals("s")
                            || excess_string.equals("er") || excess_string.equals("e") || excess_string.equals("")) //"" in the case of "jede"
                    {
                        return wurz;
                    }
                }
            }
        }

        return "";
    }

    //seiner seine
    //sein seines
    //seinen seinem

    //dieser diese
    //dies dieses
    //diesen diesem

    //jeder jede
    //jedes
    //jeden jedem

    //euer eure
    //euren //eurem
    //eures eurer

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