package com.josepmir.germanincontext.derdiedas;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.josepmir.germanincontext.MainActivity;
import com.josepmir.germanincontext.MyApp;
import com.josepmir.germanincontext.R;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


///WERBUNG: repeats the Question you answered wrong until you do them right

public class DerDieDasFragment extends Fragment {


    int cycleNumber = 0;
    String expected_article = "";
    String current_word = "";

    /* need to exclude these words (possibly more)
    Gehalt - das Gehalt/ die Gehälter ("salary") - der Gehalt/ die Gehalte ("content")
Band - das Band/ die Bänder ("ribbon") - der Band/ die Bände ("bibliographic volume")
Teil - das Teil/ die Teile (physical "piece" e.g. from a machine) - der Teil/ die Teile (conceptual "part" e.g. from a speech)
See - der See/ die Seen ("lake") - die See ("sea", no plural form) - die See/ die Seen (nautical term for "(large) wave")
*/

    public DerDieDasFragment() {

    }


    DictionaryAPI subApi;
    boolean buttonsIgnored=false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View dek = inflater.inflate(R.layout.fragment_derdiedas, container, false);

        dek.findViewById(R.id.btnDer).setOnClickListener(mButtonRat_OnClickListener);
        dek.findViewById(R.id.btnDie).setOnClickListener(mButtonRat_OnClickListener);
        dek.findViewById(R.id.btnDas).setOnClickListener(mButtonRat_OnClickListener);

