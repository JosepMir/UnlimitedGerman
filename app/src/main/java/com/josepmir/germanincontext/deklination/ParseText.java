package com.josepmir.germanincontext.deklination;

import android.content.Context;

import com.josepmir.germanincontext.adjektiv.DeklinationAdj;

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by josep on 28.02.14.
 */
public class ParseText {

    final String[] possesiv = {"mein", "dein", "sein", "ihr", "unser", "eure"};

    final String[] pronom_dativ = {"mir", "dir", "ihm", "ihr", "es", "uns", "euch", "ihnen"};
    final String[] pronom_akk = {"mich", "dich", "ihn", "sie", "es", "uns", "euch", "sie"};


    //http://www.mrshea.com/adjlist.htm
    //http://www.danielnaber.de/jwordsplitter/
    //http://www.allgemeinbildung.ch/arb/arb=deu/q_Woerterlisten_alle_Adverbien.pdf
    Context mAppContext = null;

    public ParseText(Context appContext) {
        mAppContext = appContext;
    }

    public String Parse(String input, boolean doingAdjectives) {
        try {

            char[] inputCharArray = (input + " ").toCharArray();
            StringBuilder result = new StringBuilder();

            StringBuilder current_word = new StringBuilder();
            for (int i = 0; i < inputCharArray.length; i++) {
                current_word.append(inputCharArray[i]);
                parseWord(doingAdjectives, result, current_word, inputCharArray[i]);
            }

            return PostProcess(result.toString());
        } catch (Exception ex) {
            return "Error during Parse(): " + ex.toString() + ex.getStackTrace();
        }
    }

    private void parseWord(boolean doingAdjectives, StringBuilder result, StringBuilder current_word, char c) {
        if (c == ' ' || c == '.' || c == '\r' || c == '\n' || c == ' ' || c == ',' ||
                c == ';' || c == ':' || c == '(' || c == ')' || c == '{' || c == '}' || c == '?' ||
                c == '!' || c == '\'' || c == '\\')
        //end of a word
        {
            //trim space
            String Word = current_word.toString().replace("" + c, "");


            //Filter zu, an, sein, ein if after that there is , or .. In this case this is part of the verb: never declined
            if (c == ',' || c == '.' || c == ';' || c == '!' || c == ':' || c == '?' || c == '(' || c == ')') {
                if (Word.equals("zu") ||
                        Word.equals("sein") ||
                        Word.equals("ein") ||
                        Word.equals("an") ||
                        Word.equals("aus")) {
                    result.append(Word);
                    result.append("" + c);
                    current_word.setLength(0); // = "";
                    return;
                }
            }


            if (doingAdjectives) {
                //////////////////// DEKLINATION ADJ ///////////////
                String wurzel = DeklinationAdj.IsAdjective(Word, mAppContext);
                if (wurzel != "") {
                    result.append(GenerateXML(Word, DeklinationAdj.GetSolutions(wurzel, Word)));
                } else {
                    result.append(Word);
                }
                /////////////////////////////////

            } else {

                //////////////////// OTHER ///////////////
                if (ProcessTextHelper2.IsOther(Word)) {

                    result.append(GenerateXML(Word, ProcessTextHelper2.GetSolutions(Word)));
                } else {
                    //////////////////// DEKLINATION ///////////////
                    String wurzel = ProcessTextHelper1.IsDeklinabel(Word);
                    if (wurzel != "") {
                        result.append(GenerateXML(Word, ProcessTextHelper1.GetSolutions(wurzel, Word)));
                    } else {
                        result.append(Word);
                    }

                }
                /////////////////////////////////
            }


            result.append("" + c);
            current_word.setLength(0);// = "";
        }
        return;
    }

    /// <summary>
    ///
    /// </summary>
    /// <param name="root">meinem</param>
    /// <returns></returns>
    static public String GenerateXML(String solution, List<String> combinations) {
        //PACK IN XML
        String result = "<rat>";
        for (int i = 0; i < combinations.size(); i++) {
            if (combinations.get(i).equals(solution.toLowerCase()))
                result += "<r" + i + "><sol>" + solution + "</sol></r" + i + ">";
            else
                result += "<r" + i + ">" + combinations.get(i) + "</r" + i + ">";
        }
        result += "</rat>";

        return result;
    }

    static public String PostProcess(String text) {
        Random random = new Random();
        //look for 2 consecutive rat's
        Pattern patt = Pattern.compile("</rat> <rat>");
        Matcher matcher = patt.matcher(text);

        while (matcher.find()) {
            try {
                int middle = matcher.start();

                String second_half = text.substring(middle + 7);
                int end = second_half.indexOf("</rat>") + 6;
                second_half = second_half.substring(0, end);


                String temp = text.substring(0, middle);
                int start = temp.lastIndexOf("<rat>");
                String first_half = text.substring(start, middle + 6);


                String repeated_rat = first_half + second_half;

                if (random.nextBoolean()) {
                    //remove second rat
                    text = text.substring(0, middle + 7) + DeklinationFragment.ExtractSolution(second_half) + text.substring(middle + 7 + end);
                } else {
                    //remove first rat
                    text = text.substring(0, start) + DeklinationFragment.ExtractSolution(first_half) + text.substring(middle + 6);
                }

            } catch (Exception ex) {
                return "Exception while PostProcessing." + text;
            }
            matcher = patt.matcher(text);
        }
        return text;
    }

    static public String Sanitize(String text) {
        return text;
    }
}