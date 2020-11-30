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
import com.example.als.MainActivity;
import com.example.als.R;
import com.example.als.SettingsActivity;
import com.example.als.handler.Connectivity;
import com.example.als.object.Payment;
import com.example.als.object.User;
import com.example.als.object.Variable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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
        homeDonateRM100RB, homeDonateOtherAmountRB, homeDonatePayWithStripeRB;

    //string selected amount
    private double selectedAmount;

    //edit text
    private EditText homeDonateOtherAmountET;

    //view
    private View homeDonateSelectPaymentMethodView;

    //textview
    private TextView homeDonateSelectPaymentMethodTextView;

    //cardinput
    private CardInputWidget homeDonateCIW;

    //button
    private Button homeDonateConfirmBtn;

    // 10.0.2.2 is the Android emulator's alias to localhost
    private static final String BACKEND_URL = "http://192.168.0.172:5002/";
    //private static final String BACKEND_URL = "http://10.0.2.2:4242/";
    private OkHttpClient httpClient = new OkHttpClient();
    private String paymentIntentClientSecret;
    private Stripe stripe;
    
    private String userId;
    private String eventId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_donate);

        device = new Connectivity(HomeDonateActivity.this);

        Intent session = getIntent();

        //stripe = new Stripe(getApplicationContext(), Variable.STRIPE_PUBLISHABLE_KEY);

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
                homeDonateSelectPaymentMethodView = findViewById(R.id.homeDonateSelectPaymentMethodView);
                homeDonateSelectPaymentMethodTextView = findViewById(R.id.homeDonateSelectPaymentMethodTextView);
                homeDonatePayWithStripeRB = findViewById(R.id.homeDonatePayWithStripeRadioButton);
                homeDonateCIW = findViewById(R.id.homeDonateCardInputWidget);
                homeDonateConfirmBtn = findViewById(R.id.homeDonateConfirmButton);

                homeDonateRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        if(checkedId == -1){
                            homeDonateSelectPaymentMethodView.setVisibility(View.GONE);
                            homeDonateSelectPaymentMethodTextView.setVisibility(View.GONE);
                            homeDonatePayWithStripeRB.setVisibility(View.GONE);
                            homeDonateCIW.setVisibility(View.GONE);
                            homeDonateConfirmBtn.setVisibility(View.GONE);
                        }
                        else{
                            homeDonateSelectPaymentMethodView.setVisibility(View.VISIBLE);
                            homeDonateSelectPaymentMethodTextView.setVisibility(View.VISIBLE);
                            homeDonatePayWithStripeRB.setVisibility(View.VISIBLE);
                            homeDonatePayWithStripeRB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if(isChecked){
                                        homeDonateCIW.setVisibility(View.VISIBLE);
                                        homeDonateConfirmBtn.setVisibility(View.VISIBLE);

                                        homeDonateConfirmBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                startStripePayment();
                                            }
                                        });

                                    }
                                    else{
                                        homeDonateCIW.setVisibility(View.GONE);
                                        homeDonateConfirmBtn.setVisibility(View.GONE);
                                    }
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

    private void startCheckout() {
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        Payment payment = new Payment();
        payment.setPaymentAmount(selectedAmount);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US);
        Date dateObj = Calendar.getInstance().getTime();
        final String paymentDateTime = simpleDateFormat.format(dateObj);
        payment.setPaymentDateTime(paymentDateTime);
        payment.setPaymentUserId(userId);
        payment.setPaymentEventId(eventId);
        Map<String, Object> paymentValues = payment.toMap();
        String json = new Gson().toJson(paymentValues);
        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(BACKEND_URL + "create-payment-intent")
                .post(body)
                .build();
        httpClient.newCall(request)
                .enqueue(new PayCallback(this));
        // Hook up the pay button to the card widget and stripe instance
        Button payButton = findViewById(R.id.homeDonateConfirmButton);
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardInputWidget cardInputWidget = findViewById(R.id.homeDonateCardInputWidget);
                PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();
                if (params != null) {
                    ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams
                            .createWithPaymentMethodCreateParams(params, paymentIntentClientSecret);
                    stripe.confirmPayment(HomeDonateActivity.this, confirmParams);
                }
            }
        });
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

    public void onDonateAmountSelected(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        //check which radio button was clicked
        switch (view.getId()){
            case R.id.homeDonateRM10RadioButton:
                if(checked){
                    selectedAmount = 10.0;
                    Log.d(TAG, "selectedAmount: "+ selectedAmount);
                }
                break;
            case R.id.homeDonateRM20RadioButton:
                if(checked){
                    selectedAmount = 20.0;
                    Log.d(TAG, "selectedAmount: "+ selectedAmount);
                }
                break;
            case R.id.homeDonateRM50RadioButton:
                if(checked){
                    selectedAmount = 50.0;
                    Log.d(TAG, "selectedAmount: "+ selectedAmount);
                }
                break;
            case R.id.homeDonateRM100RadioButton:
                if(checked){
                    selectedAmount = 100.0;
                    Log.d(TAG, "selectedAmount: "+ selectedAmount);
                }
                break;
            case R.id.homeDonateOtherAmountRadioButton:

                break;
        }
    }

    private void startStripePayment(){
        Payment payment = new Payment();
        payment.setPaymentAmount(selectedAmount);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US);
        Date dateObj = Calendar.getInstance().getTime();
        final String paymentDateTime = simpleDateFormat.format(dateObj);
        payment.setPaymentDateTime(paymentDateTime);
        payment.setPaymentUserId(userId);
        payment.setPaymentEventId(eventId);
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        String json = new Gson().toJson(payment);
//        String json = "{"
//                + ""currency":"usd","
//                + ""items":["
//                + "{"id":"photo_subscription"}"
//                + "]"
//                + "}";
        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(BACKEND_URL + "create-payment-intent")
                .post(body)
                .build();
        httpClient.newCall(request)
                .enqueue(new PayCallback(this));

        AlertDialog.Builder alertDialogBuider = new AlertDialog.Builder(HomeDonateActivity.this);
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

                                PaymentMethodCreateParams params = homeDonateCIW.getPaymentMethodCreateParams();
                                if (params != null) {
                                    ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams
                                            .createWithPaymentMethodCreateParams(params, paymentIntentClientSecret);
                                    stripe.confirmPayment(HomeDonateActivity.this, confirmParams);
                                }

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
        alertDialog.setTitle("Logging Out");
        alertDialog.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Handle the result of stripe.confirmPayment
        stripe.onPaymentResult(requestCode, data, new PaymentResultCallback(this));
    }

    private void onPaymentSuccess(@NonNull final Response response) throws IOException {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> responseMap = gson.fromJson(
                Objects.requireNonNull(response.body()).string(),
                type
        );
        paymentIntentClientSecret = responseMap.get("clientSecret");
    }

    private void displayAlert(@NonNull String title,
                              @Nullable String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message);
        builder.setPositiveButton("Ok", null);
        builder.create().show();
    }

    private static final class PayCallback implements Callback {
        @NonNull private final WeakReference<HomeDonateActivity> activityRef;
        PayCallback(@NonNull HomeDonateActivity activity) {
            activityRef = new WeakReference<>(activity);
        }
        @Override
        public void onFailure(@NonNull Call call, @NonNull final IOException e) {
            final HomeDonateActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(
                            activity, "Error: " + e.toString(), Toast.LENGTH_LONG
                    ).show();
                }
            });
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull final Response response)
                throws IOException {
            final HomeDonateActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }
            if (!response.isSuccessful()) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(
                                activity, "Error: " + response.toString(), Toast.LENGTH_LONG
                        ).show();
                    }
                });
            } else {
                activity.onPaymentSuccess(response);
            }
        }
    }

    private static final class PaymentResultCallback
            implements ApiResultCallback<PaymentIntentResult> {
        @NonNull private final WeakReference<HomeDonateActivity> activityRef;
        PaymentResultCallback(@NonNull HomeDonateActivity activity) {
            activityRef = new WeakReference<>(activity);
        }
        @Override
        public void onSuccess(@NonNull PaymentIntentResult result) {
            final HomeDonateActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }
            PaymentIntent paymentIntent = result.getIntent();
            PaymentIntent.Status status = paymentIntent.getStatus();
            if (status == PaymentIntent.Status.Succeeded) {
                // Payment completed successfully
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                activity.displayAlert(
                        "Payment completed",
                        gson.toJson(paymentIntent)
                );
            } else if (status == PaymentIntent.Status.RequiresPaymentMethod) {
                // Payment failed – allow retrying using a different payment method
                activity.displayAlert(
                        "Payment failed",
                        Objects.requireNonNull(paymentIntent.getLastPaymentError()).getMessage()
                );
            }
        }
        @Override
        public void onError(@NonNull Exception e) {
            final HomeDonateActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }
            // Payment request failed – allow retrying using the same payment method
            activity.displayAlert("Error", e.toString());
        }
    }
}