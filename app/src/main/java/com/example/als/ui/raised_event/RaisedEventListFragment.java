package com.example.als.ui.raised_event;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.als.MainActivity;
import com.example.als.R;
import com.example.als.adapter.RaisedEventListFragmentAdapter;
import com.example.als.handler.Connectivity;
import com.example.als.handler.GlideApp;
import com.example.als.object.Event;
import com.example.als.object.Variable;
import com.example.als.viewHolder.RaisedEventListFragmentViewHolder;
import com.example.als.widget.AlsRecyclerView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class RaisedEventListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = "EventFragment";
    private Connectivity device;
    private FirebaseAuth cAuth;

    private SwipeRefreshLayout raisedEventListSRL;

    private FirebaseRecyclerOptions<Event> raisedEventOptions;
    private FirebaseRecyclerAdapter<Event, RaisedEventListFragmentViewHolder> raisedEventAdapter;

    private AlsRecyclerView raisedEventRV;
    private RaisedEventListFragmentAdapter adapter;
    private List<Event> raisedEventList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Log.d(TAG, "running: success");
        View root = inflater.inflate(R.layout.fragment_raised_event, container, false);

        device = new Connectivity(getContext());

        cAuth = FirebaseAuth.getInstance();

        raisedEventListSRL = root.findViewById(R.id.raisedEventListSwipeRefreshLayout);
        View raisedEventEmptyView = root.findViewById(R.id.empty_raised_event_list);
        //recycler view
        raisedEventRV = root.findViewById(R.id.raisedEventListRecyclerView);
        raisedEventRV.setHasFixedSize(true);
        raisedEventRV.setLayoutManager(new LinearLayoutManager(requireActivity()));
        raisedEventRV.showIfEmpty(raisedEventEmptyView);
        raisedEventRV.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));

        //swipeRefreshLayout function
        raisedEventListSRL.setOnRefreshListener(this);
        raisedEventListSRL.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        raisedEventListSRL.post(new Runnable() {
            @Override
            public void run() {
                loadRaisedEvent();
                raisedEventListSRL.setRefreshing(true);
            }
        });

        final FirebaseUser cUser = cAuth.getCurrentUser();
        //Log.d(TAG, "uid: "+cUser.getUid());
        if(cUser != null){

//            Query queryEvent = Variable.EVENT_REF.orderByChild("eventHandler").equalTo(cUser.getUid());

            raisedEventOptions = new FirebaseRecyclerOptions.Builder<Event>()
                    .setQuery(Variable.EVENT_REF, Event.class).build();


             raisedEventAdapter =
                    new FirebaseRecyclerAdapter<Event, RaisedEventListFragmentViewHolder>(raisedEventOptions) {
                @Override
                protected void onBindViewHolder(@NonNull RaisedEventListFragmentViewHolder holder, final int position, @NonNull Event model) {
                    Log.d(TAG, "I am here");
                    if(model.getEventHandler().equals(cUser.getUid())){
                        String[] separatedDateAndTime = model.getEventDateTimeCreated().split(" ");
                        String[] separatedDate = separatedDateAndTime[0].split("/");
                        holder.raisedEventListYearTV.setText(separatedDate[2]);
                        holder.raisedEventListDayTV.setText(separatedDate[0]);
                        holder.raisedEventListMonthTV.setText(separatedDate[1]);


                        if(model.getEventTitle() != null){
                            holder.raisedEventListNameTV.setText(model.getEventTitle());
                        }
                        else{
                            holder.raisedEventListNameTV.setText("-");
                        }

                        String currentAmount;
                        String targetAmount;
                        double cAmount;
                        double tAmount;

                        if(model.getEventCurrentAmount() >= 0){
                            currentAmount = "RM " + model.getEventCurrentAmount();
                            cAmount = model.getEventCurrentAmount();
                        }
                        else{
                            currentAmount = "RM 0";
                            cAmount = 0.0;
                        }

                        if(model.getEventTargetAmount() > 0){
                            targetAmount = "RM " + model.getEventTargetAmount();
                            tAmount = model.getEventTargetAmount();
                        }
                        else {
                            targetAmount = "RM 0";
                            tAmount = 0.0;
                        }

                        String currentProgressTV = currentAmount + "/" +targetAmount;
                        holder.raisedEventListProgressTV.setText(currentProgressTV);

                        double progress = (cAmount/tAmount)*100;
                        holder.raisedEventListPB.setProgress((int) progress);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(requireActivity(), RaisedEventDetailsActivity.class);
                                i.putExtra(Variable.EVENT_SESSION_ID, getRef(position).getKey());
                                startActivity(i);
                            }
                        });
                    }
                    else{
                        holder.raisedEventListCV.setVisibility(View.GONE);
                        holder.raisedEventListCV.setLayoutParams(new RecyclerView.LayoutParams(0,0));
                    }

                }

                @NonNull
                @Override
                public RaisedEventListFragmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.raised_event_view_layout,parent,false);
                    return new RaisedEventListFragmentViewHolder(view);
                }
            };

            raisedEventAdapter.startListening();
            raisedEventAdapter.notifyDataSetChanged();
            raisedEventRV.setAdapter(raisedEventAdapter);
        }

        return root;
    }


    @Override
    public void onStart() {
        super.onStart();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            if(raisedEventAdapter !=null){
                raisedEventAdapter.startListening();
            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            if(raisedEventAdapter !=null){
                raisedEventAdapter.startListening();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(raisedEventAdapter !=null){
            raisedEventAdapter.stopListening();
        }
    }

    private void loadRaisedEvent(){

        FirebaseUser cUser = cAuth.getCurrentUser();
        if (cUser != null) {
            Variable.EVENT_REF.orderByChild("eventHandler").equalTo(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    raisedEventListSRL.setRefreshing(false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("Database Error", error.getMessage());
                }
            });

        }
    }

    @Override
    public void onRefresh() {
        loadRaisedEvent();
    }
}