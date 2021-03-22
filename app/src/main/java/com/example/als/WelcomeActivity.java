package com.example.als;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.als.handler.Connectivity;
import com.example.als.object.Contributor;
import com.example.als.object.User;
import com.example.als.object.Variable;
import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
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

    //connectivity
    private Connectivity device;

    //firebase auth variable
    private FirebaseAuth cAuth;

    ImageView logoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        //initialize connectivity
        device = new Connectivity(WelcomeActivity.this);

        //initialize firebase auth
        cAuth = FirebaseAuth.getInstance();

        logoImageView = findViewById(R.id.logoImageView);



    }

    @Override
    protected void onStart() {
        super.onStart();
        //check connectivity
        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            // Check if user is signed in (non-null)
            final FirebaseUser currentUser = cAuth.getCurrentUser();
            if(currentUser != null)
            {
                final AccessToken accessToken = AccessToken.getCurrentAccessToken();
                final GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                //a progress dialog to view progress of create account
                final ProgressDialog progressDialog = new ProgressDialog(WelcomeActivity.this);

                //set message for progress dialog
                progressDialog.setMessage("Checking Authorization...");

                //show dialog
                progressDialog.show();

                Variable.USER_REF.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Log.d(TAG, "findUserInDatabase: success");
                            User user = snapshot.getValue(User.class);

                            if(user != null){

                                //create date
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US);
                                Date dateObj = Calendar.getInstance().getTime();
                                final String currentDateTime = simpleDateFormat.format(dateObj);
                                user.setLoggedInDateTime(currentDateTime);

                                Map<String, Object> userValues = user.userMap();

                                Variable.USER_REF.child(currentUser.getUid()).updateChildren(userValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            if(accessToken != null || googleSignInAccount != null){
                                                Variable.CONTRIBUTOR_REF.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if(snapshot.exists()){
                                                            Contributor contributor = snapshot.getValue(Contributor.class);

                                                            if(contributor != null){
                                                                contributor.setName(currentUser.getDisplayName());
                                                                contributor.setUserId(currentUser.getUid());
                                                                contributor.setEmail(currentUser.getEmail());
                                                                contributor.setProfileImageUrl(currentUser.getPhotoUrl().toString());
                                                                contributor.setPhone(currentUser.getPhoneNumber());

                                                                Map<String, Object> contributorValues = contributor.contributorMap();

                                                                Variable.CONTRIBUTOR_REF.child(currentUser.getUid()).updateChildren(contributorValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){
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
                                                                        else{
                                                                            Toasty.error(WelcomeActivity.this,
                                                                                    "Something went wrong. Please proceed login session",
                                                                                    Toast.LENGTH_SHORT, true).show();
                                                                            progressDialog.dismiss();
                                                                            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                                                                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(WelcomeActivity.this, logoImageView, ViewCompat.getTransitionName(logoImageView));
                                                                            startActivity(intent, options.toBundle());
                                                                            finish();
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        Log.d(TAG, "databaseerror:" + error.getMessage());
                                                    }
                                                });
                                            }
                                            else{
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
                                        }
                                        else{
                                            Log.d(TAG, "login: failed");
                                        }
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
            else{
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, 300);

            }
        }
    }
}