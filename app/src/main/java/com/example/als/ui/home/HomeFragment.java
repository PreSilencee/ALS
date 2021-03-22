package com.example.als.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.als.R;
import com.example.als.adapter.ViewPagerAdapter;
import com.example.als.handler.Connectivity;
import com.google.android.material.tabs.TabLayout;

import es.dmoral.toasty.Toasty;

public class HomeFragment extends Fragment{

    private Connectivity device;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             final ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        device = new Connectivity(getContext());

        ViewPager homeFragmentViewPager = root.findViewById(R.id.homeFragmentViewPager);
        setupViewPager(homeFragmentViewPager);
        TabLayout homeFragmentTabLayout = root.findViewById(R.id.homeFragmentTabLayout);
        homeFragmentTabLayout.setupWithViewPager(homeFragmentViewPager);

        return root;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager(), 0);
        adapter.addFragment(new HomeEventFragment(), "Event");
        adapter.addFragment(new HomeOrganizationFragment(), "Organization");

        viewPager.setAdapter(adapter);
    }
    

    @Override
    public void onStart() {
        super.onStart();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
    }

}