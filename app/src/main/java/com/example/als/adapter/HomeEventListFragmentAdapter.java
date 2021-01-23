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
import com.example.als.ui.settings.AccountActivity;
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

    //console tag
    private static final String TAG = "HomeEventListFragment";

    //create an array list for Event object
    private List<Event> homeEventList;

    //create a context for the adapter
    private Context context;

    //constructor (home event list, context)
    public HomeEventListFragmentAdapter(List<Event> homeEventList, Context context) {
        this.homeEventList = homeEventList;
        this.context = context;
    }

    //create view for each event object
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.home_event_list_view_layout,parent,false);
        return new HomeEventListFragmentAdapter.ViewHolder(view);
    }

    //attach the data of the event object
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        //get position of donation
        final Event event = homeEventList.get(position);

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
                                holder.homeEventListProfileNameTV.setText(organization.getOrganizationName());

                                //enable the onclick text view
                                holder.homeEventListProfileNameTV.setOnClickListener(new View.OnClickListener() {
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
                                                i.putExtra(Variable.HOME_USER_SESSION_ID, organization.getUserId());
                                                context.startActivity(i);
                                            }
                                        }
                                    }
                                });
                            }
                            else{
                                //set "-" as default
                                holder.homeEventListProfileNameTV.setText("-");
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
                                                .into(holder.homeEventListProfileIV);
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "loadProfileImage:Failed");
                                                //show loading image view
                                                holder.homeEventListIV.setImageResource(R.drawable.loading_image);
                                            }
                                        });
                            }
                            else{
                                //show loading image view
                                holder.homeEventListIV.setImageResource(R.drawable.loading_image);
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
                                            holder.homeEventListProfileNameTV.setText(contributor.getName());

                                            //enable the onclick text view
                                            holder.homeEventListProfileNameTV.setOnClickListener(new View.OnClickListener() {
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
                                                            i.putExtra(Variable.HOME_USER_SESSION_ID, contributor.getUserId());
                                                            context.startActivity(i);
                                                        }
                                                    }

                                                }
                                            });
                                        }
                                        else{
                                            //set "-" as default
                                            holder.homeEventListProfileNameTV.setText("-");
                                        }

                                        //if contributor profile image name not null
                                        if(contributor.getProfileImageName() != null){

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
                                                            .into(holder.homeEventListProfileIV);
                                                }
                                            })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d(TAG, "loadProfileImage:Failed");
                                                            //show loading image view
                                                            holder.homeEventListIV.setImageResource(R.drawable.loading_image);
                                                        }
                                                    });
                                        }
                                        else{
                                            //show loading image view
                                            holder.homeEventListIV.setImageResource(R.drawable.loading_image);
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
            holder.homeEventListEventCreatedDate.setText(event.getEventDateTimeCreated());
        }
        else{
            //set "-" as default
            holder.homeEventListEventCreatedDate.setText("-");
        }

        //if event title not null
        if(event.getEventTitle() != null){
            holder.homeEventListTitleTV.setText(event.getEventTitle());
        }
        else{
            //set "-" as default
            holder.homeEventListTitleTV.setText("-");
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
                            .into(holder.homeEventListIV);
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "loadEventImage:Failed");
                            //show loading image
                            holder.homeEventListIV.setImageResource(R.drawable.loading_image);
                        }
                    });
        }
        else{
            //show loading image
            holder.homeEventListIV.setImageResource(R.drawable.loading_image);
        }

        //if event description not null
        if(event.getEventDescription() != null){
            holder.homeEventListDescriptionTV.setText(event.getEventDescription());
        }
        else{
            //set "-" as default
            holder.homeEventListDescriptionTV.setText("-");
        }

        //if event start date and end date not null
        if(event.getEventStartDate() != null && event.getEventEndDate() != null){
            String duration = event.getEventStartDate() + "~" + event.getEventEndDate();
            holder.homeEventListDurationTV.setText(duration);
        }
        else{
            //set "-" as default
            holder.homeEventListDurationTV.setText("-");
        }

        //if event current amount more than 0
        if(event.getEventCurrentAmount() > 0){
            String currentAmount = "RM " + event.getEventCurrentAmount();
            holder.homeEventListCurrentFundTV.setText(currentAmount);
        }
        else{
            //set "RM 0" as default
            String currentAmount = "RM 0";
            holder.homeEventListCurrentFundTV.setText(currentAmount);
        }

        //if event target amount more than 0
        if(event.getEventTargetAmount() > 0){
            String targetAmount = "RM " + event.getEventTargetAmount();
            holder.homeEventListTargetFundTV.setText(targetAmount);
        }
        else{
            //set "RM 0" as default
            String targetAmount = "RM 0";
            holder.homeEventListTargetFundTV.setText(targetAmount);
        }

        //calculate the currentAmount/targetAmount
        double fundProgress = (event.getEventCurrentAmount() / event.getEventTargetAmount()) * 100;
        //apply to the progress bar
        holder.homeEventListPB.setProgress((int) fundProgress);

        //donate btn
        holder.homeEventListDonateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
                //user != null
                if(cUser != null){
                    Intent i = new Intent(context, HomeDonateActivity.class);
                    i.putExtra(Variable.HOME_USER_SESSION_ID, cUser.getUid());
                    i.putExtra(Variable.HOME_EVENT_SESSION_ID, event.getEventId());
                    context.startActivity(i);
                }

            }
        });
    }

    //get the size of array list for home event list
    @Override
    public int getItemCount() {
        return homeEventList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        //linear layout
        public LinearLayout homeEventListEventHandlerLL;

        //text view
        public TextView homeEventListProfileNameTV, homeEventListEventCreatedDate, homeEventListTitleTV, homeEventListDescriptionTV, homeEventListDurationTV,
                homeEventListCurrentFundTV, homeEventListTargetFundTV;

        //image view
        public ImageView homeEventListProfileIV, homeEventListIV;

        //progress bar
        public ProgressBar homeEventListPB;

        //button
        public Button homeEventListDonateBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //find id for linear layout
            homeEventListEventHandlerLL = itemView.findViewById(R.id.homeEventListEventHandlerLinearLayout);

            //find id for text view
            homeEventListProfileNameTV = itemView.findViewById(R.id.homeEventListProfileNameTextView);
            homeEventListEventCreatedDate = itemView.findViewById(R.id.homeEventListEventCreatedTextView);
            homeEventListProfileIV = itemView.findViewById(R.id.homeEventListProfileImageView);
            homeEventListTitleTV = itemView.findViewById(R.id.homeEventListTitleTextView);
            homeEventListIV = itemView.findViewById(R.id.homeEventListImageView);
            homeEventListDescriptionTV = itemView.findViewById(R.id.homeEventListDescriptionTextView);
            homeEventListDurationTV = itemView.findViewById(R.id.homeEventListDurationTextView);
            homeEventListCurrentFundTV = itemView.findViewById(R.id.homeEventListCurrentFundTextView);
            homeEventListTargetFundTV = itemView.findViewById(R.id.homeEventListTargetFundTextView);

            //find id for progress bar
            homeEventListPB = itemView.findViewById(R.id.homeEventListProgressBar);

            //find id for button
            homeEventListDonateBtn = itemView.findViewById(R.id.homeEventListDonateButton);
        }
    }
}
