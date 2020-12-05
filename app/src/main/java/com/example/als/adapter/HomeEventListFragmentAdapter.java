package com.example.als.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.als.R;
import com.example.als.handler.GlideApp;
import com.example.als.object.Contributor;
import com.example.als.object.Donation;
import com.example.als.object.Event;
import com.example.als.object.Organization;
import com.example.als.object.Variable;
import com.example.als.ui.home.HomeDonateActivity;
import com.example.als.ui.home.HomeUserViewDetailsActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class HomeEventListFragmentAdapter extends RecyclerView.Adapter<HomeEventListFragmentAdapter.ViewHolder> {

    private static final String TAG = "HomeEventListFragment";
    private List<Event> homeEventList;
    private Context context;


    public HomeEventListFragmentAdapter(List<Event> homeEventList, Context context) {
        this.homeEventList = homeEventList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.home_event_list_view_layout,parent,false);
        return new HomeEventListFragmentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Event event = homeEventList.get(position);

        if(event.getEventHandler() != null){
            Variable.ORGANIZATION_REF.child(event.getEventHandler()).addListenerForSingleValueEvent(new ValueEventListener() {
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
                                        Intent i = new Intent(context, HomeUserViewDetailsActivity.class);
                                        i.putExtra(Variable.HOME_USER_SESSION_ID, organization.getUserId());
                                        context.startActivity(i);
                                    }
                                });
                            }
                            else{
                                holder.homeEventListProfileNameTV.setText("-");
                            }

                            if(organization.getOrganizationProfileImageName() != null){
                                StorageReference profileImageRef = Variable.ORGANIZATION_SR.child(event.getEventHandler())
                                        .child("profile").child(organization.getOrganizationProfileImageName());

                                profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Log.d(TAG, "loadProfileImage: success");
                                        Glide.with(context)
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
                        Variable.CONTRIBUTOR_REF.child(event.getEventHandler()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    final Contributor contributor = snapshot.getValue(Contributor.class);

                                    if(contributor != null){
                                        if(contributor.getName() != null){
                                            holder.homeEventListProfileNameTV.setText(contributor.getName());
                                            holder.homeEventListProfileNameTV.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent i = new Intent(context, HomeUserViewDetailsActivity.class);
                                                    i.putExtra(Variable.HOME_USER_SESSION_ID, contributor.getUserId());
                                                    context.startActivity(i);
                                                }
                                            });
                                        }
                                        else{
                                            holder.homeEventListProfileNameTV.setText("-");
                                        }

                                        if(contributor.getProfileImageName() != null){
                                            StorageReference profileImageRef = Variable.CONTRIBUTOR_SR.child(event.getEventHandler())
                                                    .child("profile").child(contributor.getProfileImageName());

                                            profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    Log.d(TAG, "loadProfileImage: success");
                                                    Glide.with(context)
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

        if(event.getEventDateTimeCreated() != null){
            holder.homeEventListEventCreatedDate.setText(event.getEventDateTimeCreated());
        }
        else{
            holder.homeEventListEventCreatedDate.setText("-");
        }

        if(event.getEventTitle() != null){
            holder.homeEventListTitleTV.setText(event.getEventTitle());
        }
        else{
            holder.homeEventListTitleTV.setText("-");
        }

        if(event.getEventImageName() != null){
            final StorageReference eventImageRef = Variable.EVENT_SR.child(event.getEventImageName());

            eventImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Log.d(TAG, "loadEventImage: success");
                    GlideApp.with(context)
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

        if(event.getEventDescription() != null){
            holder.homeEventListDescriptionTV.setText(event.getEventDescription());
        }
        else{
            holder.homeEventListDescriptionTV.setText("-");
        }

        if(event.getEventStartDate() != null && event.getEventEndDate() != null){
            String duration = event.getEventStartDate() + "~" + event.getEventEndDate();
            holder.homeEventListDurationTV.setText(duration);
        }
        else{
            holder.homeEventListDurationTV.setText("-");
        }

        if(event.getEventCurrentAmount() > 0){
            String currentAmount = "RM " + event.getEventCurrentAmount();
            holder.homeEventListCurrentFundTV.setText(currentAmount);
        }
        else{
            String currentAmount = "RM 0";
            holder.homeEventListCurrentFundTV.setText(currentAmount);
        }

        if(event.getEventTargetAmount() > 0){
            String targetAmount = "RM " + event.getEventTargetAmount();
            holder.homeEventListTargetFundTV.setText(targetAmount);
        }
        else{
            String targetAmount = "RM 0";
            holder.homeEventListTargetFundTV.setText(targetAmount);
        }

        double fundProgress = (event.getEventCurrentAmount() / event.getEventTargetAmount()) * 100;
        holder.homeEventListPB.setProgress((int) fundProgress);

        holder.homeEventListDonateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();

                if(cUser != null){
                    Intent i = new Intent(context, HomeDonateActivity.class);
                    i.putExtra(Variable.HOME_USER_SESSION_ID, cUser.getUid());
                    i.putExtra(Variable.HOME_EVENT_SESSION_ID, event.getEventId());
                    context.startActivity(i);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return homeEventList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public LinearLayout homeEventListEventHandlerLL;
        public TextView homeEventListProfileNameTV, homeEventListEventCreatedDate, homeEventListTitleTV, homeEventListDescriptionTV, homeEventListDurationTV,
                homeEventListCurrentFundTV, homeEventListTargetFundTV;
        public ImageView homeEventListProfileIV, homeEventListIV;
        public ProgressBar homeEventListPB;
        public Button homeEventListDonateBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            homeEventListEventHandlerLL = itemView.findViewById(R.id.homeEventListEventHandlerLinearLayout);
            homeEventListProfileNameTV = itemView.findViewById(R.id.homeEventListProfileNameTextView);
            homeEventListEventCreatedDate = itemView.findViewById(R.id.homeEventListEventCreatedTextView);
            homeEventListProfileIV = itemView.findViewById(R.id.homeEventListProfileImageView);
            homeEventListTitleTV = itemView.findViewById(R.id.homeEventListTitleTextView);
            homeEventListIV = itemView.findViewById(R.id.homeEventListImageView);
            homeEventListDescriptionTV = itemView.findViewById(R.id.homeEventListDescriptionTextView);
            homeEventListDurationTV = itemView.findViewById(R.id.homeEventListDurationTextView);
            homeEventListPB = itemView.findViewById(R.id.homeEventListProgressBar);
            homeEventListCurrentFundTV = itemView.findViewById(R.id.homeEventListCurrentFundTextView);
            homeEventListTargetFundTV = itemView.findViewById(R.id.homeEventListTargetFundTextView);

            homeEventListDonateBtn = itemView.findViewById(R.id.homeEventListDonateButton);
        }
    }
}
