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
import com.example.als.MainActivity;
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

public class RaisedAllEventFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    Connectivity device;
    SwipeRefreshLayout eventAllListSRL;

    List<Event> eventAllList;
    RaisedEventListFragmentAdapter eventAllAdapter;
    AlsRecyclerView eventAllListRV;

    FirebaseUser cUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_raised_all_event, container, false);

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

        eventAllListSRL = root.findViewById(R.id.eventAllListSwipeRefreshLayout);
        View emptyEventView = root.findViewById(R.id.empty_event_all_list_view);
        //recycler view
        eventAllListRV = root.findViewById(R.id.eventAllListRecyclerView);
        eventAllListRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        eventAllListRV.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
        eventAllListRV.setLayoutManager(layoutManager);
        eventAllListRV.showIfEmpty(emptyEventView);

        //swipeRefreshLayout function
        eventAllListSRL.setOnRefreshListener(this);
        eventAllListSRL.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        eventAllListSRL.post(new Runnable() {
            @Override
            public void run() {
                eventAllListSRL.setRefreshing(true);
                loadAllEventList();
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
            loadAllEventList();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT).show();
        }
        else{
            loadAllEventList();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Variable.EVENT_REF.removeEventListener(eventAllValueEventListener);
    }

    @Override
    public void onRefresh() {
        loadAllEventList();
    }

    private void loadAllEventList(){
        eventAllList = new ArrayList<>();
        Variable.EVENT_REF.addListenerForSingleValueEvent(eventAllValueEventListener);
    }

    private final ValueEventListener eventAllValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            eventAllList.clear();

            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                Event event = dataSnapshot.getValue(Event.class);

                if(event != null && event.getEventHandler().equals(cUser.getUid())) {
                    eventAllList.add(event);
                }
            }

            eventAllAdapter = new RaisedEventListFragmentAdapter(eventAllList, getContext());
            eventAllAdapter.notifyDataSetChanged();
            eventAllListRV.setAdapter(eventAllAdapter);
            eventAllListSRL.setRefreshing(false);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("EventAllFragment", "Database Error: " + error.getMessage());
        }
    };
}