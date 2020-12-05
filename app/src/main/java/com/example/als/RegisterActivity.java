package com.example.als;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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

    //connectivity
    private Connectivity device;

    //firebase authentication variable
    private FirebaseAuth cAuth;

    //textinputlayout for email, password, confirm password
    private TextInputLayout createAccountEmailTIL, createAccountPasswordTIL, createAccountConfirmPasswordTIL;

    //checkbox for register as organization
    private CheckBox registerAsOrganizationCB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        device = new Connectivity(RegisterActivity.this);

        //get email id from XML
        createAccountEmailTIL = findViewById(R.id.createAccountEmail);
        //get password id from XML
        createAccountPasswordTIL = findViewById(R.id.createAccountPassword);
        //get confirm password from XML
        createAccountConfirmPasswordTIL = findViewById(R.id.createAccountConfirmPassword);
        //get checkbox from XML
        registerAsOrganizationCB = findViewById(R.id.registerAsOrganizationCheckBox);

        //initialize firebase auth instance;
        cAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!device.haveNetwork())
        {
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
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

    //validate email
    private boolean validateEmail(){
        //get email
        String emailInput = createAccountEmailTIL.getEditText().getText().toString().trim();

        //if email == null
        if(emailInput.isEmpty())
        {
            createAccountEmailTIL.getEditText().setError("Field can't be empty");
            return false;
        }
        //if email address not an valid email address
        else if(!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()){
            createAccountEmailTIL.getEditText().setError("Please enter a valid email address");
            return false;
        }
        else{
            return true;
        }
    }

    //validate password
    private boolean validatePassword() {
        //get password
        String passwordInput = createAccountPasswordTIL.getEditText().getText().toString().trim();

        //if password == null
        if(passwordInput.isEmpty()){
            createAccountPasswordTIL.getEditText().setError("Field can't be empty");
            return false;
        }
        //if password less than 6 character or have white space
        else if(!Variable.PASSWORD_PATTERN.matcher(passwordInput).matches()){
            createAccountPasswordTIL.getEditText().setError("The length of password must have at least 6 character");
            return false;
        }
        else{
            return true;
        }
    }

    //validate confirm password
    private boolean validateConfirmPassword() {
        //get password
        String passwordInput = createAccountPasswordTIL.getEditText().getText().toString().trim();
        //get confirm password
        String confirmPasswordInput = createAccountConfirmPasswordTIL.getEditText().getText().toString().trim();

        //if confirm password == null
        if(confirmPasswordInput.isEmpty()){
            createAccountConfirmPasswordTIL.getEditText().setError("Field can't be empty");
            return false;
        }
        // if confirm password != password
        else if(!confirmPasswordInput.equals(passwordInput)){
            createAccountConfirmPasswordTIL.getEditText().setError("Passwords are not same");
            return false;
        }
        else{
            return true;
        }
    }
    //clear fields that input by user
    private void clearField() {
        createAccountEmailTIL.getEditText().getText().clear();
        createAccountPasswordTIL.getEditText().getText().clear();
        createAccountConfirmPasswordTIL.getEditText().getText().clear();
        registerAsOrganizationCB.setChecked(false);
    }

    public void createAccount(View view) {

        //check email, password and confirm password;
        if(!validateEmail() | !validatePassword() | !validateConfirmPassword()){
            return;
        }

        //get email that input by user
        final String email = createAccountEmailTIL.getEditText().getText().toString().trim();
        //get password that input by user
        final String password = createAccountPasswordTIL.getEditText().getText().toString().trim();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RegisterActivity.this);
        alertDialogBuilder.setMessage("Are you sure want use "+email+" to register an account?")
                .setCancelable(false)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //delay 0.3 sec
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                //a progress dialog to view progress of create account
                                final ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this);

                                //set message for progress dialog
                                progressDialog.setMessage("Creating Account..." +
                                        "Please wait awhile, we are processing the account");

                                //show dialog
                                progressDialog.show();

                                //create an user using email and password
                                cAuth.createUserWithEmailAndPassword(email, password)
                                        //if success
                                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                            @Override
                                            public void onSuccess(AuthResult authResult) {
                                                Log.d(TAG, "createUserWithEmail:success");

                                                //get current user
                                                final FirebaseUser cUser = cAuth.getCurrentUser();

                                                //if user != null
                                                if(cUser != null) {
                                                    //send the email verification to the user's email
                                                    cUser.sendEmailVerification()
                                                            //if success
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Log.d(TAG, "sendEmailVerification:success");

                                                                    final User newUser = new User();
                                                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US);
                                                                    Date dateObj = Calendar.getInstance().getTime();
                                                                    final String currentDateTime = simpleDateFormat.format(dateObj);

                                                                    if(registerAsOrganizationCB.isChecked()){
                                                                        newUser.setRole(Variable.ORGANIZATION);
                                                                    }
                                                                    else{
                                                                        newUser.setRole(Variable.CONTRIBUTOR);
                                                                    }

                                                                    newUser.setRegisterDateTime(currentDateTime);
                                                                    newUser.setFirstTimeLoggedIn(true);
                                                                    newUser.setId(cUser.getUid());

                                                                    FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                                                            if(task.isSuccessful()){
                                                                                String token = task.getResult().getToken();
                                                                                newUser.setToken(token);
                                                                            }
                                                                        }
                                                                    });


                                                                    Variable.USER_REF.child(cUser.getUid()).setValue(newUser)
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    //show success message to user

                                                                                    if(newUser.getRole().equals(Variable.CONTRIBUTOR)){
                                                                                        Contributor contributor = new Contributor();
                                                                                        contributor.setEmail(cUser.getEmail());
                                                                                        Variable.CONTRIBUTOR_REF.child(cUser.getUid()).setValue(contributor).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override
                                                                                            public void onSuccess(Void aVoid) {
                                                                                                Toasty.success(getApplicationContext(),"Create Account Successfully. " +
                                                                                                                "Please check your email for verification",
                                                                                                        Toast.LENGTH_SHORT, true).show();

                                                                                                //hide the progress dialog
                                                                                                progressDialog.dismiss();

                                                                                                //clear the input field
                                                                                                clearField();

                                                                                                finish();

                                                                                                //sign out
                                                                                                cAuth.signOut();
                                                                                            }
                                                                                        })
                                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                                            @Override
                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                //show warning message (create account is successful,
                                                                                                        // but database not include the user's data
                                                                                                        Toasty.warning(getApplicationContext(),"Something went wrong. " +
                                                                                                                        "Please do not use the same email to create account again" +
                                                                                                                        "Please contact Administrator!"
                                                                                                                , Toast.LENGTH_SHORT, true).show();

                                                                                                //hide the progress dialog
                                                                                                progressDialog.dismiss();

                                                                                                //clear the input field
                                                                                                clearField();

                                                                                                //sign out
                                                                                                cAuth.signOut();
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                    else{
                                                                                        Organization organization = new Organization();
                                                                                        organization.setOrganizationEmail(cUser.getEmail());
                                                                                        Variable.ORGANIZATION_REF.child(cUser.getUid()).setValue(organization).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override
                                                                                            public void onSuccess(Void aVoid) {
                                                                                                Toasty.success(getApplicationContext(),"Create Account Successfully. " +
                                                                                                                "Please check your email for verification",
                                                                                                        Toast.LENGTH_SHORT, true).show();

                                                                                                //hide the progress dialog
                                                                                                progressDialog.dismiss();

                                                                                                //clear the input field
                                                                                                clearField();

                                                                                                finish();

                                                                                                //sign out
                                                                                                cAuth.signOut();
                                                                                            }
                                                                                        })
                                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                                            @Override
                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                //show warning message (create account is successful,
                                                                                                // but database not include the user's data
                                                                                                Toasty.warning(getApplicationContext(),"Something went wrong. " +
                                                                                                                "Please do not use the same email to create account again" +
                                                                                                                "Please contact Administrator!"
                                                                                                        , Toast.LENGTH_SHORT, true).show();

                                                                                                //hide the progress dialog
                                                                                                progressDialog.dismiss();

                                                                                                //clear the input field
                                                                                                clearField();

                                                                                                //sign out
                                                                                                cAuth.signOut();

                                                                                            }
                                                                                        });
                                                                                    }

                                                                                }
                                                                            })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    //show warning message (create account is successful,
                                                                                    // but database not include the user's data
                                                                                    Toasty.warning(getApplicationContext(),"Something went wrong. " +
                                                                                                    "Please do not use the same email to create account again" +
                                                                                                    "Please contact Administrator!"
                                                                                            , Toast.LENGTH_SHORT, true).show();

                                                                                    //hide the progress dialog
                                                                                    progressDialog.dismiss();

                                                                                    //clear the input field
                                                                                    clearField();

                                                                                    //sign out
                                                                                    cAuth.signOut();
                                                                                }
                                                                            });

                                                                }
                                                            })
                                                            // if failed to send email verification
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.d(TAG, "sendEmailVerification:failed");

                                                                    //show warning message (create account is successful,
                                                                    // but email verification not send to the user's email)
                                                                    Toasty.warning(getApplicationContext(),"Something went wrong. " +
                                                                                    "Please do not use the same email to create account again" +
                                                                                    "Please contact Administrator!"
                                                                            , Toast.LENGTH_SHORT, true).show();

                                                                    //hide the progress dialog
                                                                    progressDialog.dismiss();

                                                                    //clear the field
                                                                    clearField();
                                                                }
                                                            });
                                                }
                                            }
                                        })
                                        //if failed to create account
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "createUserWithEmail:failed");

                                                //show message
                                                Toasty.error(getApplicationContext(),
                                                        "Create Account Failed. Please Try Again !"
                                                        , Toast.LENGTH_SHORT, true).show();

                                                //hide the progress dialog
                                                progressDialog.dismiss();
                                            }
                                        });
                            }
                        },200); //millisecond want to delay
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setTitle("Confirmation");
        alertDialog.show();
    }

}