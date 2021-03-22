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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class RaisedUpcomingEventFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    Connectivity device;
    SwipeRefreshLayout eventUpcomingListSRL;

    List<Event> eventUpcomingList;
    RaisedEventListFragmentAdapter eventUpcomingAdapter;
    AlsRecyclerView eventUpcomingListRV;

    FirebaseUser cUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_raised_upcoming_event, container, false);

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

        eventUpcomingListSRL = root.findViewById(R.id.eventUpcomingListSwipeRefreshLayout);
        View emptyEventView = root.findViewById(R.id.empty_event_upcoming_list_view);
        //recycler view
        eventUpcomingListRV = root.findViewById(R.id.eventUpcomingListRecyclerView);
        eventUpcomingListRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        eventUpcomingListRV.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
        eventUpcomingListRV.setLayoutManager(layoutManager);
        eventUpcomingListRV.showIfEmpty(emptyEventView);

        //swipeRefreshLayout function
        eventUpcomingListSRL.setOnRefreshListener(this);
        eventUpcomingListSRL.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        eventUpcomingListSRL.post(new Runnable() {
            @Override
            public void run() {
                eventUpcomingListSRL.setRefreshing(true);
                loadUpcomingEventList();
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
            loadUpcomingEventList();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT).show();
        }
        else{
            loadUpcomingEventList();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Variable.EVENT_REF.removeEventListener(eventUpcomingValueEventListener);
    }

    @Override
    public void onRefresh() {
        loadUpcomingEventList();
    }

    private void loadUpcomingEventList(){
        eventUpcomingList = new ArrayList<>();
        Variable.EVENT_REF.addListenerForSingleValueEvent(eventUpcomingValueEventListener);
    }

    private final ValueEventListener eventUpcomingValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            eventUpcomingList.clear();

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

                if(event != null && event.getEventHandler().equals(cUser.getUid()) && event.getEventStartDate() != null){
                    try {
                        Date eventStartDate = simpleDateFormat.parse(event.getEventStartDate());

                        if(currentDate.compareTo(eventStartDate) < 0 && event.getEventStatus().equals(Variable.AVAILABLE)){
                            eventUpcomingList.add(event);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            eventUpcomingAdapter = new RaisedEventListFragmentAdapter(eventUpcomingList, getContext());
            eventUpcomingAdapter.notifyDataSetChanged();
            eventUpcomingListRV.setAdapter(eventUpcomingAdapter);
            eventUpcomingListSRL.setRefreshing(false);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("EventPendingFragment", "Database Error: " + error.getMessage());
        }
    };
}