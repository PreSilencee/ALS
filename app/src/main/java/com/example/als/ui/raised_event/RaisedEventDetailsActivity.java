package com.example.als.ui.raised_event;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.als.LoginActivity;
import com.example.als.R;
import com.example.als.handler.Connectivity;
import com.example.als.handler.GlideApp;
import com.example.als.object.Event;
import com.example.als.object.Variable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import es.dmoral.toasty.Toasty;

public class RaisedEventDetailsActivity extends AppCompatActivity {

    private static final String TAG = "EventDetailsAct";
    private Connectivity device;
    private FirebaseAuth cAuth;
    private ImageView eventDetailsIV;
    private TextView eventDetailsNameTV, eventDetailsDescriptionTV,
            eventDetailsStartDateTV, eventDetailsEndDateTV,
            eventDetailsCurrentFundTV, eventDetailsTargetFundTV;
    private String eventSessionId;
    private ProgressBar fundProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raised_event_details);

        device = new Connectivity(RaisedEventDetailsActivity.this);

        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            cAuth = FirebaseAuth.getInstance();
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



        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            FirebaseUser cUser = cAuth.getCurrentUser();

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
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}