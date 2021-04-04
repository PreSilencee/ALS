package com.example.als.ui.search;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    public static String queryAll;

    View emptySearchAllView;

    TextView moreEventTV, moreContributorTV, moreOrganizationTV;

    public void setQuery(String q){
        queryAll = q;
    }



    public static SearchAllFragment newInstance(String q){
        SearchAllFragment fragment = new SearchAllFragment();
        fragment.setQuery(q);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

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
        emptySearchAllView = root.findViewById(R.id.empty_search_all_view);
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

        moreContributorTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchActivity.searchViewPager.setCurrentItem(2, true);
            }
        });

        moreOrganizationTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchActivity.searchViewPager.setCurrentItem(3, true);
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
            searchAll();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Variable.EVENT_REF.removeEventListener(searchEventAllValueEventListener);
        Variable.CONTRIBUTOR_REF.removeEventListener(searchContributorAllValueEventListener);
        Variable.ORGANIZATION_REF.removeEventListener(searchOrganizationAllValueEventListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT).show();
        }
        else{
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

        if(searchAllEventCV.getVisibility() == View.GONE && searchAllContributorCV.getVisibility() == View.GONE && searchAllOrganizationCV.getVisibility() == View.GONE){
            emptySearchAllView.setVisibility(View.VISIBLE);
        }
        else{
            emptySearchAllView.setVisibility(View.GONE);
        }


    }

    private final ValueEventListener searchEventAllValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            eventAllList.clear();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            Date dateObj = Calendar.getInstance().getTime();
            Date currentDate = null;
            try {
                currentDate = simpleDateFormat.parse(simpleDateFormat.format(dateObj));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                Event event = dataSnapshot.getValue(Event.class);

                if(event != null && event.getEventStatus().equals(Variable.AVAILABLE) && event.getEventEndDate() != null && (event.getEventTitle().contains(queryAll)
                                || event.getEventDescription().contains(queryAll)
                                || event.getEventTitle().toLowerCase().contains(queryAll)
                                || event.getEventDescription().toLowerCase().contains(queryAll))){
                    try {
                        Date eventEndDate = simpleDateFormat.parse(event.getEventEndDate());
                        if(currentDate != null){
                            if(((currentDate.compareTo(eventEndDate) == 0 || currentDate.compareTo(eventEndDate) < 0))){
                                if(eventAllList.size() < 2){
                                    eventAllList.add(event);
                                }
                            }
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                }
            }

            searchAllEventAdapter = new SearchEventListFragmentAdapter(eventAllList, getContext());
            searchAllEventAdapter.notifyDataSetChanged();
            searchAllEventListRV.setAdapter(searchAllEventAdapter);

            if(searchAllEventAdapter.getItemCount() == 0){
                searchAllEventCV.setVisibility(View.GONE);
            }
            else{
                searchAllEventCV.setVisibility(View.VISIBLE);
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
                    if (contributor.getName().contains(queryAll) || contributor.getName().toLowerCase().contains(queryAll)) {
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
            }
            else{
                searchAllContributorCV.setVisibility(View.VISIBLE);
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
                        if(organization.getOrganizationName().contains(queryAll) || organization.getOrganizationName().toLowerCase().contains(queryAll)){
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
            }
            else{
                searchAllOrganizationCV.setVisibility(View.VISIBLE);
            }

            searchAllListSRL.setRefreshing(false);

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("OrganizationList", "Database Error: " + error.getMessage());
        }
    };
}