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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    public static String queryEvent;
    String secondQuery;

    FirebaseUser cUser;

    public SearchEventFragment() {
        // Required empty public constructor
    }

    public void setQueryEvent(String q){
        queryEvent = q;
    }

    public static SearchEventFragment newInstance(String q) {
        SearchEventFragment fragment = new SearchEventFragment();
        fragment.setQueryEvent(q);
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
        View root = inflater.inflate(R.layout.fragment_search_event, container, false);

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
            searchAllEvent();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Variable.EVENT_REF.removeEventListener(searchAllEventValueEventListener);
    }

    public void searchAllEvent(){
        searchEventAllList = new ArrayList<>();
        Variable.EVENT_REF.addListenerForSingleValueEvent(searchAllEventValueEventListener);

    }

    private final ValueEventListener searchAllEventValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            searchEventAllList.clear();

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

                if(event != null && event.getEventStatus().equals(Variable.AVAILABLE) && event.getEventEndDate() != null && (event.getEventTitle().contains(queryEvent)
                        || event.getEventDescription().contains(queryEvent)
                        || event.getEventTitle().toLowerCase().contains(queryEvent)
                        || event.getEventDescription().toLowerCase().contains(queryEvent))){
                    try {
                        Date eventEndDate = simpleDateFormat.parse(event.getEventEndDate());
                        if(currentDate != null){
                            if(((currentDate.compareTo(eventEndDate) == 0 || currentDate.compareTo(eventEndDate) < 0))){
                                searchEventAllList.add(event);
                            }
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


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
}