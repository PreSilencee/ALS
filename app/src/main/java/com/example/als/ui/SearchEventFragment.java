package com.example.als.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.example.als.adapter.SearchEventListFragmentAdapter;
import com.example.als.handler.Connectivity;
import com.example.als.object.Event;
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
 * Use the {@link SearchEventFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchEventFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    Connectivity device;
    SwipeRefreshLayout searchEventListSRL;
    List<Event> searchEventAllList;
    SearchEventListFragmentAdapter searchEventAllAdapter;
    AlsRecyclerView searchEventAllRV;

    String query;
    String secondQuery;

    FirebaseUser cUser;

    public SearchEventFragment() {
        // Required empty public constructor
    }

    public static SearchEventFragment newInstance(String q) {
        SearchEventFragment fragment = new SearchEventFragment();
        Bundle args = new Bundle();
        args.putString(Variable.SEARCH_ITEM, q);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(secondQuery != null){
            query = secondQuery;
        }
        else
        if(getArguments() != null){
            query = getArguments().getString(Variable.SEARCH_ITEM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_search_event, container, false);

        if(secondQuery != null){
            query = secondQuery;
        }
        else
        if(getArguments() != null){
            query = getArguments().getString(Variable.SEARCH_ITEM);
        }

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

        searchEventListSRL = root.findViewById(R.id.searchEventAllListSwipeRefreshLayout);

        View emptySearchEventView = root.findViewById(R.id.empty_search_event_view);
        searchEventAllRV = root.findViewById(R.id.searchEventAllListRecyclerView);
        searchEventAllRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        searchEventAllRV.setLayoutManager(layoutManager);
        searchEventAllRV.showIfEmpty(emptySearchEventView);

        //swipeRefreshLayout function
        searchEventListSRL.setOnRefreshListener(this);
        searchEventListSRL.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        searchEventListSRL.post(new Runnable() {
            @Override
            public void run() {
                searchEventListSRL.setRefreshing(true);
                searchAllEvent();
            }
        });


        return root;
    }

    @Override
    public void onRefresh() {
        searchAllEvent();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT).show();
        }
        else{
            requireContext().registerReceiver(searchEventReceiver, new IntentFilter("KEY"));
            searchAllEvent();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT).show();
        }
        else{
            requireContext().registerReceiver(searchEventReceiver, new IntentFilter("KEY"));
            searchAllEvent();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Variable.EVENT_REF.removeEventListener(searchAllEventValueEventListener);
        requireContext().unregisterReceiver(searchEventReceiver);
    }

    public void searchAllEvent(){
        searchEventAllList = new ArrayList<>();
        Variable.EVENT_REF.addListenerForSingleValueEvent(searchAllEventValueEventListener);

    }

    private final ValueEventListener searchAllEventValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            searchEventAllList.clear();

            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                Event event = dataSnapshot.getValue(Event.class);

                if(event != null &&
                        (event.getEventTitle().contains(query)
                                || event.getEventDescription().contains(query)
                                || event.getEventTitle().toLowerCase().contains(query)
                                || event.getEventDescription().toLowerCase().contains(query))){
                    searchEventAllList.add(event);
                }
            }

            searchEventAllAdapter = new SearchEventListFragmentAdapter(searchEventAllList, getContext());
            searchEventAllAdapter.notifyDataSetChanged();
            searchEventAllRV.setAdapter(searchEventAllAdapter);

            searchEventListSRL.setRefreshing(false);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("EventAllFragment", "Database Error: " + error.getMessage());
        }
    };

    private final BroadcastReceiver searchEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            secondQuery = intent.getStringExtra(Variable.SEARCH_ITEM);
        }
    };
}