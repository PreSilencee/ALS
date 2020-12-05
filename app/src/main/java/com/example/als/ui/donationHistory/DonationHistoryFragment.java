package com.example.als.ui.donationHistory;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.als.R;
import com.example.als.adapter.DonationHistoryListFragmentAdapter;
import com.example.als.adapter.MessageChatItemAdapter;
import com.example.als.handler.Connectivity;
import com.example.als.object.Donation;
import com.example.als.object.Message;
import com.example.als.object.Variable;
import com.example.als.ui.message.MessageChatActivity;
import com.example.als.widget.AlsRecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DonationHistoryFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "DonationHistory";
    private DonationHistoryListFragmentAdapter donationHistoryListFragmentAdapter;
    private List<Donation> donationHistoryList;

    private SwipeRefreshLayout donationHistoryListSRL;
    private AlsRecyclerView donationHistoryRecyclerView;

    FirebaseUser cUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_donation_history, container, false);

        donationHistoryRecyclerView = root.findViewById(R.id.donationHistoryListRecyclerView);
        donationHistoryRecyclerView.setHasFixedSize(true);
        donationHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        donationHistoryRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
        View emptyDonationHistoryView = root.findViewById(R.id.empty_donation_history);
        donationHistoryRecyclerView.showIfEmpty(emptyDonationHistoryView);

        donationHistoryListSRL = root.findViewById(R.id.donationHistoryListSwipeRefreshLayout);


        //swipeRefreshLayout function
        donationHistoryListSRL.setOnRefreshListener(this);
        donationHistoryListSRL.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        donationHistoryListSRL.post(new Runnable() {
            @Override
            public void run() {
                loadDonationHistoryList();
                donationHistoryListSRL.setRefreshing(true);
            }
        });

        loadDonationHistoryList();

        return root;
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

                    donationHistoryListFragmentAdapter = new DonationHistoryListFragmentAdapter(getContext(), donationHistoryList);
                    donationHistoryListFragmentAdapter.notifyDataSetChanged();
                    donationHistoryRecyclerView.setAdapter(donationHistoryListFragmentAdapter);
                    donationHistoryListSRL.setRefreshing(false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "databaseError: "+error.getMessage());
                }
            });
        }
    }
}