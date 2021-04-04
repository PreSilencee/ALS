package com.example.als.ui.search;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.example.als.R;
import com.example.als.adapter.ViewPagerAdapter;
import com.example.als.handler.Connectivity;
import com.example.als.ui.raised_event.SearchOrganizationFragment;
import com.google.android.material.tabs.TabLayout;

import es.dmoral.toasty.Toasty;

public class SearchActivity extends AppCompatActivity {

    Toolbar customizeSearchViewToolbar;
    SearchView customizeSearchView;
    public View emptySearchView;

    public static ViewPager searchViewPager;
    public static ViewPagerAdapter searchViewPagerAdapter;

    Connectivity device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        device = new Connectivity(SearchActivity.this);

        customizeSearchViewToolbar = findViewById(R.id.customizeSearchViewToolbar);
        customizeSearchView = findViewById(R.id.customizeSearchView);
        emptySearchView = findViewById(R.id.empty_search_view);

        setSupportActionBar(customizeSearchViewToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        searchViewPager = findViewById(R.id.searchViewPager);

        final TabLayout searchTabLayout = findViewById(R.id.searchTabLayout);

        customizeSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                setupViewPager(searchViewPager, query);
                searchTabLayout.setupWithViewPager(searchViewPager);
                searchViewPager.setVisibility(View.VISIBLE);
                emptySearchView.setVisibility(View.GONE);
                customizeSearchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    private void setupViewPager(ViewPager viewPager, String q) {

        searchViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        searchViewPagerAdapter.addFragment(SearchAllFragment.newInstance(q), "All");
        searchViewPagerAdapter.addFragment(SearchEventFragment.newInstance(q), "Event");
        searchViewPagerAdapter.addFragment(SearchContributorFragment.newInstance(q), "Contributor");
        searchViewPagerAdapter.addFragment(SearchOrganizationFragment.newInstance(q), "Organization");
        viewPager.setAdapter(searchViewPagerAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}