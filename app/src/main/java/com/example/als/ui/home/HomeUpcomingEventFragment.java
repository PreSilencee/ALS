package com.example.als.ui.home;

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

import com.example.als.R;
import com.example.als.adapter.HomeAvailableEventListFragmentAdapter;
import com.example.als.adapter.HomeUpcomingEventListFragmentAdapter;
import com.example.als.handler.Connectivity;
import com.example.als.object.Event;
import com.example.als.object.Variable;
import com.example.als.widget.AlsRecyclerView;
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

public class HomeUpcomingEventFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    String TAG = "HomeUpcomingEvent";

    Connectivity device;

    SwipeRefreshLayout homeUpcomingEventListSRL;

    List<Event> homeUpcomingEventList;
    HomeUpcomingEventListFragmentAdapter homeUpcomingEventAdapter;
    AlsRecyclerView homeUpcomingEventListRV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_home_upcoming_event, container, false);

        device = new Connectivity(requireContext());

        homeUpcomingEventListSRL = root.findViewById(R.id.homeUpcomingEventListSwipeRefreshLayout);
        View homeUpcomingEmptyEventView = root.findViewById(R.id.homeUpcomingEventListEmptyView);
        //recycler view
        homeUpcomingEventListRV = root.findViewById(R.id.homeUpcomingEventListRecyclerView);
        homeUpcomingEventListRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        homeUpcomingEventListRV.setLayoutManager(layoutManager);
        homeUpcomingEventListRV.showIfEmpty(homeUpcomingEmptyEventView);

        //swipeRefreshLayout function
        homeUpcomingEventListSRL.setOnRefreshListener(this);
        homeUpcomingEventListSRL.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        homeUpcomingEventListSRL.post(new Runnable() {
            @Override
            public void run() {
                homeUpcomingEventListSRL.setRefreshing(true);
                loadHomeUpcomingEventList();
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
            loadHomeUpcomingEventList();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT).show();
        }
        else{
            loadHomeUpcomingEventList();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Variable.EVENT_REF.removeEventListener(homeEventUpcomingValueEventListener);
    }

    @Override
    public void onRefresh() {
        loadHomeUpcomingEventList();
    }

    private void loadHomeUpcomingEventList(){
        homeUpcomingEventList = new ArrayList<>();
        Variable.EVENT_REF.addListenerForSingleValueEvent(homeEventUpcomingValueEventListener);
    }

    private final ValueEventListener homeEventUpcomingValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            homeUpcomingEventList.clear();

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

                if(event != null && event.getEventStartDate() != null){
                    try {
                        Date eventStartDate = simpleDateFormat.parse(event.getEventStartDate());

                        if(currentDate.compareTo(eventStartDate) < 0 && event.getEventStatus().equals(Variable.AVAILABLE)){
                            homeUpcomingEventList.add(event);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            homeUpcomingEventAdapter = new HomeUpcomingEventListFragmentAdapter(homeUpcomingEventList, getContext());
            homeUpcomingEventAdapter.notifyDataSetChanged();
            homeUpcomingEventListRV.setAdapter(homeUpcomingEventAdapter);
            homeUpcomingEventListSRL.setRefreshing(false);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("EventPendingFragment", "Database Error: " + error.getMessage());
        }
    };
}