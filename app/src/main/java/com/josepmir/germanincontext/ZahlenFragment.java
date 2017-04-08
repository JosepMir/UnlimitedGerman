package com.josepmir.germanincontext;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Locale;
import java.util.Random;

/**
 * Created by josep on 28.04.14.
 */
public class ZahlenFragment extends Fragment implements TextToSpeech.OnInitListener {

    TextToSpeech tts;
    Random rnd = new Random();
    int rndNumber = 0;
    SeekBar sb;

    public ZahlenFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View dek = inflater.inflate(R.layout.fragment_zahlen, container, false);

        dek.findViewById(R.id.zahlenBtnGenerateNewNumber).setOnClickListener(mGlobal_OnClickListener);
        dek.findViewById(R.id.zahlenBtnRepeat).setOnClickListener(mGlobal_OnClickListener);
        dek.findViewById(R.id.zahlenBtnShowSolution).setOnClickListener(mGlobal_OnClickListener);

        zahlenEditTextNumber = (TextView) dek.findViewById(R.id.zahlenEditTextNumber);

        TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.WRAP_CONTENT);
        //params.addRule( LinearLayout.ALIGN_PARENT_TOP, 0);


        tts = new TextToSpeech(getActivity().getApplicationContext(), this);
        zahlenEditTextNumber.setHint("Touch me!");
        /*TextView et = (TextView) dek.findViewById(R.id.zahlenEditTextNumber);
        et.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    CheckAnswer();
                    // Toast.makeText(v.getContext(), "heeeeeee", Toast.LENGTH_LONG).show();

                    //Log.i(LOG_TAG, "IME_ACTION_DONE");
                    return true;
                }
                return false;
            }
        });*/

        sb = (SeekBar) dek.findViewById(R.id.zahlenSeekSchwierigkeit);
        sb.setKeyProgressIncrement(1);
        sb.setMax(5);

        zahlenEditTextNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                //warning: fragile
                zahlenEditTextNumber.removeTextChangedListener(this);
                if (SehrGut) {
                    zahlenEditTextNumber.setText(SehrGutNum);
                } else {
                    CheckAnswer();
                }
                zahlenEditTextNumber.addTextChangedListener(this);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        //Analytics
        Tracker t = ((MyApp) getActivity().getApplication()).getTracker(MyApp.TrackerName.APP_TRACKER);
        t.setScreenName("Zahlen");
        t.send(new HitBuilders.AppViewBuilder().build());

        return dek;
    }

    //Global On click listener for all views
    final View.OnClickListener mGlobal_OnClickListener = new View.OnClickListener() {
        public void onClick(final View v) {
            if (TTS_FAILED) {
                Toast.makeText(getActivity(), "No German TextToSpeech installed on your phone", Toast.LENGTH_SHORT).show();
                return;
            }

            switch (v.getId()) {
                case R.id.zahlenBtnGenerateNewNumber:
                    generateNewNumber();
                    break;
                case R.id.zahlenBtnRepeat:
                    speakOut(String.valueOf(rndNumber));
                    break;
                case R.id.zahlenBtnShowSolution:
                    Toast.makeText(getActivity(), String.valueOf(rndNumber), Toast.LENGTH_LONG).show();
                    break;
            }


        }
    };

    TextView zahlenEditTextNumber;
    boolean SehrGut = false;
    String SehrGutNum = "";

    public void CheckAnswer() {
        if (TTS_FAILED) {
            Toast.makeText(getActivity(), "No German TextToSpeech installed on your phone", Toast.LENGTH_SHORT).show();
            return;
        }

        String enteredText = zahlenEditTextNumber.getText().toString().trim();
        if (String.valueOf(rndNumber).equals(enteredText.trim())) {
            speakOut("Sehr gut!");
            SehrGut = true;
            SehrGutNum = enteredText;
            (new android.os.Handler()).postDelayed(new Runnable() {

                public void run() {
                    zahlenEditTextNumber.setHint("");
                    SehrGut = false;
                    generateNewNumber();

                }
            }, 800);
        } else {
            //speakOut("Versuchen Sie es bitte nochmal");
        }
    }

    //As you can see the signature of this method is different from the one of the Activity class. With fragments your method takes a MenuInflater as second parameter. And the fragment’s onCreateOptionsMenu() method also doesn’t return a boolean value.
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Inflate the menu; this adds items to the action bar if it is present.
        //   inflater.inflate(R.menu.main, menu);

        //((MainActivity) getActivity()).bIS_DEMO;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (((MainActivity) getActivity()).mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return true;
    }


    @Override
    public void onPause() {
        super.onPause();

        //TODO: also save speed.

        SeekBar sb = (SeekBar) getView().findViewById(R.id.zahlenSeekSchwierigkeit);
        int difficulty = sb.getProgress();

        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFERENCES_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("zahlenDifficulty", difficulty);
        editor.commit();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFERENCES_NAME, 0);
        int difficulty = settings.getInt("zahlenDifficulty", 100);
        SeekBar sb = (SeekBar) getView().findViewById(R.id.zahlenSeekSchwierigkeit);
        sb.setProgress(difficulty);


        (new android.os.Handler()).postDelayed(new Runnable() {

            public void run() {
                generateNewNumber();
                zahlenEditTextNumber.requestFocus();
            }
        },300);
    }


    boolean TTS_FAILED = false;

    public void onInit(int status) {
        try {
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {

                int result = tts.setLanguage(Locale.GERMAN);

                if (result == android.speech.tts.TextToSpeech.LANG_MISSING_DATA || result == android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(getActivity(), "No German TextToSpeech installed in your phone", Toast.LENGTH_SHORT).show();
                    TTS_FAILED = true;
                }

            } else {
                Toast.makeText(getActivity(), "TextToSpeech initialisation failed", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            Toast.makeText(getActivity(), "onInit() error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void generateNewNumber() {
        zahlenEditTextNumber.setText("");

        double range = Math.pow(10, sb.getProgress() + 1);
        rndNumber = rnd.nextInt((int) range); //1 000 000
        speakOut(String.valueOf(rndNumber));
    }


    private void speakOut(String text) {
        try {
            // By default the value is 1.0 You can set lower values than 1.0 to decrease pitch level or greater values for increase pitch level.
            //tts.setPitch(0.6);

            //This also will take default of 1.0 value. You can double the speed rate by setting 2.0 or make half the speed level by setting 0.5
            //tts.setSpeechRate(2);
            tts.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null);
        } catch (Exception ex) {
            Log.d("SI", ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            if (tts != null) {
                tts.stop();
                tts.shutdown();
            }
            super.onDestroy();
        }
    }


}