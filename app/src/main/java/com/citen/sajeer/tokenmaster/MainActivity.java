package com.citen.sajeer.tokenmaster;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

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

            AdSpaceListFragment adSpaceListFragment = new AdSpaceListFragment();
            adSpaceListFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, adSpaceListFragment)
                    .commit();
        }

        dbHelper = DbHelper.getInstance(getApplicationContext());


    }


    @Override
    public void onSelectionChanged(int adSpaceIndex) {
        AdsListFragment adsListFragment = (AdsListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.ads_list_fragment);

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
}
