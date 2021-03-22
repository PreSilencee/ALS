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

public class HomeAvailableEventFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {


    String TAG = "HomeAvailableEvent";

    Connectivity device;

    SwipeRefreshLayout homeAvailableEventListSRL;

    List<Event> homeAvailableEventList;
    HomeAvailableEventListFragmentAdapter homeAvailableEventAdapter;
    AlsRecyclerView homeAvailableEventListRV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_home_available_event, container, false);

        device = new Connectivity(requireContext());

        homeAvailableEventListSRL = root.findViewById(R.id.homeAvailableEventListSwipeRefreshLayout);
        View homeAvailableEmptyEventView = root.findViewById(R.id.homeAvailableEventListEmptyView);
        //recycler view
        homeAvailableEventListRV = root.findViewById(R.id.homeAvailableEventListRecyclerView);
        homeAvailableEventListRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        homeAvailableEventListRV.setLayoutManager(layoutManager);
        homeAvailableEventListRV.showIfEmpty(homeAvailableEmptyEventView);

        //swipeRefreshLayout function
        homeAvailableEventListSRL.setOnRefreshListener(this);
        homeAvailableEventListSRL.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        homeAvailableEventListSRL.post(new Runnable() {
            @Override
            public void run() {
                homeAvailableEventListSRL.setRefreshing(true);
                loadHomeAvailableEventList();
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
            loadHomeAvailableEventList();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT).show();
        }
        else{
            loadHomeAvailableEventList();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Variable.EVENT_REF.removeEventListener(homeAvailableEventValueEventListener);
    }

    @Override
    public void onRefresh() {
        loadHomeAvailableEventList();
    }

    private final ValueEventListener homeAvailableEventValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            homeAvailableEventList.clear();

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

                if(event != null && event.getEventStatus().equals(Variable.AVAILABLE)
                        && event.getEventStartDate() != null && event.getEventEndDate() != null) {
                    try {
                        Date eventStartDate = simpleDateFormat.parse(event.getEventStartDate());
                        Date eventEndDate = simpleDateFormat.parse(event.getEventEndDate());

                        if(((currentDate.compareTo(eventStartDate) == 0 || currentDate.compareTo(eventStartDate) > 0))
                                && ((currentDate.compareTo(eventEndDate) == 0 || currentDate.compareTo(eventEndDate) < 0))){
                            homeAvailableEventList.add(event);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            homeAvailableEventAdapter = new HomeAvailableEventListFragmentAdapter(homeAvailableEventList, getContext());
            homeAvailableEventAdapter.notifyDataSetChanged();
            homeAvailableEventListRV.setAdapter(homeAvailableEventAdapter);
            homeAvailableEventListSRL.setRefreshing(false);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d(TAG, "Database Error: " + error.getMessage());
        }
    };

    private void loadHomeAvailableEventList(){
        homeAvailableEventList = new ArrayList<>();
        Variable.EVENT_REF.addListenerForSingleValueEvent(homeAvailableEventValueEventListener);
    }
}