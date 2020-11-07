package com.example.als;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.als.handler.Connectivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import es.dmoral.toasty.Toasty;

public class ForgotPasswordActivity extends AppCompatActivity {

    //tag for console log
    private static final String TAG = "ForgotPasswordActivity";

    //connectivity variable
    private Connectivity device;

    //text input layout email
    private TextInputLayout inputEmailTIL;

    //firebaseauth variable
    private FirebaseAuth cAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        //initialize connectivity of device
        device = new Connectivity(ForgotPasswordActivity.this);

        //find the id of email field
        inputEmailTIL = findViewById(R.id.forgotPasswordEmailTextInputLayout);

        //initialize firebase auth
        cAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!device.haveNetwork()){
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

    public void sendEmailRequest(View view) {

        //validate email pattern or check the field whether is empty
        if(!validateForgotPasswordEmail()){
            return;
        }

        //check connectivity of device
        if(!device.haveNetwork()){
            //if no network, show error
            Toasty.error(ForgotPasswordActivity.this, device.NetworkError(), Toast.LENGTH_SHORT, true).show();
        }
        else{
            //a progress dialog to view progress of create account
            final ProgressDialog progressDialog = new ProgressDialog(ForgotPasswordActivity.this);

            //set message for progress dialog
            progressDialog.setMessage("Sending Request... " +
                    "Please wait awhile, we are processing the request");

            //show dialog
            progressDialog.show();

            //get validated email
            String validatedEmail = inputEmailTIL.getEditText().getText().toString().trim();
            cAuth.sendPasswordResetEmail(validatedEmail)
                    //if send request success
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "sendRequest:success");
                            Toasty.success(ForgotPasswordActivity.this,
                                    "Sent Request Successfully. Please check your email."
                                    ,Toast.LENGTH_SHORT,true)
                                    .show();
                            clearField();
                            progressDialog.dismiss();
                        }
                    })
                    //if send request failed
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //console log
                            Log.d(TAG, "sendRequest:failed");

                            //show error message
                            Toasty.error(ForgotPasswordActivity.this,
                                    "Send Request Failed. Please Try Again."
                                    ,Toast.LENGTH_SHORT,true)
                                    .show();
                            progressDialog.dismiss();
                        }
                    });
        }
    }

    //validate user input email
    private boolean validateForgotPasswordEmail(){
        //get email
        String emailInput = inputEmailTIL.getEditText().getText().toString().trim();

        //if email == null
        if(emailInput.isEmpty())
        {
            inputEmailTIL.setError("Field can't be empty");
            return false;
        }
        //if email address not an valid email address
        else if(!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()){
            inputEmailTIL.setError("Please enter a valid email address");
            return false;
        }
        else{
            inputEmailTIL.setError(null);
            return true;
        }
    }

    //clear field
    private void clearField(){
        inputEmailTIL.getEditText().getText().clear();
    }
}