        LinearLayout layout = (LinearLayout) dek.findViewById(R.id.artikelLinearlayout);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        SeekBar sb = (SeekBar) dek.findViewById(R.id.artikelSeekSchiwerigkeit);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                // if (MainActivity.bIS_DEMO) {
                Toast.makeText(getActivity().getApplicationContext(), "Higher levels only in the FULL Version", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                // TODO Auto-generated method stub



            }
        });

        //Analytics
        Tracker t = ((MyApp) getActivity().getApplication()).getTracker(MyApp.TrackerName.APP_TRACKER);
        t.setScreenName("DerDieDas");
        t.send(new HitBuilders.AppViewBuilder().build());

        return dek;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void BackToBlack(View v) {

        Button b = (Button) v.findViewById(R.id.btnDer);
        b.setTextColor(Color.BLACK);
        b = (Button) v.findViewById(R.id.btnDie);
        b.setTextColor(Color.BLACK);
        b = (Button) v.findViewById(R.id.btnDas);
        b.setTextColor(Color.BLACK);
    }

    TextView middleLabel;


    public void AnswerWasCorrect(View v) {
        BackToBlack(v);

     /*   SeekBar sb = (SeekBar) getView().findViewById(R.id.artikelSeekSchiwerigkeit);
        sb.setKeyProgressIncrement(1);
        sb.setMax(12);
*/
        middleLabel.setText(expected_article + " " + current_word);
        buttonsIgnored=true;
         new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // this code will be executed after 2 seconds
                smallIteration();
                buttonsIgnored=false;
            }
        }, 1000);

    }

    public void smallIteration() {
        SeekBar sb = (SeekBar) getView().findViewById(R.id.artikelSeekSchiwerigkeit);
        sb.setKeyProgressIncrement(1);
        sb.setMax(12);

        int level = 1;
        if (!MainActivity.bIS_DEMO)
            level = sb.getProgress();
        DictionaryAPI.Question entry = subApi.getQuestionByDifficulty(current_word, level);
        expected_article = entry.artikel;
        current_word = entry.wort;
        //update middlelabel!!
        UpdateGUI();
    }

    private static final ScheduledExecutorService worker =
            Executors.newSingleThreadScheduledExecutor();


    final android.os.Handler myHandler = new android.os.Handler();

    private void UpdateGUI() {

        //tv.setText(String.valueOf(i));
        myHandler.post(myRunnable);
    }

    final Runnable myRunnable = new Runnable() {
        public void run() {
            middleLabel.setText(String.valueOf(current_word));
        }
    };

    ProgressDialog progressDialog = null;
    boolean correctAtFirstAttempt = true;
    final View.OnClickListener mButtonRat_OnClickListener = new View.OnClickListener() {
        public void onClick(final View v) {

            int resourceId = v.getId();

            try {
                //hideDictionary();
                if (buttonsIgnored) return;

                Button b = (Button) getView().findViewById(resourceId);
                String selected_button = b.getText().toString();

                if (selected_button.equals(expected_article)) {
                    AnswerWasCorrect(getView());

                    subApi.updateShown(current_word);
                    subApi.updateWasCorrect(current_word, correctAtFirstAttempt);
                    correctAtFirstAttempt = true; //restart value
                } else {
                    b.setTextColor(Color.RED);
                    correctAtFirstAttempt = false;
                }

            } catch (Exception ex) {
                Toast.makeText(getActivity(), "Exception: mButtonRat_OnClickListener()-" + ex.toString() + "-" + ex.getStackTrace().toString(), Toast.LENGTH_LONG).show();
            }
        }
    };


    boolean onlyFavorite = false;

    //As you can see the signature of this method is different from the one of the Activity class. With fragments your method takes a MenuInflater as second parameter. And the fragment’s onCreateOptionsMenu() method also doesn’t return a boolean value.
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.artikel, menu);

        //WHEN WEBVIEW: SHOW FAVORITE TOO
        MenuItem mi = (MenuItem) menu.findItem(R.id.artikelFavorite);
        // mi.setVisible(onlyFavorite);
        mi.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            case R.id.artikelForward:
                actionSkip();
                return true;
            case R.id.artikelFavorite:
                //update to dictionary dictionaryCurrentWord
                return true;
            case R.id.artikelHelp:
                toggleHelp();
                return true;
        }
        return true;
    }

    public void actionSkip() {
        AnswerWasCorrect(getView());
        updateDictionary(current_word);
    }


    boolean isDictionaryVisible = false;

    private void showDictionary(String current) {

        isDictionaryVisible = true;
        WebView webview = (WebView) getView().findViewById(R.id.artikelWebView);
        webview.setWebViewClient(new WebViewClient());
        webview.loadData("Loading DictionaryAPI...", "text/plain", "utf-8");
        webview.loadUrl("http://de.thefreedictionary.com/_/dict.aspx?rd=1&word=" + current);
        webview.setVisibility(View.VISIBLE);
        webview.getSettings().setBuiltInZoomControls(true);
        onlyFavorite = true;
        getActivity().invalidateOptionsMenu();

        //find the height of the screen
        LinearLayout ll = (LinearLayout) getView().findViewById(R.id.artikelLinearlayout);
        int dictionary_height = (int) (ll.getHeight() * 0.5f);
        //define the dictionary height as 40% of it
        ViewGroup.LayoutParams vc1 = webview.getLayoutParams();
        vc1.height = dictionary_height;
        webview.setLayoutParams(vc1);


        TableLayout tl = (TableLayout) getView().findViewById(R.id.artikelTablelayout);
        tl.setVisibility(View.GONE);

    }

    private void hideDictionary() {
        isDictionaryVisible = false;
        WebView webview = (WebView) getView().findViewById(R.id.artikelWebView);
        webview.setVisibility(View.GONE);
        onlyFavorite = false;
        getActivity().invalidateOptionsMenu();

        TableLayout tl = (TableLayout) getView().findViewById(R.id.artikelTablelayout);
        tl.setVisibility(View.VISIBLE);
    }

    private void updateDictionary(String current) {
        if (!isDictionaryVisible) return;
        WebView mWebView = (WebView) getView().findViewById(R.id.artikelWebView);
        mWebView.loadUrl("about:blank");
        mWebView.loadUrl("http://de.thefreedictionary.com/_/dict.aspx?rd=1&word=" + current);
    }

    boolean isHelpMode = false;

    private void toggleHelp() {
        isHelpMode = !isHelpMode;
        if (isHelpMode)
            showDictionary(current_word);
        else
            hideDictionary();
    }


    @Override
    public void onPause() {
        super.onPause();

        SeekBar sb = (SeekBar) getView().findViewById(R.id.artikelSeekSchiwerigkeit);
        int difficulty = sb.getProgress();

        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFERENCES_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("artikelDifficulty", difficulty);
        editor.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFERENCES_NAME, 0);
        int difficulty = settings.getInt("artikelDifficulty", 0);
        SeekBar sb = (SeekBar) getView().findViewById(R.id.artikelSeekSchiwerigkeit);
        sb.setProgress(difficulty);


        if (subApi == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Loading data... Please wait");
            progressDialog.show();

            (new android.os.Handler()).postDelayed(new Runnable() {

                public void run() {
                    subApi = DictionaryAPI.getInstance(getActivity().getApplicationContext());
                    middleLabel = (TextView) getView().findViewById(R.id.artikelWord);
                    smallIteration();
                    if (progressDialog != null)
                        progressDialog.cancel();
                }
            }, 100);
        }


    }
}