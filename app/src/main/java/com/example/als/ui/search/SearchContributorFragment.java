package com.example.als.ui.search;

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
import com.example.als.adapter.SearchContributorListFragmentAdapter;
import com.example.als.handler.Connectivity;
import com.example.als.object.Contributor;
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
 * Use the {@link SearchContributorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchContributorFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    Connectivity device;
    SwipeRefreshLayout searchContributorListSRL;
    List<Contributor> searchContributorAllList;
    SearchContributorListFragmentAdapter searchContributorAllAdapter;
    AlsRecyclerView searchContributorAllRV;

    public static String queryContributor;


    FirebaseUser cUser;

    public SearchContributorFragment() {
        // Required empty public constructor
    }

    public void setQueryContributor(String q){
        queryContributor = q;
    }

    public static SearchContributorFragment newInstance(String q) {
        SearchContributorFragment fragment = new SearchContributorFragment();
        fragment.setQueryContributor(q);
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
        View root = inflater.inflate(R.layout.fragment_search_contributor, container, false);

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

        searchContributorListSRL = root.findViewById(R.id.searchContributorAllListSwipeRefreshLayout);

        View emptySearchContributorView = root.findViewById(R.id.empty_search_contributor_view);
        searchContributorAllRV = root.findViewById(R.id.searchContributorAllListRecyclerView);
        searchContributorAllRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        searchContributorAllRV.setLayoutManager(layoutManager);
        searchContributorAllRV.showIfEmpty(emptySearchContributorView);

        //swipeRefreshLayout function
        searchContributorListSRL.setOnRefreshListener(this);
        searchContributorListSRL.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        searchContributorListSRL.post(new Runnable() {
            @Override
            public void run() {
                searchContributorListSRL.setRefreshing(true);
                searchAllContributor();
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
            searchAllContributor();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Variable.CONTRIBUTOR_REF.removeEventListener(searchAllContributorValueEventListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT).show();
        }
        else{
            searchAllContributor();
        }
    }

    @Override
    public void onRefresh() {
        searchAllContributor();
    }

    public void searchAllContributor(){
        searchContributorAllList = new ArrayList<>();
        Variable.CONTRIBUTOR_REF.addListenerForSingleValueEvent(searchAllContributorValueEventListener);
    }

    private final ValueEventListener searchAllContributorValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            searchContributorAllList.clear();

            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                Contributor contributor = dataSnapshot.getValue(Contributor.class);

                if(contributor != null) {
                    if (contributor.getName().contains(queryContributor) || contributor.getName().toLowerCase().contains(queryContributor)) {
                        searchContributorAllList.add(contributor);
                    }
                }
            }

            searchContributorAllAdapter = new SearchContributorListFragmentAdapter(searchContributorAllList, getContext());
            searchContributorAllAdapter.notifyDataSetChanged();
            searchContributorAllRV.setAdapter(searchContributorAllAdapter);

            searchContributorListSRL.setRefreshing(false);

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("ContributorAllFragment", "Database Error: " + error.getMessage());
        }
    };
}