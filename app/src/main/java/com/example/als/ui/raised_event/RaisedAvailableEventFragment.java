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


public class RaisedAvailableEventFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    Connectivity device;
    SwipeRefreshLayout eventAvailableListSRL;

    List<Event> eventAvailableList;
    RaisedEventListFragmentAdapter eventAvailableAdapter;
    AlsRecyclerView eventAvailableListRV;

    FirebaseUser cUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_raised_available_event, container, false);;
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

        eventAvailableListSRL = root.findViewById(R.id.eventAvailableListSwipeRefreshLayout);
        View emptyEventView = root.findViewById(R.id.empty_event_available_list_view);
        //recycler view
        eventAvailableListRV = root.findViewById(R.id.eventAvailableListRecyclerView);
        eventAvailableListRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        eventAvailableListRV.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
        eventAvailableListRV.setLayoutManager(layoutManager);
        eventAvailableListRV.showIfEmpty(emptyEventView);

        //swipeRefreshLayout function
        eventAvailableListSRL.setOnRefreshListener(this);
        eventAvailableListSRL.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        eventAvailableListSRL.post(new Runnable() {
            @Override
            public void run() {
                eventAvailableListSRL.setRefreshing(true);
                loadAvailableEventList();
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
            loadAvailableEventList();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT).show();
        }
        else{
            loadAvailableEventList();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Variable.EVENT_REF.removeEventListener(eventAvailableValueEventListener);
    }

    @Override
    public void onRefresh() {
        loadAvailableEventList();
    }

    private void loadAvailableEventList(){
        eventAvailableList = new ArrayList<>();
        Variable.EVENT_REF.addListenerForSingleValueEvent(eventAvailableValueEventListener);
    }

    private final ValueEventListener eventAvailableValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            eventAvailableList.clear();

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

                if(event != null && event.getEventHandler().equals(cUser.getUid())
                        && event.getEventStatus().equals(Variable.AVAILABLE) && event.getEventStartDate() != null && event.getEventEndDate() != null) {
                    try {
                        Date eventStartDate = simpleDateFormat.parse(event.getEventStartDate());
                        Date eventEndDate = simpleDateFormat.parse(event.getEventEndDate());

                        if(((currentDate.compareTo(eventStartDate) == 0 || currentDate.compareTo(eventStartDate) > 0))
                                && ((currentDate.compareTo(eventEndDate) == 0 || currentDate.compareTo(eventEndDate) < 0))){
                            eventAvailableList.add(event);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            eventAvailableAdapter = new RaisedEventListFragmentAdapter(eventAvailableList, getContext());
            eventAvailableAdapter.notifyDataSetChanged();
            eventAvailableListRV.setAdapter(eventAvailableAdapter);
            eventAvailableListSRL.setRefreshing(false);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("EventAllFragment", "Database Error: " + error.getMessage());
        }
    };
}