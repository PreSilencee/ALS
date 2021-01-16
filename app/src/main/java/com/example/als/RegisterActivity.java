package com.example.als;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.als.handler.Connectivity;
import com.example.als.object.Contributor;
import com.example.als.object.Organization;
import com.example.als.object.User;
import com.example.als.object.Variable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class RegisterActivity extends AppCompatActivity {

    //tag for console log
    private static final String TAG = "Register Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

//    public void createAccount(View view) {
//
//        //check email, password and confirm password;
//        if(!validateEmail() | !validatePassword() | !validateConfirmPassword()){
//            return;
//        }
//
//        //get email that input by user
//        final String email = createAccountEmailTIL.getEditText().getText().toString().trim();
//        //get password that input by user
//        final String password = createAccountPasswordTIL.getEditText().getText().toString().trim();
//
//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RegisterActivity.this);
//        alertDialogBuilder.setMessage("Are you sure want use "+email+" to register an account?")
//                .setCancelable(false)
//                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        //delay 0.3 sec
//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                //a progress dialog to view progress of create account
//                                final ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this);
//
//                                //set message for progress dialog
//                                progressDialog.setMessage("Creating Account..." +
//                                        "Please wait awhile, we are processing the account");
//
//                                //show dialog
//                                progressDialog.show();
//
//                                //create an user using email and password
//                                cAuth.createUserWithEmailAndPassword(email, password)
//                                        //if success
//                                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                                            @Override
//                                            public void onSuccess(AuthResult authResult) {
//                                                Log.d(TAG, "createUserWithEmail:success");
//
//                                                //get current user
//                                                final FirebaseUser cUser = cAuth.getCurrentUser();
//
//                                                //if user != null
//                                                if(cUser != null) {
//                                                    //send the email verification to the user's email
//                                                    cUser.sendEmailVerification()
//                                                            //if success
//                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                                @Override
//                                                                public void onSuccess(Void aVoid) {
//                                                                    Log.d(TAG, "sendEmailVerification:success");
//
//                                                                    final User newUser = new User();
//                                                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US);
//                                                                    Date dateObj = Calendar.getInstance().getTime();
//                                                                    final String currentDateTime = simpleDateFormat.format(dateObj);
//
//                                                                    if(registerAsOrganizationCB.isChecked()){
//                                                                        newUser.setRole(Variable.ORGANIZATION);
//                                                                    }
//                                                                    else{
//                                                                        newUser.setRole(Variable.CONTRIBUTOR);
//                                                                    }
//
//                                                                    newUser.setRegisterDateTime(currentDateTime);
//                                                                    newUser.setFirstTimeLoggedIn(true);
//                                                                    newUser.setId(cUser.getUid());
//
//                                                                    FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//                                                                        @Override
//                                                                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                                                                            if(task.isSuccessful()){
//                                                                                String token = task.getResult().getToken();
//                                                                                newUser.setToken(token);
//                                                                            }
//                                                                        }
//                                                                    });
//
//
//                                                                    Variable.USER_REF.child(cUser.getUid()).setValue(newUser)
//                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                                                @Override
//                                                                                public void onSuccess(Void aVoid) {
//                                                                                    //show success message to user
//
//                                                                                    if(newUser.getRole().equals(Variable.CONTRIBUTOR)){
//                                                                                        Contributor contributor = new Contributor();
//                                                                                        contributor.setUserId(cUser.getUid());
//                                                                                        contributor.setEmail(cUser.getEmail());
//                                                                                        Variable.CONTRIBUTOR_REF.child(cUser.getUid()).setValue(contributor).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                                                            @Override
//                                                                                            public void onSuccess(Void aVoid) {
//                                                                                                Toasty.success(getApplicationContext(),"Create Account Successfully. " +
//                                                                                                                "Please check your email for verification",
//                                                                                                        Toast.LENGTH_SHORT, true).show();
//
//                                                                                                //hide the progress dialog
//                                                                                                progressDialog.dismiss();
//
//                                                                                                //clear the input field
//                                                                                                clearField();
//
//                                                                                                finish();
//
//                                                                                                //sign out
//                                                                                                cAuth.signOut();
//                                                                                            }
//                                                                                        })
//                                                                                        .addOnFailureListener(new OnFailureListener() {
//                                                                                            @Override
//                                                                                            public void onFailure(@NonNull Exception e) {
//                                                                                                //show warning message (create account is successful,
//                                                                                                        // but database not include the user's data
//                                                                                                        Toasty.warning(getApplicationContext(),"Something went wrong. " +
//                                                                                                                        "Please do not use the same email to create account again" +
//                                                                                                                        "Please contact Administrator!"
//                                                                                                                , Toast.LENGTH_SHORT, true).show();
//
//                                                                                                //hide the progress dialog
//                                                                                                progressDialog.dismiss();
//
//                                                                                                //clear the input field
//                                                                                                clearField();
//
//                                                                                                //sign out
//                                                                                                cAuth.signOut();
//                                                                                            }
//                                                                                        });
//                                                                                    }
//                                                                                    else{
//                                                                                        Organization organization = new Organization();
//                                                                                        organization.setUserId(cUser.getUid());
//                                                                                        organization.setOrganizationEmail(cUser.getEmail());
//                                                                                        Variable.ORGANIZATION_REF.child(cUser.getUid()).setValue(organization).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                                                            @Override
//                                                                                            public void onSuccess(Void aVoid) {
//                                                                                                Toasty.success(getApplicationContext(),"Create Account Successfully. " +
//                                                                                                                "Please check your email for verification",
//                                                                                                        Toast.LENGTH_SHORT, true).show();
//
//                                                                                                //hide the progress dialog
//                                                                                                progressDialog.dismiss();
//
//                                                                                                //clear the input field
//                                                                                                clearField();
//
//                                                                                                finish();
//
//                                                                                                //sign out
//                                                                                                cAuth.signOut();
//                                                                                            }
//                                                                                        })
//                                                                                        .addOnFailureListener(new OnFailureListener() {
//                                                                                            @Override
//                                                                                            public void onFailure(@NonNull Exception e) {
//                                                                                                //show warning message (create account is successful,
//                                                                                                // but database not include the user's data
//                                                                                                Toasty.warning(getApplicationContext(),"Something went wrong. " +
//                                                                                                                "Please do not use the same email to create account again" +
//                                                                                                                "Please contact Administrator!"
//                                                                                                        , Toast.LENGTH_SHORT, true).show();
//
//                                                                                                //hide the progress dialog
//                                                                                                progressDialog.dismiss();
//
//                                                                                                //clear the input field
//                                                                                                clearField();
//
//                                                                                                //sign out
//                                                                                                cAuth.signOut();
//
//                                                                                            }
//                                                                                        });
//                                                                                    }
//
//                                                                                }
//                                                                            })
//                                                                            .addOnFailureListener(new OnFailureListener() {
//                                                                                @Override
//                                                                                public void onFailure(@NonNull Exception e) {
//                                                                                    //show warning message (create account is successful,
//                                                                                    // but database not include the user's data
//                                                                                    Toasty.warning(getApplicationContext(),"Something went wrong. " +
//                                                                                                    "Please do not use the same email to create account again" +
//                                                                                                    "Please contact Administrator!"
//                                                                                            , Toast.LENGTH_SHORT, true).show();
//
//                                                                                    //hide the progress dialog
//                                                                                    progressDialog.dismiss();
//
//                                                                                    //clear the input field
//                                                                                    clearField();
//
//                                                                                    //sign out
//                                                                                    cAuth.signOut();
//                                                                                }
//                                                                            });
//
//                                                                }
//                                                            })
//                                                            // if failed to send email verification
//                                                            .addOnFailureListener(new OnFailureListener() {
//                                                                @Override
//                                                                public void onFailure(@NonNull Exception e) {
//                                                                    Log.d(TAG, "sendEmailVerification:failed");
//
//                                                                    //show warning message (create account is successful,
//                                                                    // but email verification not send to the user's email)
//                                                                    Toasty.warning(getApplicationContext(),"Something went wrong. " +
//                                                                                    "Please do not use the same email to create account again" +
//                                                                                    "Please contact Administrator!"
//                                                                            , Toast.LENGTH_SHORT, true).show();
//
//                                                                    //hide the progress dialog
//                                                                    progressDialog.dismiss();
//
//                                                                    //clear the field
//                                                                    clearField();
//                                                                }
//                                                            });
//                                                }
//                                            }
//                                        })
//                                        //if failed to create account
//                                        .addOnFailureListener(new OnFailureListener() {
//                                            @Override
//                                            public void onFailure(@NonNull Exception e) {
//                                                Log.d(TAG, "createUserWithEmail:failed");
//
//                                                //show message
//                                                Toasty.error(getApplicationContext(),
//                                                        "Create Account Failed. Please Try Again !"
//                                                        , Toast.LENGTH_SHORT, true).show();
//
//                                                //hide the progress dialog
//                                                progressDialog.dismiss();
//                                            }
//                                        });
//                            }
//                        },200); //millisecond want to delay
//                    }
//                })
//                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                    }
//                });
//
//        AlertDialog alertDialog = alertDialogBuilder.create();
//        alertDialog.setTitle("Confirmation");
//        alertDialog.show();
//    }

    public void registerAsContributor(View view) {
        startActivity(new Intent(this, RegisterContributorActivity.class));
    }

    public void registerAsOrganization(View view) {
        startActivity(new Intent(this, RegisterOrganizationActivity.class));
    }
}