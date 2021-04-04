package com.example.als.ui.search;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.als.LoginActivity;
import com.example.als.R;
import com.example.als.handler.Connectivity;
import com.example.als.handler.GlideApp;
import com.example.als.object.Contributor;
import com.example.als.object.Event;
import com.example.als.object.Organization;
import com.example.als.object.Variable;
import com.example.als.ui.home.HomeDonateActivity;
import com.example.als.ui.raised_event.RaisedEventDetailsActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class SearchEventDetailsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = "SearchEventDetailsAct";
    private Connectivity device;
    private ImageView searchEventDetailsProfileIV, searchEventDetailsIV;
    private TextView searchEventDetailsProfileNameTV, searchEventDetailsCreateDateTimeTV, searchEventDetailsTitleTV, searchEventDetailsDurationTV,
            searchEventDetailsDescriptionTV, searchEventDetailsCurrentFundTV, searchEventDetailsTargetFundTV;

    private LinearLayout searchEventDetailsProgressLL;
    private ProgressBar searchEventDetailsPB;

    private Button searchEventDetailsDonateBtn;
    private String searchEventSessionId;
    Toolbar searchEventDetailsCustomizeSearchViewToolbar;
    Button searchEventDetailsCustomizeSearchBtn;
    SwipeRefreshLayout searchEventDetailsSRL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_event_details);

        device = new Connectivity(SearchEventDetailsActivity.this);

        searchEventDetailsCustomizeSearchViewToolbar = findViewById(R.id.customizeHomeUserToolbar);
        searchEventDetailsCustomizeSearchBtn = findViewById(R.id.customizeHomeUserSearchButton);

        setSupportActionBar(searchEventDetailsCustomizeSearchViewToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        searchEventDetailsCustomizeSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SearchActivity.class));
            }
        });

        searchEventDetailsSRL = findViewById(R.id.searchEventDetailsSwipeRefreshLayout);
        //swipeRefreshLayout function
        searchEventDetailsSRL.setOnRefreshListener(this);
        searchEventDetailsSRL.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        searchEventDetailsSRL.post(new Runnable() {
            @Override
            public void run() {
                searchEventDetailsSRL.setRefreshing(true);
                initialize();
            }
        });


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initialize(){
        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{

            searchEventDetailsProfileIV = findViewById(R.id.searchEventDetailsProfileImageView);
            searchEventDetailsProfileNameTV = findViewById(R.id.searchEventDetailsProfileNameTextView);
            searchEventDetailsIV = findViewById(R.id.searchEventDetailsImageView);
            searchEventDetailsCreateDateTimeTV = findViewById(R.id.searchEventDetailsCreatedTextView);
            searchEventDetailsTitleTV = findViewById(R.id.searchEventDetailsTitleTextView);
            searchEventDetailsDurationTV = findViewById(R.id.searchEventDetailsDurationTextView);
            searchEventDetailsDescriptionTV = findViewById(R.id.searchEventDetailsDescriptionTextView);
            searchEventDetailsProgressLL = findViewById(R.id.searchEventDetailsProgressLinearLayout);
            searchEventDetailsCurrentFundTV = findViewById(R.id.searchEventDetailsCurrentFundTextView);
            searchEventDetailsTargetFundTV = findViewById(R.id.searchEventDetailsTargetFundTextView);
            searchEventDetailsPB = findViewById(R.id.searchEventDetailsProgressBar);
            searchEventDetailsDonateBtn = findViewById(R.id.searchEventDetailsDonateButton);

            Intent i = getIntent();
            searchEventSessionId = i.getStringExtra(Variable.SEARCH_EVENT_SESSION_ID);

            if(searchEventSessionId != null){
                Variable.EVENT_REF.child(searchEventSessionId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            final Event event = snapshot.getValue(Event.class);
                            if(event != null){
                                if(event.getEventHandler() != null){
                                    Variable.CONTRIBUTOR_REF.child(event.getEventHandler()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                Contributor contributor = snapshot.getValue(Contributor.class);

                                                if(contributor != null){
                                                    if(contributor.getName() != null){
                                                        searchEventDetailsProfileNameTV.setText(contributor.getName());
                                                    }

                                                    if(contributor.getProfileImageUrl() != null){
                                                        Uri photoUri = Uri.parse(contributor.getProfileImageUrl());
                                                        Log.d(TAG, "loadProfileImage: success");
                                                        //push image into image view
                                                        GlideApp.with(getApplicationContext())
                                                                .load(photoUri)
                                                                .placeholder(R.drawable.loading_image)
                                                                .into(searchEventDetailsProfileIV);
                                                    }
                                                    //if contributor profile image name not null
                                                    else if(contributor.getProfileImageName() != null) {

                                                        //go to the firebase storage reference
                                                        StorageReference profileImageRef = Variable.CONTRIBUTOR_SR.child(contributor.getUserId())
                                                                .child("profile").child(contributor.getProfileImageName());

                                                        //get download url
                                                        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                Log.d(TAG, "loadProfileImage: success");
                                                                //push image into image view
                                                                Glide.with(getApplicationContext())
                                                                        .load(uri)
                                                                        .placeholder(R.drawable.loading_image)
                                                                        .into(searchEventDetailsProfileIV);
                                                            }
                                                        })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.d(TAG, "loadProfileImage:Failed");
                                                                        //show loading image view
                                                                        searchEventDetailsProfileIV.setImageResource(R.drawable.loading_image);
                                                                    }
                                                                });
                                                    }
                                                }
                                            }
                                            else{
                                                Variable.ORGANIZATION_REF.child(event.getEventHandler()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if(snapshot.exists()){
                                                            Organization organization = snapshot.getValue(Organization.class);

                                                            if(organization != null){
                                                                if(organization.getOrganizationName() != null){
                                                                    searchEventDetailsProfileNameTV.setText(organization.getOrganizationName());
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
                                                                            Glide.with(getApplicationContext())
                                                                                    .load(uri)
                                                                                    .placeholder(R.drawable.loading_image)
                                                                                    .into(searchEventDetailsProfileIV);
                                                                        }
                                                                    })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Log.d(TAG, "loadProfileImage:Failed");
                                                                                    //show loading image view
                                                                                    searchEventDetailsProfileIV.setImageResource(R.drawable.loading_image);
                                                                                }
                                                                            });
                                                                }
                                                                else{
                                                                    //show loading image view
                                                                    searchEventDetailsProfileIV.setImageResource(R.drawable.loading_image);
                                                                }
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        //
                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            //
                                        }
                                    });
                                }

                                //if event date time created not null
                                if(event.getEventDateTimeCreated() != null){
                                    searchEventDetailsCreateDateTimeTV.setText(event.getEventDateTimeCreated());
                                }

                                if(event.getEventImageName() != null){
                                    final StorageReference eventImageRef = Variable.EVENT_SR.child(event.getEventImageName());

                                    eventImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Log.d(TAG, "loadImage: success");
                                            GlideApp.with(SearchEventDetailsActivity.this)
                                                    .load(uri)
                                                    .placeholder(R.drawable.loading_image)
                                                    .into(searchEventDetailsIV);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "loadImage:Failed");
                                            searchEventDetailsIV.setImageResource(R.drawable.loading_image);
                                        }
                                    });
                                }
                                else{
                                    searchEventDetailsIV.setImageResource(R.drawable.loading_image);
                                }

                                if(event.getEventTitle() != null){
                                    searchEventDetailsTitleTV.setText(event.getEventTitle());
                                }

                                if(event.getEventDescription() != null){
                                    searchEventDetailsDescriptionTV.setText(event.getEventDescription());
                                }


                                //if event start date and end date not null
                                if(event.getEventStartDate() != null && event.getEventEndDate() != null){
                                    String duration = event.getEventStartDate() + "~" + event.getEventEndDate();
                                    searchEventDetailsDurationTV.setText(duration);
                                }

                                if(event.getEventCurrentAmount() > 0){
                                    String currentAmount = "RM "+event.getEventCurrentAmount();
                                    searchEventDetailsCurrentFundTV.setText(currentAmount);
                                }
                                else{
                                    String currentAmount = "RM 0";
                                    searchEventDetailsCurrentFundTV.setText(currentAmount);
                                }

                                if(event.getEventTargetAmount() > 0){
                                    String targetAmount = "RM "+event.getEventTargetAmount();
                                    searchEventDetailsTargetFundTV.setText(targetAmount);
                                }
                                else{
                                    String targetAmount = "RM 0";
                                    searchEventDetailsTargetFundTV.setText(targetAmount);
                                }

                                double fundProgress = (event.getEventCurrentAmount()/event.getEventTargetAmount())*100;
                                searchEventDetailsPB.setProgress((int)fundProgress);

                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                                Date dateObj = Calendar.getInstance().getTime();
                                Date currentDate = null;

                                try {
                                    currentDate = simpleDateFormat.parse(simpleDateFormat.format(dateObj));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    Date eventStartDate = simpleDateFormat.parse(event.getEventStartDate());
                                    Date eventEndDate = simpleDateFormat.parse(event.getEventEndDate());

                                    if(((currentDate.compareTo(eventStartDate) == 0 || currentDate.compareTo(eventStartDate) > 0))
                                            && ((currentDate.compareTo(eventEndDate) == 0 || currentDate.compareTo(eventEndDate) < 0))){
                                        searchEventDetailsProgressLL.setVisibility(View.VISIBLE);
                                        searchEventDetailsDonateBtn.setVisibility(View.VISIBLE);
                                        searchEventDetailsDonateBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
                                                if(cUser != null){
                                                    Intent i = new Intent(getApplicationContext(), HomeDonateActivity.class);
                                                    i.putExtra(Variable.HOME_USER_SESSION_ID, cUser.getUid());
                                                    i.putExtra(Variable.HOME_EVENT_SESSION_ID, event.getEventId());
                                                    i.putExtra(Variable.HOME_EVENT_NAME_SESSION_ID, event.getEventTitle());
                                                    startActivity(i);
                                                }
                                            }
                                        });
                                    }
                                    else{
                                        searchEventDetailsProgressLL.setVisibility(View.GONE);
                                        searchEventDetailsDonateBtn.setVisibility(View.GONE);
                                    }

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "databaseError: "+error.getMessage());
                    }
                });
                searchEventDetailsSRL.setRefreshing(false);
            }
        }
    }

    @Override
    public void onRefresh() {
        initialize();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();

            if(cUser == null) {
                //show error message to console log
                Log.d(TAG, "getCurrentUser: failed");

                //show error message to user
                Toasty.error(getApplicationContext(), "Session is expired. Please relogin.", Toast.LENGTH_SHORT,true).show();

                //intent user to login page (relogin)
                Intent i = new Intent(SearchEventDetailsActivity.this, LoginActivity.class);

                //clear the background task
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();

            if(cUser == null) {
                //show error message to console log
                Log.d(TAG, "getCurrentUser: failed");

                //show error message to user
                Toasty.error(getApplicationContext(), "Session is expired. Please relogin.", Toast.LENGTH_SHORT,true).show();

                //intent user to login page (relogin)
                Intent i = new Intent(SearchEventDetailsActivity.this, LoginActivity.class);

                //clear the background task
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        }
    }
}