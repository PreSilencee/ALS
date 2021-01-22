package com.example.als.ui.raised_event;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.example.als.CreateEventActivity;
import com.example.als.R;
import com.example.als.adapter.RaisedEventListFragmentAdapter;
import com.example.als.adapter.ViewPagerAdapter;
import com.example.als.handler.Connectivity;
import com.example.als.object.Event;
import com.example.als.object.Variable;
import com.example.als.ui.donationHistory.DonationHistoryFragment;
import com.example.als.ui.home.HomeFragment;
import com.example.als.ui.message.MessageFragment;
import com.example.als.ui.settings.SettingFragment;
import com.example.als.widget.AlsRecyclerView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class EventFragment extends Fragment{

    private static final String TAG = "EventFragment";
    private Connectivity device;
    private FirebaseAuth cAuth;

    private SwipeRefreshLayout raisedEventListSRL;

    private AlsRecyclerView raisedEventRV;

    private List<Event> raisedEventList;
    private RaisedEventListFragmentAdapter adapter;

    FirebaseUser cUser;

    Button startEventBtn;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Log.d(TAG, "running: success");
        View root = inflater.inflate(R.layout.fragment_event, container, false);

        device = new Connectivity(getContext());

        ViewPager createEventViewPager = root.findViewById(R.id.createEventViewPager);
        setupViewPager(createEventViewPager);
        TabLayout createEventTabLayout = root.findViewById(R.id.createEventTabLayout);
        createEventTabLayout.setupWithViewPager(createEventViewPager);

        startEventBtn = root.findViewById(R.id.startCreateEventButton);

        startEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), CreateEventActivity.class));
            }
        });

//        cAuth = FirebaseAuth.getInstance();
//
//        raisedEventListSRL = root.findViewById(R.id.raisedEventListSwipeRefreshLayout);
//        View raisedEventEmptyView = root.findViewById(R.id.empty_raised_event_list);
//        //recycler view
//        raisedEventRV = root.findViewById(R.id.raisedEventListRecyclerView);
//        raisedEventRV.setHasFixedSize(true);
//        raisedEventRV.setLayoutManager(new LinearLayoutManager(requireActivity()));
//        raisedEventRV.showIfEmpty(raisedEventEmptyView);
//        raisedEventRV.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
//
//        //swipeRefreshLayout function
//        raisedEventListSRL.setOnRefreshListener(this);
//        raisedEventListSRL.setColorSchemeResources(R.color.colorPrimary,
//                android.R.color.holo_green_dark,
//                android.R.color.holo_orange_dark,
//                android.R.color.holo_blue_dark);
//
//        raisedEventListSRL.post(new Runnable() {
//            @Override
//            public void run() {
//                raisedEventListSRL.setRefreshing(true);
//                loadRaisedEvent();
//            }
//        });

        return root;
    }

    private void setupViewPager(ViewPager viewPager)
    {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager(), 0);
        adapter.addFragment(new RaisedEventFragment(), "Event");
        adapter.addFragment(new EventDraftFragment(), "Draft");

        viewPager.setAdapter(adapter);
    }


    @Override
    public void onStart() {
        super.onStart();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

//    private void loadRaisedEvent(){
//
//        cUser = cAuth.getCurrentUser();
//
//        if (cUser != null) {
//
//            raisedEventList = new ArrayList<>();
//
//            Variable.EVENT_REF.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    raisedEventList.clear();
//
//                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
//                        Event event = dataSnapshot.getValue(Event.class);
//
//                        if(event != null) {
//                            if (event.getEventHandler().equals(cUser.getUid())) {
//                                raisedEventList.add(event);
//                            }
//                        }
//                    }
//
//                    adapter = new RaisedEventListFragmentAdapter(raisedEventList, getContext());
//                    adapter.notifyDataSetChanged();
//                    raisedEventRV.setAdapter(adapter);
//                    raisedEventListSRL.setRefreshing(false);
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    Log.d(TAG, "Database Error: " + error.getMessage());
//                }
//            });
//        }
//    }
//
//    @Override
//    public void onRefresh() {
//        loadRaisedEvent();
//    }
}