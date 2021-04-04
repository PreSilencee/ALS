package com.example.als.ui.raised_event;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.als.LoginActivity;
import com.example.als.R;
import com.example.als.handler.Connectivity;
import com.example.als.handler.GlideApp;
import com.example.als.object.Event;
import com.example.als.object.Variable;
import com.example.als.ui.search.SearchActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import es.dmoral.toasty.Toasty;

public class RaisedEventDetailsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "EventDetailsAct";
    private Connectivity device;

    private ImageView eventDetailsIV;
    private TextView eventDetailsNameTV, eventDetailsDescriptionTV,
            eventDetailsStartDateTV, eventDetailsEndDateTV,
            eventDetailsCurrentFundTV, eventDetailsTargetFundTV;
    private String eventSessionId;
    private ProgressBar fundProgressBar;

    Toolbar eventDetailsCustomizeSearchViewToolbar;
    Button eventDetailsCustomizeSearchBtn;
    SwipeRefreshLayout eventDetailsSRL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raised_event_details);

        device = new Connectivity(RaisedEventDetailsActivity.this);

        eventDetailsCustomizeSearchViewToolbar = findViewById(R.id.customizeHomeUserToolbar);
        eventDetailsCustomizeSearchBtn = findViewById(R.id.customizeHomeUserSearchButton);

        setSupportActionBar(eventDetailsCustomizeSearchViewToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        eventDetailsCustomizeSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SearchActivity.class));
            }
        });

        eventDetailsSRL = findViewById(R.id.raisedEventDetailsSwipeRefreshLayout);
        //swipeRefreshLayout function
        eventDetailsSRL.setOnRefreshListener(this);
        eventDetailsSRL.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        eventDetailsSRL.post(new Runnable() {
            @Override
            public void run() {
                eventDetailsSRL.setRefreshing(true);
                initialize();
            }
        });

