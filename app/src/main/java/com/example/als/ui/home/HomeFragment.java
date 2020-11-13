package com.example.als.ui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.als.R;
import com.example.als.handler.Connectivity;
import com.example.als.object.Contributor;
import com.example.als.object.Event;
import com.example.als.object.Organization;
import com.example.als.object.User;
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
                             final ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        device = new Connectivity(getContext());
        setHasOptionsMenu(true);

        cAuth = FirebaseAuth.getInstance();

        homeEventListSRL = root.findViewById(R.id.homeEventListSwipeRefreshLayout);
        View homeEmptyEventView = root.findViewById(R.id.homeEventListEmptyView);
        //recycler view
        AlsRecyclerView eventListRV = root.findViewById(R.id.homeEventListRecyclerView);
        eventListRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        eventListRV.setLayoutManager(layoutManager);
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
            protected void onBindViewHolder(@NonNull final HomeEventListFragmentViewHolder holder, final int position, @NonNull final Event model) {

                if(model.getEventHandler() != null){
                    Variable.ORGANIZATION_REF.child(model.getEventHandler()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                final Organization organization = snapshot.getValue(Organization.class);

                                if(organization != null){
                                    if(organization.getOrganizationName() != null){
                                        holder.homeEventListProfileNameTV.setText(organization.getOrganizationName());
                                        holder.homeEventListProfileNameTV.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                final String key = snapshot.getKey();

                                                if(key != null){
                                                    Variable.USER_REF.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if(snapshot.exists()){

                                                                User user = snapshot.getValue(User.class);

                                                                if(user != null){
                                                                    Intent i = new Intent(requireActivity(), HomeUserViewDetailsActivity.class);
                                                                    i.putExtra(Variable.HOME_USER_SESSION_ID, key);
                                                                    i.putExtra(Variable.HOME_USER_SESSION_POSITION, user.getRole());
                                                                    startActivity(i);
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                                }



                                            }
                                        });
                                    }
                                    else{
                                        holder.homeEventListProfileNameTV.setText("-");
                                    }

                                    if(organization.getOrganizationProfileImageName() != null){
                                        StorageReference profileImageRef = Variable.ORGANIZATION_SR.child(model.getEventHandler())
                                                .child("profile").child(organization.getOrganizationProfileImageName());

                                        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                Log.d(TAG, "loadProfileImage: success");
                                                Glide.with(requireActivity())
                                                        .load(uri)
                                                        .placeholder(R.drawable.loading_image)
                                                        .into(holder.homeEventListProfileIV);
                                            }
                                        })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d(TAG, "loadProfileImage:Failed");
                                                        holder.homeEventListIV.setImageResource(R.drawable.loading_image);
                                                    }
                                                });
                                    }
                                }
                            }
                            else{
                                Variable.CONTRIBUTOR_REF.child(model.getEventHandler()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull final DataSnapshot snapshot) {
                                        if(snapshot.exists()){
                                            Contributor contributor = snapshot.getValue(Contributor.class);

                                            if(contributor != null){
                                                if(contributor.getName() != null){
                                                    holder.homeEventListProfileNameTV.setText(contributor.getName());
                                                    holder.homeEventListProfileNameTV.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            final String key = snapshot.getKey();

                                                            if(key != null){
                                                                Variable.USER_REF.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        if(snapshot.exists()){

                                                                            User user = snapshot.getValue(User.class);

                                                                            if(user != null){
                                                                                Intent i = new Intent(requireActivity(), HomeUserViewDetailsActivity.class);
                                                                                i.putExtra(Variable.HOME_USER_SESSION_ID, key);
                                                                                i.putExtra(Variable.HOME_USER_SESSION_POSITION, user.getRole());
                                                                                startActivity(i);
                                                                            }
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                }
                                                else{
                                                    holder.homeEventListProfileNameTV.setText("-");
                                                }

                                                if(contributor.getProfileImageName() != null){
                                                    StorageReference profileImageRef = Variable.CONTRIBUTOR_SR.child(model.getEventHandler())
                                                            .child("profile").child(contributor.getProfileImageName());

                                                    profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            Log.d(TAG, "loadProfileImage: success");
                                                            Glide.with(requireActivity())
                                                                    .load(uri)
                                                                    .placeholder(R.drawable.loading_image)
                                                                    .into(holder.homeEventListProfileIV);
                                                        }
                                                    })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.d(TAG, "loadProfileImage:Failed");
                                                                    holder.homeEventListIV.setImageResource(R.drawable.loading_image);
                                                                }
                                                            });
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.d(TAG, "databaseerror: " + error.getMessage());
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d(TAG, "databaseerror: "+ error.getMessage());
                        }
                    });
                }

                if(model.getEventDateTimeCreated() != null){
                    holder.homeEventListEventCreatedDate.setText(model.getEventDateTimeCreated());
                }
                else{
                    holder.homeEventListEventCreatedDate.setText("-");
                }

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
                            Log.d(TAG, "loadEventImage: success");
                            Glide.with(requireActivity())
                                    .load(uri)
                                    .placeholder(R.drawable.loading_image)
                                    .into(holder.homeEventListIV);
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "loadEventImage:Failed");
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

                if(model.getEventStartDate() != null && model.getEventEndDate() != null){
                    String duration = model.getEventStartDate() + "~" + model.getEventEndDate();
                    holder.homeEventListDurationTV.setText(duration);
                }
                else{
                    holder.homeEventListDurationTV.setText("-");
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
                        FirebaseUser cUser = cAuth.getCurrentUser();

                        if(cUser != null){
                            Intent i = new Intent(requireActivity(), HomeDonateActivity.class);
                            i.putExtra(Variable.HOME_USER_SESSION_ID, cUser.getUid());
                            i.putExtra(Variable.HOME_EVENT_SESSION_ID, getRef(position).getKey());
                            startActivity(i);
                        }

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