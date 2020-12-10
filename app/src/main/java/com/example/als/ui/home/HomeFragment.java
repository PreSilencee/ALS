package com.example.als.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.als.R;
import com.example.als.adapter.HomeEventListFragmentAdapter;
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

public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = "HomeEventListFragment";
    private Connectivity device;
    private FirebaseAuth cAuth;
    private SwipeRefreshLayout homeEventListSRL;

    private List<Event> homeEventList;
    private HomeEventListFragmentAdapter adapter;
    private AlsRecyclerView homeEventListRV;

    FirebaseUser cUser;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             final ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        device = new Connectivity(getContext());
        setHasOptionsMenu(true);

        cAuth = FirebaseAuth.getInstance();

        homeEventListSRL = root.findViewById(R.id.homeEventListSwipeRefreshLayout);
        View homeEmptyEventView = root.findViewById(R.id.homeEventListEmptyView);
        //recycler view
        homeEventListRV = root.findViewById(R.id.homeEventListRecyclerView);
        homeEventListRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        homeEventListRV.setLayoutManager(layoutManager);
        homeEventListRV.showIfEmpty(homeEmptyEventView);

        //swipeRefreshLayout function
        homeEventListSRL.setOnRefreshListener(this);
        homeEventListSRL.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        homeEventListSRL.post(new Runnable() {
            @Override
            public void run() {
                homeEventListSRL.setRefreshing(true);
                loadHomeEventList();
            }
        });


        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.home, menu);

        MenuItem action_search = menu.findItem(R.id.action_search_view);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(action_search);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            loadHomeEventList();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            loadHomeEventList();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onRefresh() {
        loadHomeEventList();
    }

    private void loadHomeEventList(){

        cUser = cAuth.getCurrentUser();

        if(cUser != null){
            homeEventList = new ArrayList<>();

            Variable.EVENT_REF.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    homeEventList.clear();

                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Event event = dataSnapshot.getValue(Event.class);

                        if(event != null) {
                            homeEventList.add(event);
                        }
                    }

                    adapter = new HomeEventListFragmentAdapter(homeEventList, getContext());
                    adapter.notifyDataSetChanged();
                    homeEventListRV.setAdapter(adapter);
                    homeEventListSRL.setRefreshing(false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "Database Error: " + error.getMessage());
                }
            });
        }

//        if(homeEventListAdapter != null)
//        {
//            homeEventListAdapter.startListening();
//
//            Variable.EVENT_REF.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    homeEventListSRL.setRefreshing(false);
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    Log.d("Database Error", error.getMessage());
//                }
//            });
//        }

    }
}