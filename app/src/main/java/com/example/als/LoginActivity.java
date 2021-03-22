package com.example.als;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.als.handler.Connectivity;
import com.example.als.handler.ValidateFunction;
import com.example.als.object.Contributor;
import com.example.als.object.User;
import com.example.als.object.Variable;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonIOException;
import com.paypal.android.sdk.payments.PayPalPayment;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {

    //tag for console log
    private static final String TAG = "Login Activity";
    private static final int RC_SIGN_IN = 9001;

    //google signIn
    private GoogleSignInClient cGoogleSignInClient;

    //connectivity
    private Connectivity device;

    //firebase auth variable
    private FirebaseAuth cAuth;

    //textinputlayout for loginemail, loginpassword
    private TextInputLayout inputLoginEmail, inputLoginPassword;

    Button signInWithEmailBtn, signInWithGoogleBtn, signInEmailPasswordBtn;
    CardView expandedCardView;

    CallbackManager callbackManager;
    LoginButton fbLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        device = new Connectivity(LoginActivity.this);

        signInWithEmailBtn = findViewById(R.id.signInWithEmailPasswordButton);
        signInEmailPasswordBtn = findViewById(R.id.signInEmailPassButton);

        expandedCardView = findViewById(R.id.expandedView);
        inputLoginEmail = findViewById(R.id.loggedInEmailTextInputLayout);
        inputLoginPassword = findViewById(R.id.loggedInPasswordTextInputLayout);

        signInWithGoogleBtn = findViewById(R.id.signInWithGoogleButton);
        fbLoginBtn = findViewById(R.id.signInWithFacebookButton);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        fbLoginBtn.setReadPermissions("email", "public_profile");

        fbLoginBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                authWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                Toasty.warning(getApplicationContext(), "Authentication With Facebook has been cancel", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError: "+ error.getMessage());
                Toasty.error(getApplicationContext(), "Authentication With Facebook Failed. Please Try Again", Toast.LENGTH_LONG).show();
            }
        });

        //initialize firebase authentication
        cAuth = FirebaseAuth.getInstance();

        signInWithEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(expandedCardView.getVisibility() == View.GONE){
                    expandedCardView.setVisibility(View.VISIBLE);
                    signInWithEmailBtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.corner_button_main_filled));
                    signInWithEmailBtn.setTextColor(getResources().getColor(R.color.colorWhite));
                }
                else if(expandedCardView.getVisibility() == View.VISIBLE){
                    expandedCardView.setVisibility(View.GONE);
                    signInWithEmailBtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.corner_button_main));
                    signInWithEmailBtn.setTextColor(getResources().getColor(R.color.colorAccent));
                }
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        cGoogleSignInClient= GoogleSignIn.getClient(this, gso);

        signInWithGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent googleSignInIntent = cGoogleSignInClient.getSignInIntent();
                startActivityForResult(googleSignInIntent, RC_SIGN_IN);
            }
        });

        inputLoginEmail.getEditText().addTextChangedListener(loginTextWatcher);
        inputLoginPassword.getEditText().addTextChangedListener(loginTextWatcher);


    }

    private final TextWatcher loginTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String inputEmail = inputLoginEmail.getEditText().getText().toString().trim();
            String inputPassword = inputLoginPassword.getEditText().getText().toString().trim();

            signInEmailPasswordBtn.setEnabled(!inputEmail.isEmpty() && !inputPassword.isEmpty());

            if(!signInEmailPasswordBtn.isEnabled()){
                signInEmailPasswordBtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.corner_button_gray_filled));
            }
            else{
                signInEmailPasswordBtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.corner_button_main_filled));
                signInEmailPasswordBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        signInWithEmailPassword();
                    }
                });
            }

        }

        @Override
        public void afterTextChanged(Editable s) {
            //
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toasty.error(getApplicationContext(),device.NetworkError(),Toast.LENGTH_SHORT,true).show();
        }
    }

    //login button onclick
    public void signInWithEmailPassword() {
        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_LONG).show();
        }
        else {

            //a progress dialog to view progress of create account
            final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);

            //set message for progress dialog
            progressDialog.setMessage("Signing in...");

            //show dialog
            progressDialog.show();

            //get input email
            final String inputEmail = inputLoginEmail.getEditText().getText().toString().trim();

            //get input password
            final String inputPassword = inputLoginPassword.getEditText().getText().toString().trim();

            //sign in with email and password
            cAuth.signInWithEmailAndPassword(inputEmail, inputPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "loginWithEmailPassword: success");
                        //get current user
                        final FirebaseUser cUser = cAuth.getCurrentUser();

                        //if user not null
                        assert cUser != null;

                        //if user's email has been verified
                        if (cUser.isEmailVerified()) {
                            Log.d(TAG, "isEmailVerified:true");

                            Variable.USER_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        Log.d(TAG, "findUserInDatabase: success");
                                        User user = snapshot.getValue(User.class);

                                        if (user != null) {
                                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US);
                                            Date dateObj = Calendar.getInstance().getTime();
                                            final String currentDateTime = simpleDateFormat.format(dateObj);
                                            user.setLoggedInDateTime(currentDateTime);

                                            Map<String, Object> userValues = user.userMap();

                                            Variable.USER_REF.child(cUser.getUid()).setValue(userValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Log.d(TAG, "login: success");
                                                        //show success message to the user
                                                        Toasty.success(LoginActivity.this,
                                                                "Login Successfully",
                                                                Toast.LENGTH_LONG).show();

                                                        //log into main activity
                                                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(i);

                                                        //hide the progress dialog
                                                        progressDialog.dismiss();

                                                        //finish the activity
                                                        finish();
                                                    }
                                                    else{
                                                        Log.d(TAG, "login: failed");
                                                    }
                                                }
                                            });
                                        }
                                    } else {
                                        Log.d(TAG, "findUserInDatabase: failed");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d(TAG, "databaseerror:" + error.getMessage());
                                }
                            });

                        } else {
                            Log.d(TAG, "isEmailVerified:false");

                            //show error message
                            Toasty.error(LoginActivity.this,
                                    "Login Failed ! Please verify your email first",
                                    Toast.LENGTH_LONG).show();

                            //hide the progress dialog
                            progressDialog.dismiss();
                        }
                    }
                    else{
                        Log.d(TAG, "loginWithEmailPassword: failed");

                        //show error message
                        Toasty.error(getApplicationContext(),
                                "Login Failed ! Check your email and password.",
                                Toast.LENGTH_LONG).show();

                        //hide the progress dialog
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }

    public void forgotPassword(View view) {
        startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
    }

    public void navigateToAboutUsPage(View view) {
        startActivity(new Intent(LoginActivity.this, AboutUsActivity.class));
    }

    public void navigateToRegisterPage(View view) {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try{
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle: "+account.getId());
                authWithGoogle(account.getIdToken());
            } catch (ApiException e){
                Log.w(TAG, "Google Sign in Falied ", e);
            }
        }

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void authWithGoogle(String idToken){
        //a progress dialog to view progress of create account
        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);

        //set message for progress dialog
        progressDialog.setMessage("Authenticating With Google...");

        //show dialog
        progressDialog.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        cAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    final FirebaseUser cUser = cAuth.getCurrentUser();

                    Variable.USER_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US);
                                Date dateObj = Calendar.getInstance().getTime();
                                final String currentDateTime = simpleDateFormat.format(dateObj);
                                User user = snapshot.getValue(User.class);
                                user.setLoggedInDateTime(currentDateTime);

                                Map<String, Object> userValues = user.userMap();

                                Variable.USER_REF.child(cUser.getUid()).setValue(userValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Variable.CONTRIBUTOR_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if(snapshot.exists()){
                                                        Contributor contributor = snapshot.getValue(Contributor.class);
                                                        contributor.setEmail(cUser.getEmail());
                                                        contributor.setUserId(cUser.getUid());
                                                        contributor.setName(cUser.getDisplayName());
                                                        contributor.setProfileImageUrl(cUser.getPhotoUrl().toString());
                                                        contributor.setPhone(cUser.getPhoneNumber());

                                                        Map<String, Object> contributorValues = contributor.contributorMap();

                                                        Variable.CONTRIBUTOR_REF.child(cUser.getUid()).updateChildren(contributorValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                    startActivity(i);

                                                                    //hide the progress dialog
                                                                    progressDialog.dismiss();
                                                                }
                                                                else{
                                                                    Toasty.warning(getApplicationContext(), "Something went wrong. " +
                                                                                    "Please Try Again"
                                                                            , Toast.LENGTH_SHORT).show();

                                                                    //hide the progress dialog
                                                                    progressDialog.dismiss();
                                                                }
                                                            }
                                                        });
                                                    }
                                                    else{
                                                        //show warning message (create account is successful,
                                                        // but database not include the user's data
                                                        Toasty.warning(getApplicationContext(), "Something went wrong. " +
                                                                        "Please Try Again"
                                                                , Toast.LENGTH_SHORT).show();

                                                        //hide the progress dialog
                                                        progressDialog.dismiss();
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    Log.d(TAG, "databaseError: " + error.getMessage());
                                                }
                                            });
                                        }
                                        else{
                                            //show warning message (create account is successful,
                                            // but database not include the user's data
                                            Toasty.warning(getApplicationContext(), "Something went wrong. " +
                                                            "Please Try Again"
                                                    , Toast.LENGTH_SHORT).show();

                                            //hide the progress dialog
                                            progressDialog.dismiss();
                                        }
                                    }
                                });
                            }
                            //set new contributor
                            else{

                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US);
                                Date dateObj = Calendar.getInstance().getTime();
                                final String currentDateTime = simpleDateFormat.format(dateObj);
                                User newUser = new User();
                                newUser.setId(cUser.getUid());
                                newUser.setRole(Variable.CONTRIBUTOR);
                                newUser.setRegisterDateTime(currentDateTime);
                                newUser.setFirstTimeLoggedIn(false);
                                newUser.setLoggedInDateTime(currentDateTime);

                                Variable.USER_REF.child(cUser.getUid()).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Contributor contributor = new Contributor();

                                            contributor.setName(cUser.getDisplayName());
                                            contributor.setUserId(cUser.getUid());
                                            contributor.setEmail(cUser.getEmail());
                                            contributor.setProfileImageUrl(cUser.getPhotoUrl().toString());
                                            contributor.setPhone(cUser.getPhoneNumber());

                                            Variable.CONTRIBUTOR_REF.child(cUser.getUid()).setValue(contributor).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(i);

                                                        //hide the progress dialog
                                                        progressDialog.dismiss();
                                                    }
                                                    else{
                                                        //show warning message (create account is successful,
                                                        // but database not include the user's data
                                                        Toasty.warning(getApplicationContext(), "Something went wrong. " +
                                                                        "Please Try Again"
                                                                , Toast.LENGTH_SHORT).show();

                                                        //hide the progress dialog
                                                        progressDialog.dismiss();
                                                    }
                                                }
                                            });

                                        }
                                        else{
                                            //show warning message (create account is successful,
                                            // but database not include the user's data
                                            Toasty.warning(getApplicationContext(), "Something went wrong. " +
                                                            "Please Try Again"
                                                    , Toast.LENGTH_SHORT).show();

                                            //hide the progress dialog
                                            progressDialog.dismiss();
                                        }

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d(TAG, "databaseError: " + error.getMessage());
                        }
                    });
                }
                else{
                    Log.d(TAG, "loginWithGoogle: failed");

                    //show error message
                    Toasty.error(getApplicationContext(),
                            "Authentication with google failed. Please Try Again",
                            Toast.LENGTH_LONG).show();

                    //hide the progress dialog
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void authWithFacebook(final AccessToken token){

        //a progress dialog to view progress of create account
        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);

        //set message for progress dialog
        progressDialog.setMessage("Authenticating With Facebook...");

        //show dialog
        progressDialog.show();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        cAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    final FirebaseUser cUser = cAuth.getCurrentUser();
                    Variable.USER_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US);
                                Date dateObj = Calendar.getInstance().getTime();
                                final String currentDateTime = simpleDateFormat.format(dateObj);
                                User user = snapshot.getValue(User.class);
                                user.setLoggedInDateTime(currentDateTime);

                                Map<String, Object> userValues = user.userMap();

                                Variable.USER_REF.child(cUser.getUid()).updateChildren(userValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Variable.CONTRIBUTOR_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if(snapshot.exists()){
                                                        Contributor contributor = snapshot.getValue(Contributor.class);
                                                        contributor.setEmail(cUser.getEmail());
                                                        contributor.setUserId(cUser.getUid());
                                                        contributor.setName(cUser.getDisplayName());
                                                        contributor.setProfileImageUrl(cUser.getPhotoUrl().toString());
                                                        contributor.setPhone(cUser.getPhoneNumber());

                                                        Map<String, Object> contributorValues = contributor.contributorMap();

                                                        Variable.CONTRIBUTOR_REF.child(cUser.getUid()).updateChildren(contributorValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                    startActivity(i);

                                                                    //hide the progress dialog
                                                                    progressDialog.dismiss();
                                                                }
                                                                else{
                                                                    Toasty.warning(getApplicationContext(), "Something went wrong. " +
                                                                                    "Please Try Again"
                                                                            , Toast.LENGTH_SHORT).show();

                                                                    //hide the progress dialog
                                                                    progressDialog.dismiss();
                                                                }
                                                            }
                                                        });
                                                    }
                                                    else{
                                                        Toasty.warning(getApplicationContext(), "Something went wrong. " +
                                                                        "Please Try Again"
                                                                , Toast.LENGTH_SHORT).show();

                                                        //hide the progress dialog
                                                        progressDialog.dismiss();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    Log.d(TAG, "databaseError: "+error.getMessage());
                                                }
                                            });

                                        }
                                        else{
                                            //show warning message (create account is successful,
                                            // but database not include the user's data
                                            Toasty.warning(getApplicationContext(), "Something went wrong. " +
                                                            "Please Try Again"
                                                    , Toast.LENGTH_SHORT).show();

                                            //hide the progress dialog
                                            progressDialog.dismiss();
                                        }
                                    }
                                });
                            }
                            else{
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US);
                                Date dateObj = Calendar.getInstance().getTime();
                                final String currentDateTime = simpleDateFormat.format(dateObj);
                                User newUser = new User();
                                newUser.setId(cUser.getUid());
                                newUser.setRole(Variable.CONTRIBUTOR);
                                newUser.setRegisterDateTime(currentDateTime);
                                newUser.setFirstTimeLoggedIn(false);
                                newUser.setLoggedInDateTime(currentDateTime);

                                Variable.USER_REF.child(cUser.getUid()).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                            Contributor contributor = new Contributor();

                                            contributor.setName(cUser.getDisplayName());
                                            contributor.setUserId(cUser.getUid());
                                            contributor.setEmail(cUser.getEmail());
                                            contributor.setProfileImageUrl(cUser.getPhotoUrl().toString());
                                            contributor.setPhone(cUser.getPhoneNumber());

                                            Variable.CONTRIBUTOR_REF.child(cUser.getUid()).setValue(contributor).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(i);

                                                        //hide the progress dialog
                                                        progressDialog.dismiss();
                                                    }
                                                    else{
                                                        //show warning message (create account is successful,
                                                        // but database not include the user's data
                                                        Toasty.warning(getApplicationContext(), "Something went wrong. " +
                                                                        "Please Try Again"
                                                                , Toast.LENGTH_SHORT).show();

                                                        //hide the progress dialog
                                                        progressDialog.dismiss();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d(TAG, "databaseError: "+error.getMessage());
                        }
                    });

                }
                else{
                    //show warning message (create account is successful,
                    // but database not include the user's data
                    Toasty.warning(getApplicationContext(), "Something went wrong. " +
                                    "Please Try Again"
                            , Toast.LENGTH_SHORT).show();

                    //hide the progress dialog
                    progressDialog.dismiss();
                }
            }
        });
    }


}