package com.xurent.livedance.ui;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.xurent.livedance.R;
import com.xurent.livedance.ui.viewpager.AppBarFrag1;
import com.xurent.livedance.ui.viewpager.AppBarFrag2;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class IndexFragment extends Fragment {



    private TabLayout tabLayout;


    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.frag_index,null);
        tabLayout=view.findViewById(R.id.tabLayoutHome);
        Fragment fragment=new AppBarFrag1();
        FragmentManager fm=getChildFragmentManager();
        //事务
        FragmentTransaction ft=fm.beginTransaction();
        ft.replace(R.id.index_context,fragment);
        ft.commit();
        listnerAppBar();
        return view;
    }


    private void listnerAppBar(){

        FragmentManager fm=getChildFragmentManager();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
         @Override
        public void onTabSelected(TabLayout.Tab tab) {
          int index= tab.getPosition();
          Fragment fragment=new AppBarFrag1();
          System.out.println("index-->"+index);
          switch (index){
              case 0:fragment=new AppBarFrag1();
                  break;
              case 1:fragment=new AppBarFrag2();
                  break;
              case 2:
                  break;
              case 3:
                  break;
              case 4:
                  break;
          }
            //事务
            FragmentTransaction ft=fm.beginTransaction();
            ft.replace(R.id.index_context,fragment);
            ft.commit();

    }
    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }
    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
    });

    }



}
