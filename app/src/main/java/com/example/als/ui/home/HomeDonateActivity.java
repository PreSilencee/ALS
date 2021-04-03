package com.example.als.ui.home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

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

import com.example.als.Config.Config;
import com.example.als.LoginActivity;
import com.example.als.R;
import com.example.als.handler.Connectivity;

import com.example.als.object.Donation;
import com.example.als.object.Event;
import com.example.als.object.Variable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;


public class HomeDonateActivity extends AppCompatActivity implements PaymentResultListener {

    //tag for console log
    private static final String TAG = "HomeDonateActivity";

    //connectivity
    private Connectivity device;

    //firebase auth variable
    private FirebaseAuth cAuth;

    //radio group
    private RadioGroup homeDonateRG;

    //radio button
    private RadioButton homeDonateRM10RB, homeDonateRM20RB, homeDonateRM50RB,
        homeDonateRM100RB, homeDonateOtherAmountRB;

    //textview
    private TextView homeDonatePaymentMethodTV;

    //edit text
    private EditText homeDonateOtherAmountET;

    private String userId;
    private String eventId;
    private String eventName;

    Button homeDonateRazorPayBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_donate);

        device = new Connectivity(HomeDonateActivity.this);
        Checkout.preload(getApplicationContext());


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

        if(session.hasExtra(Variable.HOME_EVENT_NAME_SESSION_ID)){
            eventName = session.getStringExtra(Variable.HOME_EVENT_NAME_SESSION_ID);
            Log.d(TAG, "eventName: "+ eventName);
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
                homeDonatePaymentMethodTV = findViewById(R.id.homeDonatePaymentMethodTextView);
                homeDonateRazorPayBtn = findViewById(R.id.homeDonateRazorPayButton);

                homeDonateRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        if(checkedId == -1){
                            homeDonatePaymentMethodTV.setVisibility(View.GONE);
                            homeDonateRazorPayBtn.setVisibility(View.GONE);
                        }
                        else{
                            homeDonatePaymentMethodTV.setVisibility(View.VISIBLE);
                            homeDonateRazorPayBtn.setVisibility(View.VISIBLE);
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

                homeDonateRazorPayBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startPayment();
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

    @Override
    protected void onDestroy() {
        //stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    public void startPayment() {

        if(getSelectedAmount() != 0.0){
            double amount = getSelectedAmount() * 100;
            Checkout checkout = new Checkout();
            checkout.setImage(R.mipmap.ic_logo);
            final Activity activity = this;

            try {
                JSONObject options = new JSONObject();

                options.put("name", "AlittleShare");
                options.put("description", "Donation for "+eventName);
                options.put("currency", "MYR");
                options.put("amount", amount);

                checkout.open(activity, options);

            } catch(Exception e) {
                Log.e(TAG, "Error in starting Razorpay Checkout", e);
            }
        }
        else{
            Toasty.warning(getApplicationContext(), "Please enter valid donation", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onPaymentSuccess(String s) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US);
        Date dateObj = Calendar.getInstance().getTime();
        final String currentDateTime = simpleDateFormat.format(dateObj);
        final Donation donation = new Donation();
        donation.setDonationCurrencyCode("MYR");
        donation.setDonationStatus(Variable.SUCCESS);
        donation.setDonationAmount(getSelectedAmount());
        donation.setDonationId(s);
        donation.setDonationDateTime(currentDateTime);
        donation.setDonationEventId(eventId);
        donation.setDonationUserId(userId);
        donation.setDonationPaymentMethod("RAZORPAY");

        Variable.DONATION_REF.child(s).setValue(donation).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                                Toasty.success(getApplicationContext(), "Donate Successfully", Toast.LENGTH_SHORT, true).show();
                                                finish();
                                            }
                                            else{
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

    @Override
    public void onPaymentError(int i, String s) {
        setResult(RESULT_CANCELED);
        Toasty.error(getApplicationContext(), "Payment failed", Toast.LENGTH_LONG).show();
    }
}