package com.example.als.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.als.LoginActivity;
import com.example.als.R;
import com.example.als.adapter.HomeOrganizationListFragmentAdapter;
import com.example.als.adapter.RaisedEventListFragmentAdapter;
import com.example.als.adapter.SearchContributorListFragmentAdapter;
import com.example.als.adapter.SearchEventListFragmentAdapter;
import com.example.als.handler.Connectivity;
import com.example.als.object.Contributor;
import com.example.als.object.Event;
import com.example.als.object.Organization;
import com.example.als.object.Variable;
import com.example.als.widget.AlsRecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class SearchAllFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    Connectivity device;
    SwipeRefreshLayout searchAllListSRL;

    List<Event> eventAllList;
    SearchEventListFragmentAdapter searchAllEventAdapter;
    AlsRecyclerView searchAllEventListRV;

    List<Contributor> contributorAllList;
    SearchContributorListFragmentAdapter searchContributorListFragmentAdapter;
    AlsRecyclerView searchAllContributorListRV;

    List<Organization> organizationAllList;
    HomeOrganizationListFragmentAdapter searchOrganizationListFragmentAdapter;
    AlsRecyclerView searchAllOrganizationListRV;

    CardView searchAllEventCV, searchAllContributorCV, searchAllOrganizationCV;
    FirebaseUser cUser;

    String query;
    String secondQuery;

    boolean firstQueryState = true;
    boolean secondQueryState = true;
    boolean thirdQueryState = true;

    TextView moreEventTV, moreContributorTV, moreOrganizationTV;

    public SearchAllFragment(String q){
        this.query = q;
    }

