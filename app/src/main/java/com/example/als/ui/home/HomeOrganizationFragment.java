package com.example.als.ui.home;

import android.content.Intent;
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
import android.widget.Toast;

import com.example.als.LoginActivity;
import com.example.als.R;
import com.example.als.adapter.HomeOrganizationListFragmentAdapter;
import com.example.als.adapter.RaisedEventListFragmentAdapter;
import com.example.als.handler.Connectivity;
import com.example.als.object.Event;
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

public class HomeOrganizationFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    Connectivity device;
    SwipeRefreshLayout organizationListSRL;

    List<Organization> organizationList;
    HomeOrganizationListFragmentAdapter organizationListAdapter;
    AlsRecyclerView organizationListRV;

    FirebaseUser cUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_home_organization, container, false);

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

        organizationListSRL = root.findViewById(R.id.homeOrganizationListSwipeRefreshLayout);
        View emptyOrganization = root.findViewById(R.id.empty_organization_list_view);
        //recycler view
        organizationListRV = root.findViewById(R.id.homeOrganizationListRecyclerView);
        organizationListRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        organizationListRV.setLayoutManager(layoutManager);
        organizationListRV.showIfEmpty(emptyOrganization);

        //swipeRefreshLayout function
        organizationListSRL.setOnRefreshListener(this);
        organizationListSRL.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        organizationListSRL.post(new Runnable() {
            @Override
            public void run() {
                organizationListSRL.setRefreshing(true);
                loadAllOrganizationList();
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
            loadAllOrganizationList();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT).show();
        }
        else{
            loadAllOrganizationList();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onRefresh() {
        loadAllOrganizationList();
    }

    private void loadAllOrganizationList(){
        organizationList = new ArrayList<>();
        Variable.ORGANIZATION_REF.addListenerForSingleValueEvent(organizationListValueEventListener);
    }

    private final ValueEventListener organizationListValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            organizationList.clear();

            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                Organization organization = dataSnapshot.getValue(Organization.class);

                if(organization != null && organization.getOrganizationVerifyStatus().equals(Variable.VERIFIED)) {
                    organizationList.add(organization);
                }
            }

            organizationListAdapter = new HomeOrganizationListFragmentAdapter(organizationList, getContext());
            organizationListAdapter.notifyDataSetChanged();
            organizationListRV.setAdapter(organizationListAdapter);
            organizationListSRL.setRefreshing(false);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("OrganizationList", "Database Error: " + error.getMessage());
        }
    };

}