package com.josepmir.germanincontext.deklination;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.analytics.HitBuilders;

import com.josepmir.germanincontext.MainActivity;
import com.josepmir.germanincontext.MyApp;
import com.josepmir.germanincontext.R;
import com.josepmir.germanincontext.util.Common;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by josep on 28.04.14.
 */

//COLORS USED
//https://color.adobe.com/Flat-UI-color-theme-2469224/edit/?copy=true&base=1&rule=Custom&selected=4&name=Copy%20of%20Flat%20UI&mode=rgb&rgbvalues=0.5280007650011954,0.7440003825004994,0.96,0.905882,0.298039,0.235294,0.92549,0.941176,0.945098,0.5577933313753523,0.687680622699759,0.83,0.160784,0.501961,0.72549&swatchOrder=0,1,2,3,4

public class DeklinationFragment extends Fragment implements MainActivity.rightDrawerCallback {

    public static final String ARG_PLANET_NUMBER = "planet_number";

    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();


    int cycleNumber = 0;
    String expected_button = "";
    String HIDDEN_DOTS = "[_____]";
    int scroll_jump_counter = 0;
    String parsedText = ""; //made global in order to be saveable when onStop()
    String parsedContentParam_lastNotEmpty = "";
    int buttonNormalHeight;
    float buttonNormalFontSize;

    /*
    Months and Seasons
    im Sommer
    im September

    Specific days
    am Sonntag
    am 24 Dezember

     */
    public static String ExtractSolution(String rat) {
        String first_part = rat.substring(rat.indexOf("<sol>") + 5);
        String solution = first_part.substring(0, first_part.indexOf("</sol>"));
        return solution;
    }

    public boolean doingAdjectives;

