package com.josepmir.germanincontext.deklination;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProcessTextHelper2 {
    static String[] deklination_wurzel = {"der", "die", "das", "dem", "den", "des",
            "dessen", "deren", "denen",
            "mir", "mich",
            "dir", "dich",
            "ihn", "ihm",
            "deswegen", "deshalb", "denn", "weil",
            "an", "am", "in", "im", "ans", "ins",
            "zu", "zum", "zur",
            "wer", "wen", "wem",
            "selbe", "selber", "selbes", "selben",
            "darauf", "daran", "dazu", "darin", "davon", "darüber",
            "derselbe", "dieselbe", "dasselbe",
            "derjenige", "diejenige", "dasjenige"
    };
    //"darum", "dadurch", "dabei", "danach", "davor", "damit", "dafür"

    static String[] articles = {"der", "die", "das", "dem", "den", "des"};
    static Random r = new Random();

    static public boolean IsOther(String word) {
        if (word != "Wurde") {
            word = word.toLowerCase();
        }

        int aleatori = r.nextInt(100);
        if (word.equals("der") || word.equals("die") || word.equals("das") || word.equals("den") || word.equals("dem") || word.equals("des")) {
            if (aleatori > 30) {
                return false; //discard 70% of the articles
            }
        }

        if (word.equals("an") || word.equals("am") || word.equals("in") || word.equals("im") || word.equals("ans") || word.equals("ins")) {
            if (aleatori > 30) {
                return false;
            }
        }

        if (word.equals("zu")) {
            if (aleatori > 10) {
                return false; //discard 90% of the articles
            }
        }

        for (String wurz : deklination_wurzel) {
            if (word.equals(wurz)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> GetSolutions(String word) {
        word = word.toLowerCase();
        List<String> combinations = new ArrayList<String>();

        if (word.equals("der") || word.equals("die") || word.equals("das") || word.equals("den") || word.equals("dem") || word.equals("des")) {
            combinations.add("der");
            combinations.add("die");
            combinations.add("das");
            combinations.add("des");
            combinations.add("den");
            combinations.add("dem");
        }

        if (word.equals("wer") || word.equals("wen") || word.equals("wem") || word.equals("wessen")) {
            combinations.add("wer");
            combinations.add("wen");
            combinations.add("wem");
            combinations.add("wessen");
        }

/*        if (word.equals("würde") || word.equals("wurde")) {
            combinations.add("würde");
            combinations.add("wurde");
        }*/

        if (word.equals("dessen") || word.equals("deren") || word.equals("denen")) {
            combinations.add("dessen");
            combinations.add("deren");
            combinations.add("denen");
        }

        if (word.equals("mir") || word.equals("mich")) {
            combinations.add("mir");
            combinations.add("mich");
        }

        if (word.equals("dir") || word.equals("dich")) {
            combinations.add("dir");
            combinations.add("dich");
        }

        if (word.equals("ihn") || word.equals("ihm")) {

            combinations.add("ihn");
            combinations.add("ihm");
        }

        //von/vom

        if (word.equals("deswegen") || word.equals("deshalb") || word.equals("denn") || word.equals("weil")) {
            combinations.add("deswegen");
            combinations.add("deshalb");
            combinations.add("denn");
            combinations.add("weil");
        }

        if (word.equals("an") || word.equals("am") || word.equals("ans")) {
            combinations.add("an");
            combinations.add("am");
            combinations.add("ans");
        }

        if (word.equals("in") || word.equals("im") || word.equals("ins")) {
            combinations.add("in");
            combinations.add("im");
            combinations.add("ins");
        }


        if (word.equals("zu") || word.equals("zum") || word.equals("zur")) {
            combinations.add("zu");
            combinations.add("zum");
            combinations.add("zur");
        }

        if (word.equals("selbe") || word.equals("selber") || word.equals("selbes") || word.equals("selben")) {
            combinations.add("selbe");
            combinations.add("selber");
            combinations.add("selbes");
            combinations.add("selben");
        }

        if (word.equals("darauf") || word.equals("daran") || word.equals("dazu") || word.equals("darin") || word.equals("davon") || word.equals("darüber")) {
            combinations.add("darauf");
            combinations.add("daran");
            combinations.add("dazu");
            combinations.add("darin");
            combinations.add("davon");
            combinations.add("darüber");
        }

        if (word.equals("derselbe") || word.equals("dieselbe") || word.equals("dasselbe")) {
            combinations.add("derselbe");
            combinations.add("dieselbe");
            combinations.add("dasselbe");
        }

        if (word.equals("derjenige") || word.equals("diejenige") || word.equals("dasjenige")) {
            combinations.add("derjenige");
            combinations.add("diejenige");
            combinations.add("dasjenige");
        }
        //to learn semantic /for real anfängers
        //nicht/nichts - enforce equilibrium
        //wieder/wider - enforce equilibrium
        //hin/her
        //wo/was/wer/wie/warum
        return combinations;
    }


}