package com.example.als.ui.raised_event;

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
import com.example.als.adapter.RaisedEventListFragmentAdapter;
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

public class RaisedDeclinedEventFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    Connectivity device;
    SwipeRefreshLayout eventDeclinedListSRL;

    List<Event> eventDeclinedList;
    RaisedEventListFragmentAdapter eventDeclinedAdapter;
    AlsRecyclerView eventDeclinedListRV;

    FirebaseUser cUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_raised_declined_event, container, false);

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

        eventDeclinedListSRL = root.findViewById(R.id.eventDeclinedListSwipeRefreshLayout);
        View emptyEventView = root.findViewById(R.id.empty_event_declined_list_view);
        //recycler view
        eventDeclinedListRV = root.findViewById(R.id.eventDeclinedListRecyclerView);
        eventDeclinedListRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        eventDeclinedListRV.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
        eventDeclinedListRV.setLayoutManager(layoutManager);
        eventDeclinedListRV.showIfEmpty(emptyEventView);

        //swipeRefreshLayout function
        eventDeclinedListSRL.setOnRefreshListener(this);
        eventDeclinedListSRL.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        eventDeclinedListSRL.post(new Runnable() {
            @Override
            public void run() {
                eventDeclinedListSRL.setRefreshing(true);
                loadDeclinedEventList();
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
            loadDeclinedEventList();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT).show();
        }
        else{
            loadDeclinedEventList();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Variable.EVENT_REF.removeEventListener(eventDeclinedValueEventListener);
    }

    @Override
    public void onRefresh() {
        loadDeclinedEventList();
    }

    private void loadDeclinedEventList(){
        eventDeclinedList = new ArrayList<>();
        Variable.EVENT_REF.addListenerForSingleValueEvent(eventDeclinedValueEventListener);
    }

    private final ValueEventListener eventDeclinedValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            eventDeclinedList.clear();

            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                Event event = dataSnapshot.getValue(Event.class);

                if(event != null && event.getEventHandler().equals(cUser.getUid()) && event.getEventStatus().equals(Variable.DECLINED)) {
                    eventDeclinedList.add(event);
                }
            }

            eventDeclinedAdapter = new RaisedEventListFragmentAdapter(eventDeclinedList, getContext());
            eventDeclinedAdapter.notifyDataSetChanged();
            eventDeclinedListRV.setAdapter(eventDeclinedAdapter);
            eventDeclinedListSRL.setRefreshing(false);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("EventPendingFragment", "Database Error: " + error.getMessage());
        }
    };
}