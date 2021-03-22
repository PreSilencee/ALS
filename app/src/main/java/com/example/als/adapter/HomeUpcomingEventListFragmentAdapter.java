package com.example.als.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.als.R;
import com.example.als.handler.GlideApp;
import com.example.als.object.Contributor;
import com.example.als.object.Event;
import com.example.als.object.Organization;
import com.example.als.object.Variable;
import com.example.als.ui.home.HomeUserViewDetailsActivity;
import com.example.als.ui.more.AccountActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class HomeUpcomingEventListFragmentAdapter extends RecyclerView.Adapter<HomeUpcomingEventListFragmentAdapter.ViewHolder> {

    //console tag
    private static final String TAG = "HomeEventListFragment";

    //create an array list for Event object
    private List<Event> homeUpcomingEventList;

    //create a context for the adapter
    private Context context;

    //constructor (home event list, context)
    public HomeUpcomingEventListFragmentAdapter(List<Event> homeUpcomingEventList, Context context) {
        this.homeUpcomingEventList = homeUpcomingEventList;
        this.context = context;
    }

    @NonNull
    @Override
    public HomeUpcomingEventListFragmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.home_upcoming_event_list_view_layout,parent,false);
        return new HomeUpcomingEventListFragmentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HomeUpcomingEventListFragmentAdapter.ViewHolder holder, int position) {
        //get position of donation
        final Event event = homeUpcomingEventList.get(position);

        //if event handler not null
        if(event.getEventHandler() != null){
            //check whether organization or contributor
            Variable.ORGANIZATION_REF.child(event.getEventHandler()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull final DataSnapshot snapshot) {
                    //if exists
                    if(snapshot.exists()){
                        //get organization object
                        final Organization organization = snapshot.getValue(Organization.class);

                        //if organization obejct not null
                        if(organization != null){

                            //if organization name not null
                            if(organization.getOrganizationName() != null){

                                //set name into text view
                                holder.homeUpcomingEventListProfileNameTV.setText(organization.getOrganizationName());

                                //enable the onclick text view
                                holder.homeUpcomingEventListProfileNameTV.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
                                        if(cUser != null){
                                            if(event.getEventHandler().equals(cUser.getUid())){
                                                context.startActivity(new Intent(context, AccountActivity.class));
                                            }
                                            else{
                                                //go to the home user details page
                                                Intent i = new Intent(context, HomeUserViewDetailsActivity.class);
                                                i.putExtra(Variable.HOME_USER_SESSION_ID, event.getEventHandler());
                                                context.startActivity(i);
                                            }
                                        }
                                    }
                                });
                            }
                            else{
                                //set "-" as default
                                holder.homeUpcomingEventListProfileNameTV.setText("-");
                            }

                            //if organization profile image name not null
                            if(organization.getOrganizationProfileImageName() != null){

                                //go to the firebase storage reference
                                StorageReference profileImageRef = Variable.ORGANIZATION_SR.child(event.getEventHandler())
                                        .child("profile").child(organization.getOrganizationProfileImageName());

                                //get download url
                                profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Log.d(TAG, "loadProfileImage: success");
                                        //push image into image view
                                        Glide.with(context)
                                                .load(uri)
                                                .placeholder(R.drawable.loading_image)
                                                .into(holder.homeUpcomingEventListProfileIV);
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "loadProfileImage:Failed");
                                                //show loading image view
                                                holder.homeUpcomingEventListProfileIV.setImageResource(R.drawable.loading_image);
                                            }
                                        });
                            }
                            else{
                                //show loading image view
                                holder.homeUpcomingEventListProfileIV.setImageResource(R.drawable.loading_image);
                            }
                        }
                    }
                    else{
                        //check whether he/she is contributor
                        Variable.CONTRIBUTOR_REF.child(event.getEventHandler()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                                //if exists
                                if(snapshot.exists()){

                                    //get object of contributor
                                    final Contributor contributor = snapshot.getValue(Contributor.class);

                                    //if contributor object not null
                                    if(contributor != null){

                                        //if contributor's name not null
                                        if(contributor.getName() != null){

                                            //set name to text view
                                            holder.homeUpcomingEventListProfileNameTV.setText(contributor.getName());

                                            //enable the onclick text view
                                            holder.homeUpcomingEventListProfileNameTV.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
                                                    if(cUser != null){
                                                        if(event.getEventHandler().equals(cUser.getUid())){
                                                            context.startActivity(new Intent(context, AccountActivity.class));
                                                        }
                                                        else{
                                                            //go to the home user details page
                                                            Intent i = new Intent(context, HomeUserViewDetailsActivity.class);
                                                            i.putExtra(Variable.HOME_USER_SESSION_ID, event.getEventHandler());
                                                            context.startActivity(i);
                                                        }
                                                    }

                                                }
                                            });
                                        }
                                        else{
                                            //set "-" as default
                                            holder.homeUpcomingEventListProfileNameTV.setText("-");
                                        }

                                        if(contributor.getProfileImageUrl() != null){
                                            Uri photoUri = Uri.parse(contributor.getProfileImageUrl());
                                            Log.d(TAG, "loadProfileImage: success");
                                            //push image into image view
                                            GlideApp.with(context)
                                                    .load(photoUri)
                                                    .placeholder(R.drawable.loading_image)
                                                    .into(holder.homeUpcomingEventListProfileIV);
                                        }
                                        //if contributor profile image name not null
                                        else if(contributor.getProfileImageName() != null){

                                            //go to the firebase storage reference
                                            StorageReference profileImageRef = Variable.CONTRIBUTOR_SR.child(event.getEventHandler())
                                                    .child("profile").child(contributor.getProfileImageName());

                                            //get download url
                                            profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    Log.d(TAG, "loadProfileImage: success");
                                                    //push image into image view
                                                    Glide.with(context)
                                                            .load(uri)
                                                            .placeholder(R.drawable.loading_image)
                                                            .into(holder.homeUpcomingEventListProfileIV);
                                                }
                                            })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d(TAG, "loadProfileImage:Failed");
                                                            //show loading image view
                                                            holder.homeUpcomingEventListProfileIV.setImageResource(R.drawable.loading_image);
                                                        }
                                                    });
                                        }
                                        else{
                                            //show loading image view
                                            holder.homeUpcomingEventListProfileIV.setImageResource(R.drawable.loading_image);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                //if database error
                                Log.d(TAG, "databaseerror: " + error.getMessage());
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    //if database error
                    Log.d(TAG, "databaseerror: "+ error.getMessage());
                }
            });
        }

        //if event date time created not null
        if(event.getEventDateTimeCreated() != null){
            holder.homeUpcomingEventListEventCreatedDate.setText(event.getEventDateTimeCreated());
        }
        else{
            //set "-" as default
            holder.homeUpcomingEventListEventCreatedDate.setText("-");
        }

        //if event title not null
        if(event.getEventTitle() != null){
            holder.homeUpcomingEventListTitleTV.setText(event.getEventTitle());
        }
        else{
            //set "-" as default
            holder.homeUpcomingEventListTitleTV.setText("-");
        }

        //if event image name not null
        if(event.getEventImageName() != null){

            //define the url for image
            final StorageReference eventImageRef = Variable.EVENT_SR.child(event.getEventImageName());

            //get url and download it
            eventImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Log.d(TAG, "loadEventImage: success");

                    //push image to image view
                    GlideApp.with(context)
                            .load(uri)
                            .placeholder(R.drawable.loading_image)
                            .into(holder.homeUpcomingEventListIV);
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "loadEventImage:Failed");
                            //show loading image
                            holder.homeUpcomingEventListIV.setImageResource(R.drawable.loading_image);
                        }
                    });
        }
        else{
            //show loading image
            holder.homeUpcomingEventListIV.setImageResource(R.drawable.loading_image);
        }

        //if event description not null
        if(event.getEventDescription() != null){
            holder.homeUpcomingEventListDescriptionTV.setText(event.getEventDescription());
        }
        else{
            //set "-" as default
            holder.homeUpcomingEventListDescriptionTV.setText("-");
        }

        //if event start date and end date not null
        if(event.getEventStartDate() != null && event.getEventEndDate() != null){
            String duration = event.getEventStartDate() + "~" + event.getEventEndDate();
            holder.homeUpcomingEventListDurationTV.setText(duration);
        }
        else{
            //set "-" as default
            holder.homeUpcomingEventListDurationTV.setText("-");
        }
    }

    @Override
    public int getItemCount() {
        return homeUpcomingEventList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{


        //text view
        public TextView homeUpcomingEventListProfileNameTV, homeUpcomingEventListEventCreatedDate, homeUpcomingEventListTitleTV,
                homeUpcomingEventListDescriptionTV, homeUpcomingEventListDurationTV;

        //image view
        public ImageView homeUpcomingEventListProfileIV, homeUpcomingEventListIV;

        public ViewHolder(@NonNull View itemView){
            super(itemView);


            //find id for text view
            homeUpcomingEventListProfileNameTV = itemView.findViewById(R.id.homeUpcomingEventListProfileNameTextView);
            homeUpcomingEventListEventCreatedDate = itemView.findViewById(R.id.homeUpcomingEventListEventCreatedTextView);
            homeUpcomingEventListDurationTV = itemView.findViewById(R.id.homeUpcomingEventListDurationTextView);
            homeUpcomingEventListProfileIV = itemView.findViewById(R.id.homeUpcomingEventListProfileImageView);
            homeUpcomingEventListTitleTV = itemView.findViewById(R.id.homeUpcomingEventListTitleTextView);
            homeUpcomingEventListIV = itemView.findViewById(R.id.homeUpcomingEventListImageView);
            homeUpcomingEventListDescriptionTV = itemView.findViewById(R.id.homeUpcomingEventListDescriptionTextView);
        }
    }
}
