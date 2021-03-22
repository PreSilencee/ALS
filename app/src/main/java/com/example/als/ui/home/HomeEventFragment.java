package com.example.als.ui.home;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.als.R;
import com.example.als.adapter.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;

public class HomeEventFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_home_event, container, false);;

        ViewPager homeFragmentViewPager = root.findViewById(R.id.homeEventFragmentViewPager);
        setupViewPager(homeFragmentViewPager);
        TabLayout homeFragmentTabLayout = root.findViewById(R.id.homeEventFragmentTabLayout);
        homeFragmentTabLayout.setupWithViewPager(homeFragmentViewPager);

        return root;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager(), 0);
        adapter.addFragment(new HomeAvailableEventFragment(), "Available");
        adapter.addFragment(new HomeUpcomingEventFragment(), "Upcoming");

        viewPager.setAdapter(adapter);
    }
}