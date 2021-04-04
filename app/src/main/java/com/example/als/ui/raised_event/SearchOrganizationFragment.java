package com.example.als.ui.raised_event;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.als.LoginActivity;
import com.example.als.R;
import com.example.als.adapter.HomeOrganizationListFragmentAdapter;
import com.example.als.adapter.SearchContributorListFragmentAdapter;
import com.example.als.handler.Connectivity;
import com.example.als.object.Contributor;
import com.example.als.object.Organization;
import com.example.als.object.Variable;
import com.example.als.widget.AlsRecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchOrganizationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchOrganizationFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    Connectivity device;
    SwipeRefreshLayout searchOrganizationListSRL;
    List<Organization> searchOrganizationAllList;
    HomeOrganizationListFragmentAdapter searchOrganizationAllAdapter;
    AlsRecyclerView searchOrganizationAllRV;

    FirebaseUser cUser;
    public static String queryOrganization;

    public void setQueryOrganization(String q){
        queryOrganization = q;
    }

    public SearchOrganizationFragment() {
        // Required empty public constructor
    }

    public static SearchOrganizationFragment newInstance(String q) {
        SearchOrganizationFragment fragment = new SearchOrganizationFragment();
        fragment.setQueryOrganization(q);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_search_organization, container, false);

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

        searchOrganizationListSRL = root.findViewById(R.id.searchOrganizationAllListSwipeRefreshLayout);

        View emptySearchOrganizationView = root.findViewById(R.id.empty_search_organization_view);
        searchOrganizationAllRV = root.findViewById(R.id.searchOrganizationAllListRecyclerView);
        searchOrganizationAllRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        searchOrganizationAllRV.setLayoutManager(layoutManager);
        searchOrganizationAllRV.showIfEmpty(emptySearchOrganizationView);

        //swipeRefreshLayout function
        searchOrganizationListSRL.setOnRefreshListener(this);
        searchOrganizationListSRL.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        searchOrganizationListSRL.post(new Runnable() {
            @Override
            public void run() {
                searchOrganizationListSRL.setRefreshing(true);
                searchAllOrganization();
            }
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT).show();
        }
        else{
            searchAllOrganization();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT).show();
        }
        else{
            searchAllOrganization();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Variable.ORGANIZATION_REF.removeEventListener(searchAllOrganizationValueEventListener);
    }

    public void searchAllOrganization(){
        searchOrganizationAllList = new ArrayList<>();
        Variable.ORGANIZATION_REF.addListenerForSingleValueEvent(searchAllOrganizationValueEventListener);
    }

    private final ValueEventListener searchAllOrganizationValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            searchOrganizationAllList.clear();

            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                Organization organization = dataSnapshot.getValue(Organization.class);

                if(organization != null){
                    if(organization.getOrganizationVerifyStatus().equals(Variable.VERIFIED)){
                        if(organization.getOrganizationName().contains(queryOrganization) || organization.getOrganizationName().toLowerCase().contains(queryOrganization)){
                            if(searchOrganizationAllList.size() < 2){
                                searchOrganizationAllList.add(organization);
                            }

                        }
                    }
                }
            }

            searchOrganizationAllAdapter = new HomeOrganizationListFragmentAdapter(searchOrganizationAllList, getContext());
            searchOrganizationAllAdapter.notifyDataSetChanged();
            searchOrganizationAllRV.setAdapter(searchOrganizationAllAdapter);

            searchOrganizationListSRL.setRefreshing(false);

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("OrganizationAllFragment", "Database Error: " + error.getMessage());
        }
    };

    @Override
    public void onRefresh() {
        searchAllOrganization();
    }
}