package com.citen.sajeer.tokenmaster;

import android.app.ActionBar;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements OnAdSpaceSelectionChangeListner {


    DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(findViewById(R.id.fragment_container)!= null){

            if(savedInstanceState != null){
                return;
            }

            //AdSpaceListFragment adSpaceListFragment = new AdSpaceListFragment();
            //adSpaceListFragment.setArguments(getIntent().getExtras());

            MainContentFragment mainContentFragment = new MainContentFragment();
            mainContentFragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, mainContentFragment)
                    .commit();
        }

        setTitle("TOKEN MANAGER MASTER");
    }


    @Override
    public void onSelectionChanged(int adSpaceIndex) {
        AdsListFragment adsListFragment = (AdsListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.ads_list_fragment);

        setTitle("AD SPACE " + Integer.toString(adSpaceIndex + 1));

        if (adsListFragment != null){
            // If description is available, we are in two pane layout
            // so we call the method in DescriptionFragment to update its content
            adsListFragment.setAdSpace(adSpaceIndex);

        } else {
            AdsListFragment newDesriptionFragment = new AdsListFragment();
            Bundle args = new Bundle();

            args.putInt(AdsListFragment.KEY_POSITION,adSpaceIndex);
            newDesriptionFragment.setArguments(args);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the backStack so the User can navigate back
            fragmentTransaction.replace(R.id.fragment_container,newDesriptionFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void changeTitle(int i) {
        if(i == 0)
            setTitle("TOKEN MANAGER MASTER");
        else if(i == 1)
            setTitle("SETTINGS");
        else
            setTitle("ABOUT");
    }

    @Override
    protected void onStop() {
        super.onStop();
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if(actionBar != null) {
            String classTitle = actionBar.getTitle().toString();
            editor.putString("TITLE", classTitle);
            Log.d("onstop" , classTitle.toString());
        }
        editor.apply();

        Log.d("onstop" , "onstop");


    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        setTitle(sharedPref.getString("TITLE","TOKEN MANAGER MASTER"));

        if(!sharedPref.getBoolean("ISTEXTIMAGEAVAIL", false)) {
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.letter_t);
            SharedPreferences.Editor editor = sharedPref.edit();
            try {
                editor.putString("LETTERTPATH", saveToInternalStorage(bm, "letter_t.png"));
                editor.putBoolean("ISTEXTIMAGEAVAIL", true);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                editor.apply();
            }
        }

    }

    @NonNull
    private String saveToInternalStorage(Bitmap thumbnail, String fileName) throws IOException {

        ContextWrapper cw = new ContextWrapper(this.getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File path = new File(directory, fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            // Use the compress method on the BitMap object to write image to the OutputStream
            thumbnail.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(fos != null)
                fos.close();
        }
        return directory.getAbsolutePath();
    }
}