//    public static SearchAllFragment newInstance(String q){
//        SearchAllFragment fragment = new SearchAllFragment();
//        Bundle args = new Bundle();
//        args.putString(Variable.SEARCH_ITEM, q);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if(secondQuery != null){
//            query = secondQuery;
//        }
//        else
//        if(getArguments() != null){
//            query = getArguments().getString(Variable.SEARCH_ITEM);
//        }
//    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        if(secondQuery != null){
//            query = secondQuery;
//        }
//        else
//        if(getArguments() != null){
//            query = getArguments().getString(Variable.SEARCH_ITEM);
//            getArguments().remove(Variable.SEARCH_ITEM);
//        }
        View root = inflater.inflate(R.layout.fragment_search_all, container, false);

        device = new Connectivity(getContext());

        if(!device.haveNetwork()){
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_LONG).show();
        }
        else{
            cUser = FirebaseAuth.getInstance().getCurrentUser();
        }

        if(cUser == null){
            //show error message to user
            Toasty.error(requireContext(), "Session is expired. Please relogin.", Toast.LENGTH_SHORT,true).show();

            //intent user to login page (relogin)
            Intent i = new Intent(requireActivity(), LoginActivity.class);

            //clear the background task
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }

        searchAllListSRL = root.findViewById(R.id.searchAllListSwipeRefreshLayout);

        //cardview
        searchAllEventCV = root.findViewById(R.id.searchAllEventCardView);
        searchAllContributorCV = root.findViewById(R.id.searchAllContributorCardView);
        searchAllOrganizationCV = root.findViewById(R.id.searchAllOrganizationCardView);

        //textview
        moreEventTV = root.findViewById(R.id.moreEventTextView);
        moreContributorTV = root.findViewById(R.id.moreContributorTextView);
        moreOrganizationTV = root.findViewById(R.id.moreOrganizationTextView);

        //recycler view
        searchAllEventListRV = root.findViewById(R.id.searchEventListRecyclerView);
        searchAllEventListRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(requireActivity());
        searchAllEventListRV.setLayoutManager(layoutManager1);

        searchAllContributorListRV = root.findViewById(R.id.searchContributorListRecyclerView);
        searchAllContributorListRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(requireActivity());
        searchAllContributorListRV.setLayoutManager(layoutManager2);

        searchAllOrganizationListRV = root.findViewById(R.id.searchOrganizationListRecyclerView);
        searchAllOrganizationListRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager3 = new LinearLayoutManager(requireActivity());
        searchAllOrganizationListRV.setLayoutManager(layoutManager3);

        //swipeRefreshLayout function
        searchAllListSRL.setOnRefreshListener(this);
        searchAllListSRL.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        searchAllListSRL.post(new Runnable() {
            @Override
            public void run() {
                searchAllListSRL.setRefreshing(true);
                searchAll();
            }
        });

        moreEventTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchActivity.searchViewPager.setCurrentItem(1, true);
            }
        });

        return root;
    }

    @Override
    public void onRefresh() {
        searchAll();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT).show();
        }
        else{
            //requireContext().registerReceiver(searchReceiver, new IntentFilter("KEY"));
            searchAll();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Variable.EVENT_REF.removeEventListener(searchEventAllValueEventListener);
        Variable.CONTRIBUTOR_REF.removeEventListener(searchContributorAllValueEventListener);
        Variable.ORGANIZATION_REF.removeEventListener(searchOrganizationAllValueEventListener);
        //requireContext().unregisterReceiver(searchReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT).show();
        }
        else{
            //requireContext().registerReceiver(searchReceiver, new IntentFilter("KEY"));
            searchAll();
        }
    }

    public void searchAll(){
        eventAllList = new ArrayList<>();
        contributorAllList = new ArrayList<>();
        organizationAllList = new ArrayList<>();
        Variable.EVENT_REF.addListenerForSingleValueEvent(searchEventAllValueEventListener);
        Variable.CONTRIBUTOR_REF.addListenerForSingleValueEvent(searchContributorAllValueEventListener);
        Variable.ORGANIZATION_REF.addListenerForSingleValueEvent(searchOrganizationAllValueEventListener);


//        Log.d("Data1: " , String.valueOf(firstQueryState));
//        Log.d("Data2: " , String.valueOf(secondQueryState));
//        Log.d("Data3: " , String.valueOf(thirdQueryState));
//        if(!firstQueryState && !secondQueryState && !thirdQueryState){
//            SearchActivity.searchViewPager.setVisibility(View.GONE);
//            SearchActivity.emptySearchView.setVisibility(View.VISIBLE);
////            Intent i = new Intent("FRAGMENT_S");
////            i.putExtra(Variable.FRAGMENT_STATE, Variable.NOTVISIBLE);
////            requireContext().sendBroadcast(i);
//        }
//        else{
////            Intent i = new Intent("FRAGMENT_S");
////            i.putExtra(Variable.FRAGMENT_STATE, Variable.VISIBLE);
////            requireContext().sendBroadcast(i);
//        }

    }

    private final ValueEventListener searchEventAllValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            eventAllList.clear();

            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                Event event = dataSnapshot.getValue(Event.class);

                if(event != null &&
                        (event.getEventTitle().contains(query)
                                || event.getEventDescription().contains(query)
                                || event.getEventTitle().toLowerCase().contains(query)
                                || event.getEventDescription().toLowerCase().contains(query))){
                    if(eventAllList.size() < 2){
                        eventAllList.add(event);
                    }

                }
            }

            searchAllEventAdapter = new SearchEventListFragmentAdapter(eventAllList, getContext());
            searchAllEventAdapter.notifyDataSetChanged();
            searchAllEventListRV.setAdapter(searchAllEventAdapter);

            if(searchAllEventAdapter.getItemCount() == 0){
                searchAllEventCV.setVisibility(View.GONE);
                firstQueryState = false;
            }


        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("EventAllFragment", "Database Error: " + error.getMessage());
        }
    };

    private final ValueEventListener searchContributorAllValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            contributorAllList.clear();

            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                Contributor contributor = dataSnapshot.getValue(Contributor.class);

                if(contributor != null) {
                    if (contributor.getName().contains(query) || contributor.getName().toLowerCase().contains(query)) {
                        if(contributorAllList.size() < 2){
                            contributorAllList.add(contributor);
                        }

                    }
                }
            }

            searchContributorListFragmentAdapter = new SearchContributorListFragmentAdapter(contributorAllList, getContext());
            searchContributorListFragmentAdapter.notifyDataSetChanged();
            searchAllContributorListRV.setAdapter(searchContributorListFragmentAdapter);

            if(searchContributorListFragmentAdapter.getItemCount() == 0){
                searchAllContributorCV.setVisibility(View.GONE);
                thirdQueryState = false;
            }

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("ContributorAllFragment", "Database Error: " + error.getMessage());
        }
    };

    private final ValueEventListener searchOrganizationAllValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            organizationAllList.clear();

            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                Organization organization = dataSnapshot.getValue(Organization.class);

                if(organization != null){
                    if(organization.getOrganizationVerifyStatus().equals(Variable.VERIFIED)){
                        if(organization.getOrganizationName().contains(query) || organization.getOrganizationName().toLowerCase().contains(query)){
                            if(organizationAllList.size() < 2){
                                organizationAllList.add(organization);
                            }

                        }
                    }
                }
            }

            searchOrganizationListFragmentAdapter = new HomeOrganizationListFragmentAdapter(organizationAllList, getContext());
            searchOrganizationListFragmentAdapter.notifyDataSetChanged();
            searchAllOrganizationListRV.setAdapter(searchOrganizationListFragmentAdapter);

            if(searchOrganizationListFragmentAdapter.getItemCount() == 0){
                searchAllOrganizationCV.setVisibility(View.GONE);
                secondQueryState = false;
            }

            searchAllListSRL.setRefreshing(false);

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("OrganizationList", "Database Error: " + error.getMessage());
        }
    };

//    private final BroadcastReceiver searchReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            //newInstance(intent.getStringExtra(Variable.SEARCH_ITEM));
//            //Log.d("Data", intent.getStringExtra(Variable.SEARCH_ITEM));
//        }
//    };
}