//        if(!device.haveNetwork()){
//            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
//        }
//        else{
//            cAuth = FirebaseAuth.getInstance();
//            eventDetailsIV = findViewById(R.id.eventDetailsMainImage);
//            eventDetailsNameTV = findViewById(R.id.eventDetailsNameTextView);
//            eventDetailsDescriptionTV = findViewById(R.id.eventDetailsDescriptionTextView);
//            eventDetailsStartDateTV = findViewById(R.id.eventDetailsStartDateTextView);
//            eventDetailsEndDateTV = findViewById(R.id.eventDetailsEndDateTextView);
//            eventDetailsCurrentFundTV = findViewById(R.id.eventDetailsCurrentFundTextView);
//            eventDetailsTargetFundTV = findViewById(R.id.eventDetailsTargetFundTextView);
//            fundProgressBar = findViewById(R.id.eventDetailsTargetFundProgressBar);
//
//            Intent i = getIntent();
//            eventSessionId = i.getStringExtra(Variable.EVENT_SESSION_ID);
//
//            if(eventSessionId != null){
//                Variable.EVENT_REF.child(eventSessionId).addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if(snapshot.exists()){
//                            Event event = snapshot.getValue(Event.class);
//                            if(event != null){
//                                if(event.getEventImageName() != null){
//                                    final StorageReference eventImageRef = Variable.EVENT_SR.child(event.getEventImageName());
//
//                                    eventImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                        @Override
//                                        public void onSuccess(Uri uri) {
//                                            Log.d(TAG, "loadImage: success");
//                                            GlideApp.with(RaisedEventDetailsActivity.this)
//                                                    .load(uri)
//                                                    .placeholder(R.drawable.loading_image)
//                                                    .into(eventDetailsIV);
//                                        }
//                                    }).addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//                                            Log.d(TAG, "loadImage:Failed");
//                                            eventDetailsIV.setImageResource(R.drawable.loading_image);
//                                        }
//                                    });
//                                }
//                                else{
//                                    eventDetailsIV.setImageResource(R.drawable.loading_image);
//                                }
//
//                                if(event.getEventTitle() != null){
//                                    eventDetailsNameTV.setText(event.getEventTitle());
//                                }
//                                else{
//                                    eventDetailsNameTV.setText("-");
//                                }
//
//                                if(event.getEventDescription() != null){
//                                    eventDetailsDescriptionTV.setText(event.getEventDescription());
//                                }
//                                else{
//                                    eventDetailsDescriptionTV.setText("-");
//                                }
//
//                                if(event.getEventStartDate() != null){
//                                    eventDetailsStartDateTV.setText(event.getEventStartDate());
//                                }
//                                else{
//                                    eventDetailsStartDateTV.setText("-");
//                                }
//
//                                if(event.getEventEndDate() != null){
//                                    eventDetailsEndDateTV.setText(event.getEventEndDate());
//                                }
//                                else{
//                                    eventDetailsEndDateTV.setText("-");
//                                }
//
//                                if(event.getEventCurrentAmount() > 0){
//                                    String currentAmount = "RM "+event.getEventCurrentAmount();
//                                    eventDetailsCurrentFundTV.setText(currentAmount);
//                                }
//                                else{
//                                    String currentAmount = "RM 0";
//                                    eventDetailsCurrentFundTV.setText(currentAmount);
//                                }
//
//                                if(event.getEventTargetAmount() > 0){
//                                    String targetAmount = "RM "+event.getEventTargetAmount();
//                                    eventDetailsTargetFundTV.setText(targetAmount);
//                                }
//                                else{
//                                    String targetAmount = "RM 0";
//                                    eventDetailsTargetFundTV.setText(targetAmount);
//                                }
//
//                                double fundProgress = (event.getEventCurrentAmount()/event.getEventTargetAmount())*100;
//                                fundProgressBar.setProgress((int)fundProgress);
//
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                        Log.d(TAG, "databaseError: "+error.getMessage());
//                    }
//                });
//            }
//
//
//
//        }
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

            eventDetailsIV = findViewById(R.id.eventDetailsMainImage);
            eventDetailsNameTV = findViewById(R.id.eventDetailsNameTextView);
            eventDetailsDescriptionTV = findViewById(R.id.eventDetailsDescriptionTextView);
            eventDetailsStartDateTV = findViewById(R.id.eventDetailsStartDateTextView);
            eventDetailsEndDateTV = findViewById(R.id.eventDetailsEndDateTextView);
            eventDetailsCurrentFundTV = findViewById(R.id.eventDetailsCurrentFundTextView);
            eventDetailsTargetFundTV = findViewById(R.id.eventDetailsTargetFundTextView);
            fundProgressBar = findViewById(R.id.eventDetailsTargetFundProgressBar);

            Intent i = getIntent();
            eventSessionId = i.getStringExtra(Variable.EVENT_SESSION_ID);

            if(eventSessionId != null){
                Variable.EVENT_REF.child(eventSessionId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Event event = snapshot.getValue(Event.class);
                            if(event != null){
                                if(event.getEventImageName() != null){
                                    final StorageReference eventImageRef = Variable.EVENT_SR.child(event.getEventImageName());

                                    eventImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Log.d(TAG, "loadImage: success");
                                            GlideApp.with(RaisedEventDetailsActivity.this)
                                                    .load(uri)
                                                    .placeholder(R.drawable.loading_image)
                                                    .into(eventDetailsIV);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "loadImage:Failed");
                                            eventDetailsIV.setImageResource(R.drawable.loading_image);
                                        }
                                    });
                                }
                                else{
                                    eventDetailsIV.setImageResource(R.drawable.loading_image);
                                }

                                if(event.getEventTitle() != null){
                                    eventDetailsNameTV.setText(event.getEventTitle());
                                }
                                else{
                                    eventDetailsNameTV.setText("-");
                                }

                                if(event.getEventDescription() != null){
                                    eventDetailsDescriptionTV.setText(event.getEventDescription());
                                }
                                else{
                                    eventDetailsDescriptionTV.setText("-");
                                }

                                if(event.getEventStartDate() != null){
                                    eventDetailsStartDateTV.setText(event.getEventStartDate());
                                }
                                else{
                                    eventDetailsStartDateTV.setText("-");
                                }

                                if(event.getEventEndDate() != null){
                                    eventDetailsEndDateTV.setText(event.getEventEndDate());
                                }
                                else{
                                    eventDetailsEndDateTV.setText("-");
                                }

                                if(event.getEventCurrentAmount() > 0){
                                    String currentAmount = "RM "+event.getEventCurrentAmount();
                                    eventDetailsCurrentFundTV.setText(currentAmount);
                                }
                                else{
                                    String currentAmount = "RM 0";
                                    eventDetailsCurrentFundTV.setText(currentAmount);
                                }

                                if(event.getEventTargetAmount() > 0){
                                    String targetAmount = "RM "+event.getEventTargetAmount();
                                    eventDetailsTargetFundTV.setText(targetAmount);
                                }
                                else{
                                    String targetAmount = "RM 0";
                                    eventDetailsTargetFundTV.setText(targetAmount);
                                }

                                double fundProgress = (event.getEventCurrentAmount()/event.getEventTargetAmount())*100;
                                fundProgressBar.setProgress((int)fundProgress);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "databaseError: "+error.getMessage());
                    }
                });
            }

            eventDetailsSRL.setRefreshing(false);

        }
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
                Intent i = new Intent(RaisedEventDetailsActivity.this, LoginActivity.class);

                //clear the background task
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        }
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
                Intent i = new Intent(RaisedEventDetailsActivity.this, LoginActivity.class);

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
    public void onRefresh() {
        initialize();
    }
}