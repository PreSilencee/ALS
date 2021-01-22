package com.example.als;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.als.handler.Connectivity;
import com.example.als.handler.ValidateFunction;
import com.example.als.object.Contributor;
import com.example.als.object.Organization;
import com.example.als.object.User;
import com.example.als.object.Variable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class RegisterContributorActivity extends AppCompatActivity {

    //tag for console log
    private static final String TAG = "Register Contributor";

    //connectivity
    private Connectivity device;

    //firebase authentication variable
    private FirebaseAuth cAuth;

    //textinputlayout for email, password, confirm password
    private TextInputLayout createAccountUsernameTIL, createAccountEmailTIL, createAccountPasswordTIL, createAccountConfirmPasswordTIL;

    CheckBox contributorAgreeCB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_contributor);

        device = new Connectivity(this);

        //get username id from XML
        createAccountUsernameTIL = findViewById(R.id.createAccountUsername);
        //get email id from XML
        createAccountEmailTIL = findViewById(R.id.createAccountEmail);
        //get password id from XML
        createAccountPasswordTIL = findViewById(R.id.createAccountPassword);
        //get confirm password from XML
        createAccountConfirmPasswordTIL = findViewById(R.id.createAccountConfirmPassword);

        contributorAgreeCB = findViewById(R.id.contributorAgreeCheckBox);

        //initialize firebase authentication
        cAuth = FirebaseAuth.getInstance();
    }

    public void createContributorAccount(View view) {

        //check email, password and confirm password;
        if(!ValidateFunction.validateTILField(createAccountUsernameTIL) | !ValidateFunction.validateEmail(createAccountEmailTIL)
                | !ValidateFunction.validatePassword(createAccountPasswordTIL)
                | !ValidateFunction.validateConfirmPassword(createAccountPasswordTIL, createAccountConfirmPasswordTIL)){
            return;
        }

        if(!contributorAgreeCB.isChecked()){
            Toasty.warning(getApplicationContext(), "You must agree our terms & conditions first before signing up.", Toast.LENGTH_LONG).show();
            return;
        }

        if(!device.haveNetwork()){
            Toasty.error(this, device.NetworkError(), Toast.LENGTH_LONG).show();
        }
        else {
            //get username that input by user
            final String username = createAccountUsernameTIL.getEditText().getText().toString().trim();
            //get email that input by user
            final String email = createAccountEmailTIL.getEditText().getText().toString().trim();
            //get password that input by user
            final String password = createAccountPasswordTIL.getEditText().getText().toString().trim();

            //a progress dialog to view progress of create account
            final ProgressDialog progressDialog = new ProgressDialog(RegisterContributorActivity.this);

            //set message for progress dialog
            progressDialog.setMessage("One moment...");

            //show dialog
            progressDialog.show();

            cAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");

                        //get current user
                        final FirebaseUser cUser = cAuth.getCurrentUser();

                        //if user != null
                        if (cUser != null) {
                            //send the email verification to the user's email
                            cUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US);
                                        Date dateObj = Calendar.getInstance().getTime();
                                        final String currentDateTime = simpleDateFormat.format(dateObj);
                                        User newUser = new User();
                                        newUser.setId(cUser.getUid());
                                        newUser.setRole(Variable.CONTRIBUTOR);
                                        newUser.setRegisterDateTime(currentDateTime);
                                        newUser.setFirstTimeLoggedIn(true);

                                        Variable.USER_REF.child(cUser.getUid()).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Contributor contributor = new Contributor();
                                                    contributor.setName(username);
                                                    contributor.setUserId(cUser.getUid());
                                                    contributor.setEmail(cUser.getEmail());

                                                    Variable.CONTRIBUTOR_REF.child(cUser.getUid()).setValue(contributor).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toasty.success(getApplicationContext(), "Create Account Successfully. " +
                                                                                "Please check your email for verification",
                                                                        Toast.LENGTH_LONG, true).show();

                                                                //hide the progress dialog
                                                                progressDialog.dismiss();

                                                                //clear the input field
                                                                clearField();

                                                                finish();

                                                                //sign out
                                                                cAuth.signOut();
                                                            } else {
                                                                //show warning message (create account is successful,
                                                                // but database not include the user's data
                                                                Toasty.warning(getApplicationContext(), "Something went wrong. " +
                                                                                "Please do not use the same email to create account again" +
                                                                                "Please contact Administrator!"
                                                                        , Toast.LENGTH_SHORT).show();

                                                                //hide the progress dialog
                                                                progressDialog.dismiss();

                                                                //clear the input field
                                                                clearField();

                                                                //sign out
                                                                cAuth.signOut();
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    //show warning message (create account is successful,
                                                    // but database not include the user's data
                                                    Toasty.warning(getApplicationContext(), "Something went wrong. " +
                                                                    "Please do not use the same email to create account again" +
                                                                    "Please contact Administrator!"
                                                            , Toast.LENGTH_SHORT).show();

                                                    //hide the progress dialog
                                                    progressDialog.dismiss();

                                                    //clear the input field
                                                    clearField();

                                                    //sign out
                                                    cAuth.signOut();
                                                }
                                            }
                                        });
                                    } else {
                                        //show warning message (create account is successful,
                                        // but database not include the user's data
                                        Toasty.warning(getApplicationContext(), "Something went wrong. " +
                                                        "Please do not use the same email to create account again" +
                                                        "Please contact Administrator!"
                                                , Toast.LENGTH_SHORT).show();

                                        //hide the progress dialog
                                        progressDialog.dismiss();

                                        //clear the input field
                                        clearField();

                                        //sign out
                                        cAuth.signOut();
                                    }
                                }
                            });
                        }
                    } else {
                        Log.d(TAG, "createUserWithEmail:failed");

                        //show message
                        Toasty.error(getApplicationContext(),
                                "Create Account Failed. Please Try Again !"
                                , Toast.LENGTH_SHORT).show();

                        //hide the progress dialog
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }

    //clear fields that input by user
    private void clearField() {
        createAccountUsernameTIL.getEditText().getText().clear();
        createAccountEmailTIL.getEditText().getText().clear();
        createAccountPasswordTIL.getEditText().getText().clear();
        createAccountConfirmPasswordTIL.getEditText().getText().clear();
    }
}