package com.example.als.ui.more;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.als.R;
import com.example.als.adapter.DonationHistoryListFragmentAdapter;
import com.example.als.handler.Connectivity;
import com.example.als.object.Donation;
import com.example.als.object.Variable;
import com.example.als.ui.search.SearchActivity;
import com.example.als.widget.AlsRecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class DonationHistoryActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = "DonationHistory";
    private DonationHistoryListFragmentAdapter donationHistoryListFragmentAdapter;
    private List<Donation> donationHistoryList;
    private Connectivity device;
    private SwipeRefreshLayout donationHistoryListSRL;
    private AlsRecyclerView donationHistoryRecyclerView;

    FirebaseUser cUser;

    //toolbar
    Toolbar donationHistoryCustomizeToolbar;

    //image button
    ImageButton donationHistorySearchImageBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_history);

        donationHistoryCustomizeToolbar = findViewById(R.id.customizeToolbar);
        donationHistorySearchImageBtn = findViewById(R.id.searchImageButton);

        //set up customize toolbar
        setSupportActionBar(donationHistoryCustomizeToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Donation History");

        donationHistorySearchImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SearchActivity.class));
            }
        });

        device = new Connectivity(getApplicationContext());
        donationHistoryRecyclerView = findViewById(R.id.donationHistoryListRecyclerView);
        donationHistoryRecyclerView.setHasFixedSize(true);
        donationHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        donationHistoryRecyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL));
        View emptyDonationHistoryView = findViewById(R.id.empty_donation_history);
        donationHistoryRecyclerView.showIfEmpty(emptyDonationHistoryView);

        donationHistoryListSRL = findViewById(R.id.donationHistoryListSwipeRefreshLayout);


        //swipeRefreshLayout function
        donationHistoryListSRL.setOnRefreshListener(this);
        donationHistoryListSRL.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        donationHistoryListSRL.post(new Runnable() {
            @Override
            public void run() {
                donationHistoryListSRL.setRefreshing(true);
                loadDonationHistoryList();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!device.haveNetwork()) {
            Toasty.error(getApplicationContext(), device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            loadDonationHistoryList();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toasty.error(getApplicationContext(), device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            loadDonationHistoryList();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onRefresh() {
        loadDonationHistoryList();
    }

    private void loadDonationHistoryList(){
        cUser = FirebaseAuth.getInstance().getCurrentUser();

        if(cUser != null){

            donationHistoryList = new ArrayList<>();

            Variable.DONATION_REF.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    donationHistoryList.clear();
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Donation donation = dataSnapshot.getValue(Donation.class);

                        if(donation != null){
                            if(donation.getDonationUserId().equals(cUser.getUid())){
                                donationHistoryList.add(donation);
                            }
                        }

                    }

                    donationHistoryListFragmentAdapter = new DonationHistoryListFragmentAdapter(donationHistoryList, getApplicationContext());
                    donationHistoryListFragmentAdapter.notifyDataSetChanged();
                    donationHistoryRecyclerView.setAdapter(donationHistoryListFragmentAdapter);
                    donationHistoryListSRL.setRefreshing(false);
                    donationHistoryListFragmentAdapter.setOnClickListener(new DonationHistoryListFragmentAdapter.OnDonationListener() {
                        @Override
                        public void onDonationClicked(int position) {
                            Intent i = new Intent(getApplicationContext(), DonationDetailsActivity.class);
                            i.putExtra(Variable.DONATION_SESSION_ID, donationHistoryList.get(position).getDonationId());
                            startActivity(i);
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "databaseError: "+error.getMessage());
                }
            });
        }
    }
}