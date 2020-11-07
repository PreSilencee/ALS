package com.example.als.ui.home;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.als.R;
import com.example.als.handler.Connectivity;
import com.example.als.object.Event;
import com.example.als.object.Variable;
import com.example.als.viewHolder.HomeEventListFragmentViewHolder;
import com.example.als.widget.AlsRecyclerView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import es.dmoral.toasty.Toasty;

public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = "HomeEventListFragment";
    private Connectivity device;
    private FirebaseAuth cAuth;
    private SwipeRefreshLayout homeEventListSRL;
    private FirebaseRecyclerOptions<Event> homeEventListOptions;
    private FirebaseRecyclerAdapter<Event, HomeEventListFragmentViewHolder> homeEventListAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        device = new Connectivity(getContext());

        cAuth = FirebaseAuth.getInstance();
        homeEventListSRL = root.findViewById(R.id.homeEventListSwipeRefreshLayout);
        View homeEmptyEventView = root.findViewById(R.id.homeEventListEmptyView);
        //recycler view
        AlsRecyclerView eventListRV = root.findViewById(R.id.homeEventListRecyclerView);
        eventListRV.setHasFixedSize(true);
        eventListRV.setLayoutManager(new LinearLayoutManager(requireActivity()));
        eventListRV.showIfEmpty(homeEmptyEventView);

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

        homeEventListOptions = new FirebaseRecyclerOptions.Builder<Event>()
                .setQuery(Variable.EVENT_REF, Event.class).build();

        homeEventListAdapter = new FirebaseRecyclerAdapter<Event, HomeEventListFragmentViewHolder>(homeEventListOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final HomeEventListFragmentViewHolder holder, int position, @NonNull Event model) {
                if(model.getEventTitle() != null){
                    holder.homeEventListTitleTV.setText(model.getEventTitle());
                }
                else{
                    holder.homeEventListTitleTV.setText("-");
                }

                if(model.getEventImageName() != null){
                    final StorageReference eventImageRef = Variable.EVENT_SR.child(model.getEventImageName());

                    eventImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d(TAG, "loadImage: success");
                            Glide.with(requireActivity())
                                    .load(uri)
                                    .placeholder(R.drawable.loading_image)
                                    .into(holder.homeEventListIV);
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "loadImage:Failed");
                                    holder.homeEventListIV.setImageResource(R.drawable.loading_image);
                                }
                            });
                }

                if(model.getEventDescription() != null){
                    holder.homeEventListDescriptionTV.setText(model.getEventDescription());
                }
                else{
                    holder.homeEventListDescriptionTV.setText("-");
                }

                if(model.getEventStartDate() != null){
                    holder.homeEventListStartDateTV.setText(model.getEventStartDate());
                }
                else{
                    holder.homeEventListStartDateTV.setText("-");
                }

                if(model.getEventEndDate() != null){
                    holder.homeEventListEndDateTV.setText(model.getEventEndDate());
                }
                else{
                    holder.homeEventListEndDateTV.setText("-");
                }

                if(model.getEventCurrentAmount() > 0){
                    String currentAmount = "RM " + model.getEventCurrentAmount();
                    holder.homeEventListCurrentFundTV.setText(currentAmount);
                }
                else{
                    String currentAmount = "RM 0";
                    holder.homeEventListCurrentFundTV.setText(currentAmount);
                }

                if(model.getEventTargetAmount() > 0){
                    String targetAmount = "RM " + model.getEventTargetAmount();
                    holder.homeEventListTargetFundTV.setText(targetAmount);
                }
                else{
                    String targetAmount = "RM 0";
                    holder.homeEventListTargetFundTV.setText(targetAmount);
                }

                double fundProgress = (model.getEventCurrentAmount() / model.getEventTargetAmount()) * 100;
                holder.homeEventListPB.setProgress((int) fundProgress);

                holder.homeEventListDonateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(requireActivity(), HomeDonateActivity.class));
                    }
                });

                holder.homeEventListViewDetailsBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });


            }

            @NonNull
            @Override
            public HomeEventListFragmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_event_list_view_layout,parent,false);
                return new HomeEventListFragmentViewHolder(view);
            }
        };

        homeEventListAdapter.startListening();
        homeEventListAdapter.notifyDataSetChanged();
        eventListRV.setAdapter(homeEventListAdapter);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!device.haveNetwork()) {
            Toasty.error(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            if(homeEventListAdapter != null){
                homeEventListAdapter.startListening();
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
            if(homeEventListAdapter != null){
                homeEventListAdapter.startListening();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(homeEventListAdapter != null){
            homeEventListAdapter.stopListening();
        }
    }

    @Override
    public void onRefresh() {
        loadHomeEventList();
    }

    private void loadHomeEventList(){
        if(homeEventListAdapter != null)
        {
            homeEventListAdapter.startListening();

            Variable.EVENT_REF.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    homeEventListSRL.setRefreshing(false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("Database Error", error.getMessage());
                }
            });
        }


    }
}