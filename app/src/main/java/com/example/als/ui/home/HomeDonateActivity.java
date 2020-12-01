package com.example.als.ui.home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.als.LoginActivity;
import com.example.als.R;
import com.example.als.SettingsActivity;
import com.example.als.handler.Connectivity;
import com.example.als.object.Donation;
import com.example.als.object.Event;
import com.example.als.object.User;
import com.example.als.object.Variable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeDonateActivity extends AppCompatActivity{

    //tag for console log
    private static final String TAG = "HomeDonateActivity";
    //private GoogleSignInClient cGoogleSignInClient;

    //connectivity
    private Connectivity device;

    //firebase auth variable
    private FirebaseAuth cAuth;

    //radio group
    private RadioGroup homeDonateRG;

    //radio button
    private RadioButton homeDonateRM10RB, homeDonateRM20RB, homeDonateRM50RB,
        homeDonateRM100RB, homeDonateOtherAmountRB;


    //edit text
    private EditText homeDonateOtherAmountET;

    //button
    private Button homeDonateConfirmBtn;

    private String userId;
    private String eventId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_donate);

        device = new Connectivity(HomeDonateActivity.this);

        Intent session = getIntent();


        if(session.hasExtra(Variable.HOME_USER_SESSION_ID)){
            userId = session.getStringExtra(Variable.HOME_USER_SESSION_ID);
            Log.d(TAG, "userId" + userId);
        }
        else{
            Toasty.warning(getApplicationContext(), "Something went wrong. Please Try Again",Toast.LENGTH_SHORT,true).show();
            finish();
        }

        if(session.hasExtra(Variable.HOME_EVENT_SESSION_ID)){
            eventId = session.getStringExtra(Variable.HOME_EVENT_SESSION_ID);
            Log.d(TAG, "eventId: "+ eventId);
        }
        else{
            Toasty.warning(getApplicationContext(), "Something went wrong. Please Try Again",Toast.LENGTH_SHORT,true).show();
            finish();
        }


        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            cAuth = FirebaseAuth.getInstance();
            FirebaseUser cUser = cAuth.getCurrentUser();

            if(cUser != null){
                homeDonateRG = findViewById(R.id.homeDonateRadioGroup);
                homeDonateRM10RB = findViewById(R.id.homeDonateRM10RadioButton);
                homeDonateRM20RB = findViewById(R.id.homeDonateRM20RadioButton);
                homeDonateRM50RB = findViewById(R.id.homeDonateRM50RadioButton);
                homeDonateRM100RB = findViewById(R.id.homeDonateRM100RadioButton);
                homeDonateOtherAmountRB = findViewById(R.id.homeDonateOtherAmountRadioButton);
                homeDonateOtherAmountET = findViewById(R.id.homeDonateOtherAmountEditText);
                homeDonateConfirmBtn = findViewById(R.id.homeDonateConfirmButton);

                homeDonateRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        if(checkedId == -1){

                            homeDonateConfirmBtn.setVisibility(View.GONE);
                        }
                        else{
                            homeDonateConfirmBtn.setVisibility(View.VISIBLE);

                            homeDonateConfirmBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startDonation();
                                }
                            });

                        }
                    }
                });

                homeDonateOtherAmountRB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked){
                            homeDonateOtherAmountET.setVisibility(View.VISIBLE);
                        }
                        else{
                            homeDonateOtherAmountET.setVisibility(View.GONE);
                        }
                    }
                });
            }
            else{
                //show error message to console log
                Log.d(TAG, "getCurrentUser: failed");

                //show error message to user
                Toasty.error(getApplicationContext(), "Session is expired. Please relogin.", Toast.LENGTH_SHORT,true).show();

                //intent user to login page (relogin)
                Intent i = new Intent(HomeDonateActivity.this, LoginActivity.class);

                //clear the background task
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        //if device no network
        if(!device.haveNetwork()){
            //show error message
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            //get current user
            FirebaseUser cUser = cAuth.getCurrentUser();

            //if user != null
            if(cUser != null)
            {
                //show success message to console log
                Log.d(TAG, "getCurrentUser: success");
            }
            else
            {
                //show error message to console log
                Log.d(TAG, "getCurrentUser: failed");

                //show error message to user
                Toasty.error(getApplicationContext(), "Session is expired. Please relogin.", Toast.LENGTH_SHORT,true).show();

                //intent user to login page (relogin)
                Intent i = new Intent(HomeDonateActivity.this, LoginActivity.class);

                //clear the background task
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        }
    }

    @Override
    protected void onStop() {
        if(!device.haveNetwork()){
            //show error message
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!device.haveNetwork()){
            //show error message
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            //get current user
            FirebaseUser cUser = cAuth.getCurrentUser();

            //if user != null
            if(cUser != null)
            {
                //show success message to console log
                Log.d(TAG, "getCurrentUser: success");
            }
            else
            {
                //show error message to console log
                Log.d(TAG, "getCurrentUser: failed");

                //show error message to user
                Toasty.error(getApplicationContext(), "Session is expired. Please relogin.", Toast.LENGTH_SHORT,true).show();

                //intent user to login page (relogin)
                Intent i = new Intent(HomeDonateActivity.this, LoginActivity.class);

                //clear the background task
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        }
    }

    private double getSelectedAmount(){
        double selectedAmount = 0.0;

        if(homeDonateRM10RB.isChecked()){
            selectedAmount = 10.0;
        }
        else if(homeDonateRM20RB.isChecked()){
            selectedAmount = 20.0;
        }
        else if(homeDonateRM50RB.isChecked()){
            selectedAmount = 50.0;
        }
        else if(homeDonateRM100RB.isChecked()){
            selectedAmount = 100.0;
        }
        else if(homeDonateOtherAmountRB.isChecked()){
            if(TextUtils.isEmpty(homeDonateOtherAmountET.getText().toString().trim())){
                selectedAmount = 0.0;
            }
            else{
                selectedAmount = Double.parseDouble(homeDonateOtherAmountET.getText().toString().trim());
            }
        }

        return selectedAmount;
    }

    private void startDonation(){
        if(getSelectedAmount() != 0.0){
            AlertDialog.Builder alertDialogBuider = new AlertDialog.Builder(this);
            alertDialogBuider.setMessage("Are you sure want to donate to this event?")
                    .setCancelable(false)
                    .setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //delay 0.3 sec
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //a progress dialog to view progress of create account
                                    final ProgressDialog progressDialog = new ProgressDialog(HomeDonateActivity.this);

                                    //set message for progress dialog
                                    progressDialog.setMessage("Please wait awhile, we are processing");

                                    //show dialog
                                    progressDialog.show();
                                    final Donation donation = new Donation();
                                    donation.setDonationAmount(getSelectedAmount());
                                    donation.setDonationUserId(userId);
                                    donation.setDonationEventId(eventId);
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US);
                                    Date dateObj = Calendar.getInstance().getTime();
                                    final String currentDateTime = simpleDateFormat.format(dateObj);
                                    donation.setDonationDateTime(currentDateTime);

                                    DatabaseReference pushedDonation = Variable.DONATION_REF.push();

                                    String id = pushedDonation.getKey();
                                    donation.setDonationId(id);

                                    pushedDonation.setValue(donation).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Variable.EVENT_REF.child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if(snapshot.exists()){
                                                            Event event = snapshot.getValue(Event.class);

                                                            if(event != null){
                                                                event.setEventCurrentAmount(event.getEventCurrentAmount() + donation.getDonationAmount());
                                                                Map<String, Object> eventValues = event.eventMap();
                                                                Variable.EVENT_REF.child(eventId).updateChildren(eventValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){
                                                                            progressDialog.dismiss();
                                                                            Toasty.success(getApplicationContext(), "Donate Successfully", Toast.LENGTH_SHORT, true).show();
                                                                            finish();
                                                                        }
                                                                        else{
                                                                            progressDialog.dismiss();
                                                                            Toasty.error(getApplicationContext(), "Something went wrong. Please contact administrator", Toast.LENGTH_SHORT,true).show();
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        Log.d(TAG, "databaseError: "+ error.getDetails());
                                                    }
                                                });
                                            }
                                        }
                                    });



                                }
                            },300);
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alertDialog = alertDialogBuider.create();
            alertDialog.setTitle("Confirmation");
            alertDialog.show();

        }
        else{
            Toasty.error(getApplicationContext(), "Please Select/Type a valid amount", Toast.LENGTH_SHORT, true).show();
        }
    }

}