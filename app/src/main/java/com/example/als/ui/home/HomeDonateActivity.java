package com.example.als.ui.home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalFuturePaymentActivity;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;


import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Map;

import es.dmoral.toasty.Toasty;


public class HomeDonateActivity extends AppCompatActivity{

    public static final int PAYPAL_REQUEST_CODE = 7171;
    public static final int PAYPAL_REQUEST_FUTURE_CODE = 7172;

    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;
    private static PayPalConfiguration config;

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

    //textview
    private TextView homeDonatePaymentMethodTV;

    //button
    private Button homeDonatePayPalBtn;

    //edit text
    private EditText homeDonateOtherAmountET;

    private String userId;
    private String eventId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_donate);

        device = new Connectivity(HomeDonateActivity.this);
        configPayPal();

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
                homeDonatePaymentMethodTV = findViewById(R.id.homeDonatePaymentMethodTextView);
                homeDonatePayPalBtn = findViewById(R.id.homeDonatePayPalButton);

                homeDonateRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        if(checkedId == -1){
                            homeDonatePaymentMethodTV.setVisibility(View.GONE);
                            homeDonatePayPalBtn.setVisibility(View.GONE);
                        }
                        else{
                            homeDonatePaymentMethodTV.setVisibility(View.VISIBLE);
                            homeDonatePayPalBtn.setVisibility(View.VISIBLE);
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

                homeDonatePayPalBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startPayPalDonation();
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

    private void configPayPal(){
        config = new PayPalConfiguration()
                .environment(CONFIG_ENVIRONMENT)
                .clientId(Config.PAYPAL_CLIENT_ID);
    }

    private void startPayPalDonation(){
        if(getSelectedAmount() != 0.0){
            Intent intent = new Intent(this, PayPalService.class);
            intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
            startService(intent);
            PayPalPayment payPalPayment = new PayPalPayment(BigDecimal.valueOf(getSelectedAmount()), "MYR",
                    "Donation for "+eventId, PayPalPayment.PAYMENT_INTENT_SALE);
            Intent payment = new Intent(this, PaymentActivity.class);
            payment.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
            payment.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
            startActivityForResult(payment, PAYPAL_REQUEST_CODE);
        }
        else{
            Toasty.error(getApplicationContext(), "Please Select/Type a valid amount", Toast.LENGTH_SHORT, true).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PAYPAL_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if(confirmation != null){
                    try{
                        System.out.println(confirmation.toJSONObject().toString(4));
                        System.out.println(confirmation.getPayment().toJSONObject().toString(4));
                        JSONObject response = confirmation.toJSONObject().getJSONObject("response");
                        final Donation donation = new Donation();
                        donation.setDonationId(response.getString("id"));
                        donation.setDonationAmount(getSelectedAmount());
                        donation.setDonationDateTime(response.getString("create_time"));
                        donation.setDonationUserId(userId);
                        donation.setDonationEventId(eventId);
                        donation.setDonationState(response.getString("state"));
                        donation.setDonationCurrencyCode("MYR");

                        Variable.DONATION_REF.child(donation.getDonationId()).setValue(donation).addOnCompleteListener(new OnCompleteListener<Void>() {
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

                    } catch (JSONException e){
                        Toasty.error(this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            else if(resultCode == RESULT_CANCELED){
                Toasty.error(this, "Payment has been cancelled", Toast.LENGTH_SHORT).show();
            }else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID){
                Toasty.error(this, "Error Occured", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == PAYPAL_REQUEST_FUTURE_CODE){
            if(requestCode == RESULT_OK){
                PayPalAuthorization authorization = data.getParcelableExtra(PayPalFuturePaymentActivity.EXTRA_RESULT_AUTHORIZATION);

                if(authorization != null){
                    String authorization_code = authorization.getAuthorizationCode();

                    if(authorization_code != null){
                        PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                        if(confirmation != null){
                            try{
                                System.out.println(confirmation.toJSONObject().toString(4));
                                System.out.println(confirmation.getPayment().toJSONObject().toString(4));
                                JSONObject response = confirmation.toJSONObject().getJSONObject("response");
                                final Donation donation = new Donation();
                                donation.setDonationId(response.getString("id"));
                                donation.setDonationAmount(getSelectedAmount());
                                donation.setDonationDateTime(response.getString("create_time"));
                                donation.setDonationUserId(userId);
                                donation.setDonationEventId(eventId);
                                donation.setDonationState(response.getString("state"));
                                donation.setDonationCurrencyCode("MYR");

                                Variable.DONATION_REF.child(donation.getDonationId()).setValue(donation).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                            catch (JSONException e){
                                Toasty.error(this, e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                }
            }
        }

    }
}