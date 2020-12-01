package com.example.als;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.als.handler.Connectivity;
import com.example.als.object.User;
import com.example.als.object.Variable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
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

public class LoginActivity extends AppCompatActivity {

    //tag for console log
    private static final String TAG = "Login Activity";
    //private GoogleSignInClient cGoogleSignInClient;

    //connectivity
    private Connectivity device;

    //firebase auth variable
    private FirebaseAuth cAuth;

    //textinputlayout for loginemail, loginpassword
    private TextInputLayout inputLoginEmail, inputLoginPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        device = new Connectivity(LoginActivity.this);


        inputLoginEmail = findViewById(R.id.loggedInEmailTextInputLayout);
        inputLoginPassword = findViewById(R.id.loggedInPasswordTextInputLayout);

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
                final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);

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
                            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    Log.d(TAG, "findUserInDatabase: success");
                                    User user = snapshot.getValue(User.class);

                                    if(user != null){
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US);
                                        Date dateObj = Calendar.getInstance().getTime();
                                        final String currentDateTime = simpleDateFormat.format(dateObj);
                                        user.setLoggedInDateTime(currentDateTime);

                                        Map<String, Object> userValues = user.userMap();

                                        Variable.USER_REF.child(currentUser.getUid()).setValue(userValues).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "login: success");
                                                //show success message to the user
                                                Toasty.success(LoginActivity.this,
                                                        "Login Successfully",
                                                        Toast.LENGTH_SHORT, true).show();

                                                //log into main activity
                                                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                                i.putExtra(Variable.USER_SESSION_ID, snapshot.getKey());
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

    //validate log in email
    private boolean validateLoginEmail(){
        //get email
        String emailInput = inputLoginEmail.getEditText().getText().toString().trim();

        //if email == null
        if(emailInput.isEmpty())
        {
            inputLoginEmail.setError("Field can't be empty");
            return false;
        }
        //if email address not an valid email address
        else if(!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()){
            inputLoginEmail.setError("Please enter a valid email address");
            return false;
        }
        else{
            inputLoginEmail.setError(null);
            return true;
        }
    }

    //validate log in password
    private boolean validateLoginPassword() {
        //get password
        String passwordInput = inputLoginPassword.getEditText().getText().toString().trim();

        //if password == null
        if(passwordInput.isEmpty()){
            inputLoginPassword.setError("Field can't be empty");
            return false;
        }
        else{
            inputLoginPassword.setError(null);
            return true;
        }
    }

    //login button onclick
    public void signInWithEmailPassword(View view) {
        if(!validateLoginEmail() | !validateLoginPassword()){
            return;
        }

        //get input email
        final String inputEmail = inputLoginEmail.getEditText().getText().toString().trim();

        //get input password
        final String inputPassword = inputLoginPassword.getEditText().getText().toString().trim();

        //delay 0.3 sec
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //a progress dialog to view progress of create account
                final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);

                //set message for progress dialog
                progressDialog.setMessage("Signing in... " +
                        "Please wait awhile, we are processing the account");

                //show dialog
                progressDialog.show();

                //sign in with email and password
                cAuth.signInWithEmailAndPassword(inputEmail, inputPassword)
                        //if success
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Log.d(TAG, "loginWithEmailPassword: success");
                                //get current user
                                final FirebaseUser cUser = cAuth.getCurrentUser();

                                //if user not null
                                assert cUser != null;

                                //if user's email has been verified
                                if(cUser.isEmailVerified()){
                                    Log.d(TAG, "isEmailVerified:true");

                                    Variable.USER_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
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

                                                    Map<String, Object> userValues = user.userMap();

                                                    Variable.USER_REF.child(cUser.getUid()).setValue(userValues).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d(TAG, "login: success");
                                                            //show success message to the user
                                                            Toasty.success(LoginActivity.this,
                                                                    "Login Successfully",
                                                                    Toast.LENGTH_SHORT, true).show();

                                                            //log into main activity
                                                            Intent i = new Intent(LoginActivity.this, MainActivity.class);
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
                                else{
                                    Log.d(TAG, "isEmailVerified:false");

                                    //show error message
                                    Toasty.error(LoginActivity.this,
                                            "Login Failed ! Please verify your email first",
                                            Toast.LENGTH_SHORT, true).show();

                                    //hide the progress dialog
                                    progressDialog.dismiss();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "loginWithEmailPassword: failed");

                                //show error message
                                Toasty.error(LoginActivity.this,
                                        "Login Failed ! Check your email and password.",
                                        Toast.LENGTH_SHORT,
                                        true).show();

                                //hide the progress dialog
                                progressDialog.dismiss();
                            }
                        });
            }
        },300);
    }

    public void forgotPassword(View view) {
        startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
    }

    public void googleSignIn(View view) {
    }

    public void navigateToAboutUsPage(View view) {
        startActivity(new Intent(LoginActivity.this, AboutUsActivity.class));
    }

    public void navigateToRegisterPage(View view) {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }
}