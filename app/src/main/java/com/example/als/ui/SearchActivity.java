package com.example.als.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.example.als.LoginActivity;
import com.example.als.R;
import com.example.als.adapter.ViewPagerAdapter;
import com.example.als.handler.Connectivity;
import com.example.als.object.Variable;
import com.example.als.ui.raised_event.RaisedAllEventFragment;
import com.example.als.ui.raised_event.RaisedAvailableEventFragment;
import com.example.als.ui.raised_event.RaisedDeclinedEventFragment;
import com.example.als.ui.raised_event.RaisedPendingEventFragment;
import com.example.als.ui.raised_event.RaisedUpcomingEventFragment;
import com.google.android.material.tabs.TabLayout;

import es.dmoral.toasty.Toasty;

public class SearchActivity extends AppCompatActivity {

    Toolbar customizeSearchViewToolbar;
    SearchView customizeSearchView;
    public static View emptySearchView;

    String fragmentState;

    public static ViewPager searchViewPager;
    public static ViewPagerAdapter searchViewPagerAdapter;

    Connectivity device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        device = new Connectivity(SearchActivity.this);

        Intent i = new Intent("KEY");
        sendBroadcast(i);

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
                Bundle bundle = new Bundle();
                bundle.putString(Variable.SEARCH_ITEM, query);
                setupViewPager(searchViewPager, bundle);
                searchTabLayout.setupWithViewPager(searchViewPager);
                searchViewPager.setVisibility(View.VISIBLE);
                emptySearchView.setVisibility(View.GONE);
                customizeSearchView.clearFocus();
//                Intent i = new Intent("KEY");
//                i.putExtra(Variable.SEARCH_ITEM, query);
//                sendBroadcast(i);
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

    private void setupViewPager(ViewPager viewPager, Bundle bundle) {
        searchViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
//        SearchAllFragment searchAllFragment =  SearchAllFragment.newInstance(bundle.getString(Variable.SEARCH_ITEM));
        SearchEventFragment searchEventFragment = SearchEventFragment.newInstance(bundle.getString(Variable.SEARCH_ITEM));
        SearchContributorFragment searchContributorFragment = SearchContributorFragment.newInstance(bundle.getString(Variable.SEARCH_ITEM));
        searchViewPagerAdapter.addFragment(new SearchAllFragment(bundle.getString(Variable.SEARCH_ITEM)), "All");
        //searchViewPagerAdapter.addFragment(searchEventFragment, "Event");
        //searchViewPagerAdapter.addFragment(searchContributorFragment, "Contributor");

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
        else{
            //registerReceiver(fragmentStateReceiver, new IntentFilter("FRAGMENT_S"));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //unregisterReceiver(fragmentStateReceiver);
    }

//    private final BroadcastReceiver fragmentStateReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            fragmentState = intent.getStringExtra(Variable.FRAGMENT_STATE);
//            Log.d("Data", intent.getStringExtra(Variable.FRAGMENT_STATE));
//            if(fragmentState.equals(Variable.NOTVISIBLE)){
//                searchViewPager.setVisibility(View.GONE);
//                emptySearchView.setVisibility(View.VISIBLE);
//            }
//        }
//    };
}