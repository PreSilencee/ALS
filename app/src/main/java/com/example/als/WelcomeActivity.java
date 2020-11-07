package com.example.als;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.als.handler.Connectivity;
import com.example.als.object.User;
import com.example.als.object.Variable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class WelcomeActivity extends AppCompatActivity {

    //tag for console log
    private static final String TAG = "Welcome Activity";
    //private GoogleSignInClient cGoogleSignInClient;

    //connectivity
    private Connectivity device;

    //firebase auth variable
    private FirebaseAuth cAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        device = new Connectivity(WelcomeActivity.this);

        //initialize firebase authentication
        cAuth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            // Check if user is signed in (non-null)
            final FirebaseUser currentUser = cAuth.getCurrentUser();
            if(currentUser != null && currentUser.isEmailVerified())
            {
                //a progress dialog to view progress of create account
                final ProgressDialog progressDialog = new ProgressDialog(WelcomeActivity.this);

                //set message for progress dialog
                progressDialog.setMessage("Signing in... " +
                        "Please wait awhile, we are processing the account");

                //show dialog
                progressDialog.show();

                //delay 0.2 sec
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Variable.USER_REF.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    Log.d(TAG, "findUserInDatabase: success");
                                    User user = snapshot.getValue(User.class);

                                    if(user != null){
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US);
                                        Date dateObj = Calendar.getInstance().getTime();
                                        final String currentDateTime = simpleDateFormat.format(dateObj);
                                        user.setLoggedInDateTime(currentDateTime);

                                        Map<String, Object> userValues = user.toMap();

                                        Variable.USER_REF.child(currentUser.getUid()).setValue(userValues).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "login: success");
                                                //show success message to the user
                                                Toasty.success(WelcomeActivity.this,
                                                        "Login Successfully",
                                                        Toast.LENGTH_SHORT, true).show();

                                                //log into main activity
                                                Intent i = new Intent(WelcomeActivity.this, MainActivity.class);
                                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(i);

                                                //hide the progress dialog
                                                progressDialog.dismiss();

                                                //finish the activity
                                                finish();
                                            }
                                        })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d(TAG, "login: failed");
                                                    }
                                                });
                                    }
                                }
                                else
                                {
                                    Log.d(TAG, "findUserInDatabase: failed");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d(TAG, "databaseerror:" + error.getMessage());
                            }
                        });

                    }
                },200);
            }
            else{
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
                        finish();
                    }
                },300);

            }
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!device.haveNetwork())
        {
            Toasty.error(getApplicationContext(),device.NetworkError(),Toast.LENGTH_SHORT,true).show();
        }
    }

    @Override
    protected void onStop() {
        if(!device.haveNetwork())
        {
            Toasty.error(getApplicationContext(),device.NetworkError(),Toast.LENGTH_SHORT,true).show();
        }
        super.onStop();
    }
}