    public static DeklinationFragment newInstance(boolean doingAdjectives) {
        DeklinationFragment fragment = new DeklinationFragment();
        fragment.doingAdjectives = doingAdjectives;
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View dek = inflater.inflate(R.layout.fragment_deklination, container, false);

        dek.findViewById(R.id.button1).setOnClickListener(ratButton_OnClickListener);
        dek.findViewById(R.id.button2).setOnClickListener(ratButton_OnClickListener);
        dek.findViewById(R.id.button3).setOnClickListener(ratButton_OnClickListener);
        dek.findViewById(R.id.button4).setOnClickListener(ratButton_OnClickListener);
        dek.findViewById(R.id.button5).setOnClickListener(ratButton_OnClickListener);
        dek.findViewById(R.id.button6).setOnClickListener(ratButton_OnClickListener);

        //Analytics
        Tracker t = ((MyApp) getActivity().getApplication()).getTracker(MyApp.TrackerName.APP_TRACKER);
        t.setScreenName("Deklination");
        t.send(new HitBuilders.AppViewBuilder().build());

        return dek;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

private String getLastPart(String wholeWord) {
    return wholeWord.substring(expected_button.length() - 4);
}
    final View.OnClickListener ratButton_OnClickListener = new View.OnClickListener() {
        public void onClick(final View v) {
            int resourceId = v.getId();
            try {
                hideDictionary();

                Button b = (Button) getView().findViewById(resourceId);
                String selected_button = b.getText().toString();


                boolean correct = (selected_button.equals(expected_button) ||
                        (selected_button.startsWith("...") && selected_button.substring(3).equals(getLastPart(expected_button) ) )); //the word is too long and was shortened with ...


                if (correct) {
                    playResult(v.getContext(),true);
                    Cycle("");
                } else {
                    b.setTextColor(Color.rgb(231,76,60));
                    playResult(v.getContext(),false);
                }









            } catch (Exception ex) {
                Toast.makeText(getActivity().getApplicationContext(), "Exception: ratButton_OnClickListener()-" + ex.toString() + "-" + ex.getStackTrace().toString(), Toast.LENGTH_LONG).show();
            }
        }
    };

public void playResult(Context ctx, boolean right) throws IOException {
    final MediaPlayer mPlayer;

    if (right)
        mPlayer= MediaPlayer.create(ctx, R.raw.wine_glass);
    else
        mPlayer= MediaPlayer.create(ctx,R.raw.wrong);

    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

    mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer arg0) {
            mPlayer.start();

        }
    });


}


    boolean onlyFavorite = false;

    //As you can see the signature of this method is different from the one of the Activity class. With fragments your method takes a MenuInflater as second parameter. And the fragment’s onCreateOptionsMenu() method also doesn’t return a boolean value.
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.main, menu);

        //MenuItem mi = (MenuItem) menu.findItem(R.id.buy);
        //mi.setVisible(((MainActivity) getActivity()).bIS_DEMO);


        //WHEN WEBVIEW: SHOW FAVORITE TOO
        // MenuItem mi = (MenuItem) menu.findItem(R.id.favorite);
        // mi.setVisible(onlyFavorite);
        // mi.setVisible(true);

        MenuItem mi = (MenuItem) menu.findItem(R.id.action_forward);
        mi.setVisible(!onlyFavorite);

        mi = (MenuItem) menu.findItem(R.id.random_wiki);
        mi.setVisible(!onlyFavorite);

    }

    String url_current_article = "";

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {
            case R.id.repeat:
                Repeat();
                return true;
            case R.id.text1:
                NewContent(Common.OpenRawResource(getResources(), R.raw.text1), true);
                return true;
            case R.id.text2:
                NewContent(Common.OpenRawResource(getResources(), R.raw.text2), true);
                return true;
            case R.id.text3:
                NewContent(Common.OpenRawResource(getResources(), R.raw.text3));
                return true;
            case R.id.text4:
                NewContent(Common.OpenRawResource(getResources(), R.raw.text4));
                return true;
            case R.id.action_forward:
                if (!IsIntro && MainActivity.bIS_DEMO) {
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.NEED_PAID_VERSION), Toast.LENGTH_LONG).show();
                    return true;
                }
                actionSkip();
                return true;
            case R.id.random_stupidedia:
                String article_name2 = WikiCleaner.GetRandomStupidediaArticleName();
                NewContent(article_name2 + " from stupidedia.org\n\n" + WikiCleaner.GetStupidediaContent(article_name2));

                url_current_article = "http://www.stupidedia.org/stupi/" + article_name2;
                return true;
            case R.id.random_wiki:
                String article_name = WikiCleaner.GetRandomWikipediaArticleName();
                NewContent(article_name + " from wikipedia.org\n\n" + WikiCleaner.GetWikiContent(article_name));
                url_current_article = "http://de.wikipedia.org/wiki/" + article_name;
                return true;
            case R.id.custom_wiki:
                actionCustomWiki();
                return true;
            case R.id.custom_text:
                actionFromClipboard();
                return true;

            case R.id.open_in_browser:
                openInBrowser(url_current_article);
                return true;

           /* case R.id.favorite:
                if (isDictionaryVisible) {
                    ////update to dictionary dictionaryCurrentWord
                } else {
                    saveCurrentTextToFavorites();
                }
                return true;
            */
            case R.id.help:
                toggleHelp();
                return true;
        }

        return true;
    }


    public void actionFromClipboard() {
        try {
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            String pasteData = "";

            // If it does contain data, decide if you can handle the data.
            if (clipboard.hasPrimaryClip()) {

                ClipData clipdata = clipboard.getPrimaryClip();
                if (clipdata != null && clipdata.getItemCount() > 0) {
                    ClipData.Item item = clipdata.getItemAt(0);
                    if (clipboard.getPrimaryClipDescription().hasMimeType(android.content.ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                        pasteData = item.getText().toString();
                    }

                    if (clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)) {
                        pasteData = item.getText().toString();
                    }
                }
            }


            if (pasteData.trim() != "")
                NewContent(pasteData);
            else
                Toast.makeText(getActivity().getApplicationContext(), "Clipboard is empty", Toast.LENGTH_LONG).show();


        } catch (Exception ex) {
        }
    }

    public void saveCurrentTextToFavorites() {
        String filename = UUID.randomUUID().toString();
        Common.WriteToSDCard(getActivity().getApplicationContext(), "deklination", filename, parsedText);
    }

    public void actionCustomWiki() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        alert.setTitle("Wikipedia Article");
        //alert.setMessage("Message");

        final EditText input = new EditText(getActivity());
        alert.setView(input);

        //SET FOCUS ON EDIT (AND SHOW KEYBOARD)
        (new android.os.Handler()).postDelayed(new Runnable() {

            public void run() {
                input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
            }
        }, 200);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString().trim();

                if (!value.equals("")) NewContent(WikiCleaner.GetWikiContent(value));
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.setCancelable(true);
        alert.show();

    }

    String[] praepositionen_dativ = {"ab", "aus", "außer", "bei", "mit", "von", "zu", "seit", "nach", "binnen", "dank", "entgegen", "entsprechend", "fern", "gegenüber", "gemäß", "getreu", "laut", "samt", "mitsamt"
            , "nahe", "nebst", "zufolge", "zuliebe", "vis-à-vis"};
    String[] praepositionen_akkusativ = {"a", "um", "gegen", "durch", "um", "für", "bis", "bis zu", "ohne", "wider", "je", "versus", "betreffend", "via", "kontra", "entlang", "per", "pro"};

    String[] praepositionen_wechsel = {"an", "auf", "in", "unter", "über", "vor", "hinter", "neben", "zwischen"};

    String[] praepositionen_genitiv = {"oberhalb", "mithilfe", "namens", "mittels", "wegen", "während", "zwecks", "trotz", "außerhalb", "anstatt", "aufgrund", "laut",
            "infolge", "hinsichtlich", "anstelle", "anhand", "angesichts", "abseits", "statt", "mangels", "inmitten", "bezüglich", "einschließlich"};

    public String firstLetterCapital(String word) {
        if (word.length() < 2)
            return word.toUpperCase();
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }

    public void setSpanAllDativ(String text, Spannable spannable) {
        int color = getResources().getColor(R.color.Dat);
        for (String preap : praepositionen_dativ) {
            setSpanToAll(" " + preap + " ", text, spannable, color);
            setSpanToAll(" " + firstLetterCapital(preap) + " ", text, spannable, color);
        }
    }

    public void setSpanAllAkkusativ(String text, Spannable spannable) {
        int color = getResources().getColor(R.color.Akk);
        for (String preap : praepositionen_akkusativ) {
            setSpanToAll(" " + preap + " ", text, spannable, color);
            setSpanToAll(" " + firstLetterCapital(preap) + " ", text, spannable, color);
        }
    }

    public void setSpanAllWechsel(String text, Spannable spannable) {
        int color1 = getResources().getColor(R.color.Akk);
        int color2 = getResources().getColor(R.color.Dat);

        for (String preap : praepositionen_wechsel) {
            setSpanToAll2Colors(" " + preap + " ", text, spannable, color1, color2);
            setSpanToAll2Colors(" " + firstLetterCapital(preap) + " ", text, spannable, color1, color2);
        }
    }

    public void setSpanAllGenitiv(String text, Spannable spannable) {
        int color = getResources().getColor(R.color.Gen);
        for (String preap : praepositionen_genitiv) {
            setSpanToAll(" " + preap + " ", text, spannable, color);
            setSpanToAll(" " + firstLetterCapital(preap) + " ", text, spannable, color);
        }
    }

    public void setSpanToAll(String word, String text, Spannable spannable, int color) {
        int index = text.indexOf(word);
        while (index >= 0) {
            spannable.setSpan(new BackgroundColorSpan(color), index, index + word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            index = text.indexOf(word, index + 1);
        }
    }

    public void setSpanToAll2Colors(String word, String text, Spannable spannable, int color1, int color2) {
        int index = text.indexOf(word);
        while (index >= 0) {
            for (int i = 0; i < word.length(); i++) {
                int color_temp;
                if (i % 2 == 1)
                    color_temp = color1;
                else
                    color_temp = color2;
                spannable.setSpan(new BackgroundColorSpan(color_temp), index + i, index + i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            index = text.indexOf(word, index + 1);
        }
    }


        class IntPair {
            // Ideally, name the class after whatever you're actually using
            // the int pairs *for.*
            final int x;
            final int y;
            IntPair(int x, int y) {this.x=x;this.y=y;}
            // depending on your use case, equals? hashCode?  More methods?
        }

        List<IntPair> pastWords= new ArrayList<>();

    public void Highlight(boolean showHelp) {
        try {
            TextView textView = (TextView) getView().findViewById(R.id.maintext);
            String text = textView.getText().toString();
            Spannable spannable = new SpannableString(textView.getText().toString());

            int max=0;
            for (IntPair pair : pastWords) {
                if (cycleNumber ==max) break;
                StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
                StyleSpan boldSpan2 = new StyleSpan(Typeface.ITALIC);
                spannable.setSpan(boldSpan, pair.x, pair.y, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(boldSpan2, pair.x, pair.y, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                max++;
            }

            int index = text.indexOf(HIDDEN_DOTS);
            if (index != -1) {

//                StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
                //              StyleSpan boldSpan2 = new StyleSpan(Typeface.ITALIC);
                //            spannable.setSpan(boldSpan, 1, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                //          spannable.setSpan(boldSpan2, 9, 12, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                spannable.setSpan(new BackgroundColorSpan(Color.rgb(135,190,245)), index, index + HIDDEN_DOTS.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                //              spannable.setSpan(new BackgroundColorSpan(Color.CYAN), 2, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                //            spannable.setSpan(new BackgroundColorSpan(Color.RED), 5, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            }

            if (showHelp) {
                setSpanAllDativ(text, spannable); //zu
                setSpanAllAkkusativ(text, spannable); //bis zu, needs to be done later in order for it to be kept akk
                setSpanAllWechsel(text, spannable);
                setSpanAllGenitiv(text, spannable);
            }

            textView.setText(spannable, TextView.BufferType.SPANNABLE);

        } catch (Exception ex) {
            Toast.makeText(getActivity().getApplicationContext(), "Exception: Highlight()-" + ex.toString() + "-" + ex.getStackTrace().toString(), Toast.LENGTH_LONG).show();
        }
    }


    public void actionSkip() {
        hideDictionary();
        Cycle("");
    }

    public void NewContent(String text) {
        NewContent(text, false);
    }

    public void Repeat() {
        putParsedTextToScreen(parsedText, 0);
    }

    boolean IsIntro = false;

    public void NewContent(String text, boolean isIntro) {
        try {
            if (isIntro)
                IsIntro = true;
            else
                IsIntro = false;

            ParseText pt = new ParseText(getActivity().getApplicationContext());
            putParsedTextToScreen(pt.Parse(text, doingAdjectives), 0);

        } catch (Exception ex) {
            Toast.makeText(getActivity().getApplicationContext(), "Exception: NewContent()-" + ex.toString() + "-" + ex.getStackTrace().toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void putParsedTextToScreen(String parsedTextParam, int startFrom_CycleNumber) {
        try {
            parsedText = parsedTextParam; //we could do this function without parameter, but then somebody could forget setting the global variable parsedText before
            cycleNumber = startFrom_CycleNumber;

            scroll_jump_counter = 0; //scroll back at the beginning

            Cycle(parsedTextParam);
            scrollToTop();


        } catch (Exception ex) {
            Toast.makeText(getActivity().getApplicationContext(), "Exception: putParsedTextToScreen()-" + ex.toString() + "-" + ex.getStackTrace().toString(), Toast.LENGTH_LONG).show();
        }

    }

    private void scrollToTop() {
        //after everything is loaded, as it is the first time, scroll to the top
        ScrollView sv = (ScrollView) getView().findViewById(R.id.scrollView);
        sv.smoothScrollTo(0, 0);
    }

    private void Cycle(String parsedContentParam) {
        try {

            hideDictionary();


            if (parsedContentParam.equals(""))
                parsedContentParam = parsedContentParam_lastNotEmpty;
            else
                parsedContentParam_lastNotEmpty = parsedContentParam;

            TextView t = (TextView) getView().findViewById(R.id.maintext);
            String masked = MaskInput(cycleNumber, parsedContentParam);
            t.setText(masked);
            Highlight(isHelpMode);

            String current_rat = GetNRat(parsedContentParam, cycleNumber++);


            if (!IsIntro && MainActivity.bIS_DEMO) {
                current_rat = "";
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.NEED_PAID_VERSION)
                        , Toast.LENGTH_LONG).show();
            }


            FillButtons(current_rat);
        } catch (Exception ex) {
            Toast.makeText(getActivity().getApplicationContext(), "Exception: AnswerWasCorrect()-" + ex.toString() + "-" + ex.getStackTrace().toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void FillButtons(String rat) {

        Button b1 = (Button) getView().findViewById(R.id.button1);
        Button b2 = (Button) getView().findViewById(R.id.button2);
        Button b3 = (Button) getView().findViewById(R.id.button3);
        Button b4 = (Button) getView().findViewById(R.id.button4);
        Button b5 = (Button) getView().findViewById(R.id.button5);
        Button b6 = (Button) getView().findViewById(R.id.button6);

        if (rat.equals("")) {
            b1.setVisibility(View.INVISIBLE);
            b2.setVisibility(View.INVISIBLE);
            b3.setVisibility(View.INVISIBLE);
            b4.setVisibility(View.INVISIBLE);
            b5.setVisibility(View.INVISIBLE);
            b6.setVisibility(View.INVISIBLE);
            return;
        }

        ButtonVisibility(rat, 0, b1);
        ButtonVisibility(rat, 1, b2);
        ButtonVisibility(rat, 2, b3);
        ButtonVisibility(rat, 3, b4);
        ButtonVisibility(rat, 4, b5);
        ButtonVisibility(rat, 5, b6);

        if (b2.getText().length() > 9 || b4.getText().length() > 9 || b6.getText().length() > 9) {

            b2.setText("..." + getLastPart(b2.getText().toString()));

            b4.setText("..." + getLastPart( b4.getText().toString()));

            b6.setText("..." + getLastPart( b6.getText().toString()));
        }

    }

    private void ButtonVisibility(String rat, int buttonnumber, Button b) {
        if (rat.contains("<r" + buttonnumber + ">")) {
            b.setText(getNthChoiceFromRat(rat, buttonnumber));
            b.setTextColor(Color.BLACK);
            b.setVisibility(View.VISIBLE);
        } else {
            b.setVisibility(View.INVISIBLE);
        }
    }

    private String getNthChoiceFromRat(String rat, int i) {

        if (rat.trim().equals("")) return "";

        Pattern patt = Pattern.compile("<r" + i + ">(\\S+)</r" + i + ">");

        Matcher matcher = patt.matcher(rat);
        matcher.find();
        try {
            String sol = matcher.group(1);
            sol = sol.replace("<sol>", "");
            sol = sol.replace("</sol>", "");
            return sol.toLowerCase();
        } catch (Exception e) {
            return "";
        }

    }

    private String GetNRat(String input_xml, int n) throws IllegalStateException {
        try {
            Pattern patt = Pattern.compile("<rat>(\\S+)</rat>");

            String rat = "";
            for (int i = 0; i < n + 1; i++) //COPIED FOR
            {
                Matcher first_rat_occurrence = patt.matcher(input_xml);
                if (first_rat_occurrence.find()) {
                    rat = first_rat_occurrence.group(1);
                } else {
                    //WE ARE ALREADY AT THE END, FINISHED
                    expected_button = "";
                    return "";
                }

                String solution = ExtractSolution(rat);

                input_xml = first_rat_occurrence.replaceFirst(solution);
                expected_button = solution.toLowerCase();
            }

            return rat;
        } catch (Exception ex) {
            Toast.makeText(getActivity().getApplicationContext(), "Exception: GetNRat()-" + ex.toString() + "-" + ex.getStackTrace().toString(), Toast.LENGTH_LONG).show();
            return "";
        }
    }

    //Unmasked items are already the correct ones (reasons: learned things already appliable to next
    // items, growing tension if all are ok, not forgetting what was you first item choice and why...)
    String MaskInput(int CorrectedItems, String input_xml) {

        try {
            //1st PART: replace N-first <rat> with solution
            Pattern patt = Pattern.compile("<rat>(\\S+)</rat>");

            pastWords.clear();
            for (int i = 0; i < CorrectedItems; i++) {

                Matcher first_rat_occurrence = patt.matcher(input_xml);
                if (!first_rat_occurrence.find())
                    break; //no more rats... this happens when we press SKIP though the text is already finished

                String rat = first_rat_occurrence.group(1);



                String first_part = rat.substring(rat.indexOf("<sol>") + 5);
                String solution = first_part.substring(0, first_part.indexOf("</sol>"));
                input_xml = first_rat_occurrence.replaceFirst(solution);

                int x = first_rat_occurrence.start() ;
                int y = first_rat_occurrence.start() + solution.length();

                pastWords.add(new IntPair(x,y));
            }

            //2nd PART: replace all <rat> with ...
            Matcher first_rat_occurrence = patt.matcher(input_xml);
            first_rat_occurrence.find();
            input_xml = first_rat_occurrence.replaceAll(HIDDEN_DOTS);

            return input_xml;
        } catch (Exception ex) {
            Toast.makeText(getActivity().getApplicationContext(), "Exception: MaskInput()-" + ex.toString() + "-" + ex.getStackTrace().toString(), Toast.LENGTH_LONG).show();
            return "";
        }
    }

    ///////////////
    /////////////GET TOUCHED WORD
    //////////////
    public int getOffsetForPosition(TextView textView, float x, float y) {
        if (textView.getLayout() == null) {
            return -1;
        }
        final int line = getLineAtCoordinate(textView, y);
        final int offset = getOffsetAtCoordinate(textView, line, x);
        return offset;
    }

    private int getOffsetAtCoordinate(TextView textView2, int line, float x) {
        x = convertToLocalHorizontalCoordinate(textView2, x);
        return textView2.getLayout().getOffsetForHorizontal(line, x);
    }

    private float convertToLocalHorizontalCoordinate(TextView textView2, float x) {
        x -= textView2.getTotalPaddingLeft();
        // Clamp the position to inside of the view.
        x = Math.max(0.0f, x);
        x = Math.min(textView2.getWidth() - textView2.getTotalPaddingRight() - 1, x);
        x += textView2.getScrollX();
        return x;
    }

    private int getLineAtCoordinate(TextView textView2, float y) {
        y -= textView2.getTotalPaddingTop();
        // Clamp the position to inside of the view.
        y = Math.max(0.0f, y);
        y = Math.min(textView2.getHeight() - textView2.getTotalPaddingBottom() - 1, y);
        y += textView2.getScrollY();
        return textView2.getLayout().getLineForVertical((int) y);
    }


    private String GetToday() {

        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("dd"); //"MM-dd-yyyy HH:mm:ss");
        return format.format(date);

    }

    @Override
    public void onStop() {
        super.onStop();  // Always call the superclass method first
        String extension = "";
        if (doingAdjectives) extension = "_Adj";

        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFERENCES_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("parsedText" + extension, parsedText);
        editor.putInt("cycleNumber" + extension, cycleNumber);
        editor.putBoolean("IsIntro" + extension, IsIntro);
        editor.putString("url_current_article" + extension, url_current_article);


        ScrollView sv = (ScrollView) getView().findViewById(R.id.scrollView);
        int scrollPosition = sv.getScrollY();
        editor.putInt("scrollPosition" + extension, scrollPosition);

        editor.commit();

    }

    @Override
    public void onStart() {
        super.onStart();
        try {

            ConfigureTouchDictionary();

            final String extension;
            if (doingAdjectives) extension = "_Adj";
            else extension = "";

            String intro = Common.OpenRawResource(getResources(), R.raw.intro);
            SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFERENCES_NAME, 0);
            String local_parsedText = settings.getString("parsedText" + extension, "");
            int local_cycleNumber = settings.getInt("cycleNumber" + extension, 1);

            IsIntro=settings.getBoolean("IsIntro"+ extension,false);
            //DEMO functionality
            // guessesToday = settings.getInt("guessesToday", 0); //if they deleted the data, fuck them. they can avoid the whole mechanismus by uninstalling and installing again. but thats hassle enough i find.
            //String todayDateInMemory = settings.getString("todayDate", "-1");
            //todayDate = String.valueOf(GetToday()); //we save the date now in a global variable, because when closing we want to save the one date that was when opened, not when closing (in case of midnight switch and so on...)
            //if (!todayDateInMemory.equals(todayDate)) {
            //    guessesToday = 0;
            //}

            url_current_article = settings.getString("url_current_article" + extension, url_current_article);

            local_cycleNumber--;
            if (!local_parsedText.trim().equals("")) {
                putParsedTextToScreen(local_parsedText, local_cycleNumber);
            } else {
                NewContent(intro, true);
            }


            Runnable task = new Runnable() {
                public void run() {
                    SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFERENCES_NAME, 0);
                    int scrollPosition = settings.getInt("scrollPosition" + extension, 0);
                    ScrollView sv = (ScrollView) getView().findViewById(R.id.scrollView);
                    sv.smoothScrollTo(0, scrollPosition);
                }
            };

            worker.schedule(task, 1, TimeUnit.SECONDS);
        } catch (Exception ex) {
            Toast.makeText(getActivity(), "Exception: OnStart()-" + ex.toString() + "-" + ex.getStackTrace().toString(), Toast.LENGTH_LONG).show();

        }
    }


    private String getPointedWord(int off, String text) {
        text = " " + text.replace("\n", " ");

        int start = off;
        int end = off + 1;
        String current = "";
        do {
            current = text.substring(start, end).replaceAll("[,.'():;*=„“]", " ");  //replaceAll uses regex, [ ] means inside has to be interpreted as lonely chars
            start--;
        }
        while (!current.startsWith(" ") && start != 0);
        start++;

        do {
            current = text.substring(start, end).replaceAll("[,.'():;*=„“]", " ");
            end++;
        }
        while (!current.endsWith(" ") && end < text.length());

        current = current.trim();
        return current;

    }

    String dictionaryCurrentWord = "";

    private void ConfigureTouchDictionary() {
        try {
            TextView tv = (TextView) getView().findViewById(R.id.maintext);

            tv.setOnLongClickListener(new Button.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });

            tv.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    try {
                        TextView tv = (TextView) v;

                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            // then = getSystemTime();
                        } else if (event.getAction() == MotionEvent.ACTION_UP) {


                            if (isDictionaryVisible)
                                hideDictionary();
                            else {
                                int offset = getOffsetForPosition((TextView) v, event.getX(), event.getY());
                                String current = getPointedWord(offset, tv.getText().toString());

                                //when nothing such [...] as is clicked
                                if (!current.equals("")) {
                                    dictionaryCurrentWord = current;
                                    showDictionary(current);
                                }
                            }


                            return true;
                        }
                        return false;
                    } catch (Exception ex) {
                        Toast.makeText(getActivity(), "Exception: OnTouch()-" + ex.toString() + "-" + ex.getStackTrace().toString(), Toast.LENGTH_LONG).show();
                        return false;
                    }
                }
            });
        } catch (Exception ex) {
            Toast.makeText(getActivity(), "Exception: ConfigureTouchDictionary()-" + ex.toString() + "-" + ex.getStackTrace().toString(), Toast.LENGTH_LONG).show();

        }
    }


    boolean isDictionaryVisible = false;

    private void showDictionary(String current) {

        isDictionaryVisible = true;
        WebView webview = (WebView) getView().findViewById(R.id.webView);
        webview.setWebViewClient(new WebViewClient());
        webview.loadData("Loading DictionaryAPI...", "text/plain", "utf-8");
        webview.loadUrl("http://de.thefreedictionary.com/_/dict.aspx?rd=1&word=" + current);
        webview.setVisibility(View.VISIBLE);
        webview.getSettings().setBuiltInZoomControls(true);
        onlyFavorite = true;
        getActivity().invalidateOptionsMenu();

        //find the height of the screen
        LinearLayout ll = (LinearLayout) getView().findViewById(R.id.linearlayout);
        int dictionary_height = (int) (ll.getHeight() * 0.5f);
        //define the dictionary height as 40% of it
        ViewGroup.LayoutParams vc1 = webview.getLayoutParams();
        vc1.height = dictionary_height;
        webview.setLayoutParams(vc1);

        View tl = (View) getView().findViewById(R.id.tablelayout);
        tl.setVisibility(View.GONE);
    }

    private void hideDictionary() {
        isDictionaryVisible = false;
        WebView webview = (WebView) getView().findViewById(R.id.webView);
        webview.setVisibility(View.GONE);
        onlyFavorite = false;
        getActivity().invalidateOptionsMenu();

        View tl = (View) getView().findViewById(R.id.tablelayout);
        tl.setVisibility(View.VISIBLE);

    }


    boolean isHelpMode = false;

    private void toggleHelp() {
        isHelpMode = !isHelpMode;
        Highlight(isHelpMode);
        View view = (View) getView().findViewById(R.id.tblHelp);
        if (isHelpMode)
            view.setVisibility(View.VISIBLE);
        else
            view.setVisibility(View.GONE);

    }

    public void onDrawerCreation() {

    }

    private void openInBrowser(String url) {
        try {
            //  if (url.isEmpty())
            //    Toast.makeText(getActivity().getApplicationContext(), "URL IS EMPTY", Toast.LENGTH_LONG).show();

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        } catch (Exception ex) {
            Toast.makeText(getActivity().getApplicationContext(), "Exception: openInBrowser()-" + ex.toString() + "-" + ex.getStackTrace().toString(), Toast.LENGTH_LONG).show();
        }
    }
}