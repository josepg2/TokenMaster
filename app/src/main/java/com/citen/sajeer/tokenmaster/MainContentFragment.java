package com.citen.sajeer.tokenmaster;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by josepg4 on 21/5/17.
 */

public class MainContentFragment extends Fragment{

    View v;

    public MainContentFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_main_content, container, false);


        if(savedInstanceState == null) {
            FragmentManager childFragMan = getChildFragmentManager();
            FragmentTransaction childFragTrans = childFragMan.beginTransaction();
            AdSpaceListFragment adSpaceListFragment = new AdSpaceListFragment();
            childFragTrans.replace(R.id.main_content, adSpaceListFragment);
            childFragTrans.addToBackStack("B");
            childFragTrans.commit();
        }

        return v;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)  {
        super.onViewCreated(view, savedInstanceState);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) v.findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
               new BottomNavigationView.OnNavigationItemSelectedListener() {
                  @Override
                  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                      FragmentManager childFragMan = getChildFragmentManager();
                      FragmentTransaction childFragTrans = childFragMan.beginTransaction();
                      childFragTrans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                      OnAdSpaceSelectionChangeListner listener = (OnAdSpaceSelectionChangeListner) getActivity();
                       switch (item.getItemId()) {
                           case R.id.action_ad_space_list:
                               listener.changeTitle(0);
                               childFragTrans.replace(R.id.main_content, new AdSpaceListFragment());
                               childFragTrans.addToBackStack("B");
                               childFragTrans.commit();
                               break;
                           case R.id.action_settings:
                               listener.changeTitle(1);
                               childFragTrans.replace(R.id.main_content, new MyPreferenceFragment());
                               childFragTrans.addToBackStack("B");
                               childFragTrans.commit();
                               break;
                           case R.id.action_display_main_content:

                               break;
                           case R.id.action_audio:

                               break;
                           case R.id.action_about:
                               listener.changeTitle(2);
                               childFragTrans.replace(R.id.main_content, new AboutFragment());
                               childFragTrans.addToBackStack("B");
                               childFragTrans.commit();
                               break;
                       }
                       return true;
                  }
               });

    }

}
