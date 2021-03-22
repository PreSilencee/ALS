package com.example.als.ui.more;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.als.LoginActivity;
import com.example.als.R;
import com.example.als.handler.Connectivity;
import com.example.als.object.Variable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.dmoral.toasty.Toasty;

public class ChangePasswordActivity extends AppCompatActivity {

    //tag for console log
    private static final String TAG = "CPActivity";

    //connectivity variable
    private Connectivity device;

    //firebase auth variable
    private FirebaseAuth cAuth;

    //text input layout variable
    private TextInputLayout oldPasswordTIL, newPasswordTIL, confirmPasswordTIL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        //initialize connectivity variable
        device = new Connectivity(ChangePasswordActivity.this);

        //initialize firebaseauth
        cAuth = FirebaseAuth.getInstance();

        //initialize textinputlayout for oldpassword, newpassword, confirmpassword
        oldPasswordTIL = findViewById(R.id.oldPasswordTextInputLayout);
        newPasswordTIL = findViewById(R.id.newPasswordTextInputLayout);
        confirmPasswordTIL = findViewById(R.id.confirmPasswordTextInputLayout);
    }

    //run after the layout is create
    @Override
    protected void onStart() {
        super.onStart();

        //if device no network
        if(!device.haveNetwork()){
            //show error message
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else
        {
            //initialize current user
            FirebaseUser cUser = cAuth.getCurrentUser();

            //if user != null
            if(cUser != null)
            {
                Log.d(TAG, "getCurrentUser: success");
            }
            else
            {
                //if user == null
                Log.d(TAG, "getCurrentUser: failed");

                //show error message
                Toasty.error(getApplicationContext(), "Session is expired. Please relogin.", Toast.LENGTH_SHORT,true).show();

                //intent user to login page to relogin
                Intent i = new Intent(ChangePasswordActivity.this, LoginActivity.class);

                //clear the background task for the application
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!device.haveNetwork()){
            //show error message
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
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

    //button onclick changePassword
    public void changePassword(View view) {

        //check oldpassword, newpassword, confirmpassword in the front end
        if(!validateOldPassword() | !validateNewPassword() | !validateConfirmPassword()){
            return;
        }

        //get user input current password
        final String currentPass = oldPasswordTIL.getEditText().getText().toString().trim();

        //get user input new password
        final String newPass = newPasswordTIL.getEditText().getText().toString().trim();

        //delay 0.3 sec
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //a progress dialog to view progress of create account
                final ProgressDialog progressDialog = new ProgressDialog(ChangePasswordActivity.this);

                //set message for progress dialog
                progressDialog.setMessage("Signing in... " +
                        "Please wait awhile, we are processing the account");

                //show dialog
                progressDialog.show();

                //initialize current user
                final FirebaseUser cUser = cAuth.getCurrentUser();

                //if user != null and user's email != null
                if(cUser != null && cUser.getEmail() != null){

                    //get detail of emailauthcredential for current user
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(cUser.getEmail(), currentPass);

                    //check whether current user enter correct current password
                    cUser.reauthenticate(credential)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //if success
                                    Log.d(TAG, "User re-authenticated: success");

                                    //update the password
                                    cUser.updatePassword(newPass).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //if success to change password
                                            //send message to console
                                            clearField();
                                            Log.d(TAG, "changePassword: success");

                                            //show message to the user
                                            Toasty.success(ChangePasswordActivity.this, "Password Changed Successfully", Toast.LENGTH_SHORT, true).show();

                                            //sign out for current user
                                            cAuth.signOut();

                                            //sign in again using email and newpassword
                                            cAuth.signInWithEmailAndPassword(cUser.getEmail(), newPass)
                                                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                                        @Override
                                                        public void onSuccess(AuthResult authResult) {
                                                            //if success
                                                            //send message to console
                                                            Log.d(TAG, "userRelogin: success");
                                                            progressDialog.dismiss();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            //if failed
                                                            //show error message to console log
                                                            Log.d(TAG, "userRelogin: failed");

                                                            //cancel the progress dialog
                                                            progressDialog.dismiss();
                                                        }
                                                    });

                                        }
                                    })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    //if change password failed
                                                    //show error message to console log
                                                    Log.d(TAG, "changePassword: failed");

                                                    //show error message to the user
                                                    Toasty.error(ChangePasswordActivity.this, "Password Changed Failed", Toast.LENGTH_SHORT, true).show();

                                                    //cancel the progress dialog
                                                    progressDialog.dismiss();
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //if re-authenticated failed
                                    Log.d(TAG, "User re-authenticated: failed");

                                    //show error message for the user (type wrong old password)
                                    Toasty.error(ChangePasswordActivity.this, "Invalid Old Password", Toast.LENGTH_SHORT, true).show();
                                    //cancel the progress dialog
                                    progressDialog.dismiss();
                                }
                            });
                }

            }
        },300);
    }

    //validate old password
    private boolean validateOldPassword() {
        //get password
        String oldPasswordInput = oldPasswordTIL.getEditText().getText().toString().trim();

        //if password == null
        if(oldPasswordInput.isEmpty()){
            oldPasswordTIL.setError("Field can't be empty");
            return false;
        }
        else{
            oldPasswordTIL.setError(null);
            return true;
        }
    }

    //validate new password
    private boolean validateNewPassword() {
        //get password
        String newPasswordInput = newPasswordTIL.getEditText().getText().toString().trim();

        //if password == null
        if(newPasswordInput.isEmpty()){
            newPasswordTIL.setError("Field can't be empty");
            return false;
        }
        //if password less than 6 character or have white space
        else if(!Variable.PASSWORD_PATTERN.matcher(newPasswordInput).matches()){
            newPasswordTIL.setError("The length of password must have at least 6 character");
            return false;
        }
        else{
            newPasswordTIL.setError(null);
            return true;
        }
    }

    //validate confirm password
    private boolean validateConfirmPassword() {
        //get password
        String newPasswordInput = newPasswordTIL.getEditText().getText().toString().trim();
        //get confirm password
        String confirmPasswordInput = confirmPasswordTIL.getEditText().getText().toString().trim();

        //if confirm password == null
        if(confirmPasswordInput.isEmpty()){
            confirmPasswordTIL.setError("Field can't be empty");
            return false;
        }
        // if confirm password != password
        else if(!confirmPasswordInput.equals(newPasswordInput)){
            confirmPasswordTIL.setError("Passwords are not same");
            return false;
        }
        else{
            confirmPasswordTIL.setError(null);
            return true;
        }
    }

    private void clearField(){
        oldPasswordTIL.getEditText().getText().clear();
        newPasswordTIL.getEditText().getText().clear();
        confirmPasswordTIL.getEditText().getText().clear();
    }
}