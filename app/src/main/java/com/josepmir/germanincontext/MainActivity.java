package com.josepmir.germanincontext;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.josepmir.germanincontext.derdiedas.DerDieDasFragment;
import com.josepmir.germanincontext.deklination.DeklinationFragment;


import java.util.HashMap;

public class MainActivity extends Activity {

     public static boolean bIS_DEMO = true;

    private DrawerLayout mDrawerLayout;
    private ListView mLeftDrawerList;
    protected ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mRightDrawerLayout;
    private ListView mRightDrawerList;
    protected ActionBarDrawerToggle mRightDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mSections;
    public static String PREFERENCES_NAME = "learnRealGerman";

    private int currentDrawer = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            mSections = getResources().getStringArray(R.array.sections);
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mLeftDrawerList = (ListView) findViewById(R.id.left_drawer);

            mLeftDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mSections));

            mLeftDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectItemLeft(position);
                }
            });

            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);

            getActionBar().setTitle("");


            mDrawerToggle = new ActionBarDrawerToggle(
                    this,                  /* host Activity */
                    mDrawerLayout,         /* DrawerLayout object */
                    R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                    R.string.drawer_open,  /* "open drawer" description for accessibility */
                    R.string.drawer_close  /* "close drawer" description for accessibility */
            ) {
                public void onDrawerClosed(View view) {
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }

                public void onDrawerOpened(View drawerView) {
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }
            };
            mDrawerLayout.setDrawerListener(mDrawerToggle);

            if (savedInstanceState == null) {
                selectItemLeft(0);
            }




/*
        mRightDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mRightDrawerList = (ListView) findViewById(R.id.right_drawer);


        String[] filenames = Common.getDirectoryFiles(getApplicationContext(), "deklination");
        mRightDrawerList.setAdapter(new ArrayAdapter<String>(getApplicationContext(),R.layout.drawer_list_item, filenames));
         mRightDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItemRight(position);
            }
        });*/

        } catch (Exception ex) {
            Toast.makeText(this, "Exception: MainActivity.onCreate()-" + ex.toString() + "-" + ex.getStackTrace().toString(), Toast.LENGTH_LONG).show();

        }
    }


    private void selectItemRight(int position) {
        mRightDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mRightDrawerList);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void selectItemLeft(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = DeklinationFragment.newInstance(false);
                break;
            case 1:
                fragment = DeklinationFragment.newInstance(true); //adjectives
                break;
            case 2:
                fragment = new ZahlenFragment();
                break;
            case 3:
                fragment = new DerDieDasFragment();
                break;
            case 4:
                 showUnlimitedGerman();
                return;
            case 5:
                showFullVersion();
                return;
            default:
                fragment = DeklinationFragment.newInstance(false);
        }
        currentDrawer = position;

        getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment, "LATEST").commit();

        mLeftDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mLeftDrawerList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    final int RQS_GooglePlayServices = 1;
    @Override
    protected void onResume() {
        try {
            super.onResume();

            SharedPreferences settings = this.getSharedPreferences(MainActivity.PREFERENCES_NAME, 0);
            selectItemLeft(settings.getInt("mainCurrentDrawer", currentDrawer));


            //ARE GOOGLE PLAY SERVICES PRESENT?
           /* int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

            if (resultCode == ConnectionResult.SUCCESS){
                Toast.makeText(getApplicationContext(),
                        "isGooglePlayServicesAvailable SUCCESS",
                        Toast.LENGTH_LONG).show();
            }else{
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, RQS_GooglePlayServices);
            }*/

        } catch (Exception ex) {
            Toast.makeText(this, "Exception: OnStart()-" + ex.toString() + "-" + ex.getStackTrace().toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
         getSharedPreferences(MainActivity.PREFERENCES_NAME, 0).edit().putInt("mainCurrentDrawer", currentDrawer).commit();

    }

    private void showUnlimitedGerman() {
        showPackage("com.josepmir.germanincontext");
    }
    private void showFullVersion() {
        showPackage("com.josepmir.germanincontextpro");
    }

    private void showPackage(String packageName) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)));
        }
    }

    public interface rightDrawerCallback {
        void onDrawerCreation();
    }

}

