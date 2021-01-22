package com.example.als;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.als.handler.Connectivity;
import com.example.als.handler.ValidateFunction;
import com.example.als.object.Organization;
import com.example.als.object.User;
import com.example.als.object.Variable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class RegisterOrganizationActivity extends AppCompatActivity {

    //console log
    private static final String TAG = "Register Organization";
    private Connectivity device;
    //text input layout for email, password, confirm password
    private TextInputLayout createOrganizationAccountEmailTIL, createOrganizationAccountPasswordTIL, createOrganizationAccountConfirmPasswordTIL;

    //text input layout for organization details
    private TextInputLayout createOrganizationNameTIL, createOrganizationRegistrationNumberTIL, createOrganizationDescriptionTIL,
            createOrganizationAddressTIL, createOrganizationPhoneTIL;

    private Spinner createOrganizationtypeSpinn;
    private String selectedType;

    //firebase authentication variable
    private FirebaseAuth cAuth;

    CheckBox organizationAgreeCB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_organization);

        //initialize connectivity
        device = new Connectivity(this);

        //check connectivity
        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_LONG).show();
        }
        else{
            //initialize firebase auth
            cAuth = FirebaseAuth.getInstance();
        }

        organizationAgreeCB = findViewById(R.id.organizationAgreeCheckBox);

        //get email id from XML
        createOrganizationAccountEmailTIL = findViewById(R.id.createOrganizationAccountEmailTextInputLayout);
        //get password id from XML
        createOrganizationAccountPasswordTIL = findViewById(R.id.createOrganizationAccountPasswordTextInputLayout);
        //get confirm password from XML
        createOrganizationAccountConfirmPasswordTIL = findViewById(R.id.createOrganizationAccountConfirmPasswordTextInputLayout);

        //find id for text input layout
        createOrganizationNameTIL = findViewById(R.id.createOrganizationNameTextInputLayout);
        createOrganizationRegistrationNumberTIL = findViewById(R.id.createOrganizationRegistrationNumberTextInputLayout);
        createOrganizationDescriptionTIL = findViewById(R.id.createOrganizationDescriptionTextInputLayout);
        createOrganizationAddressTIL = findViewById(R.id.createOrganizationAddressTextInputLayout);
        createOrganizationPhoneTIL = findViewById(R.id.createOrganizationPhoneTextInputLayout);

        //find id for spinner
        createOrganizationtypeSpinn = findViewById(R.id.createOrganizationTypeSpinner);

        //create an array list that store the type of organization
        List<String> organizationTypeList = new ArrayList<>();
        organizationTypeList.add("--Select Organization Type--");
        organizationTypeList.add("Government Organization (GO)");
        organizationTypeList.add("Non-Government Organization (NGO)");

        //create array adapter for spinner
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, organizationTypeList){
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;

                //if list == "--Select Organization Type--"
                if(position == 0)
                {
                    tv.setTextColor(Color.GRAY);
                }
                else
                {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }

            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }
        };

        //set drop down resources
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //set adapter to spinner
        createOrganizationtypeSpinn.setAdapter(myAdapter);

        createOrganizationtypeSpinn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //if current position not "--Select Organization Type--"
                if(position > 0)
                {
                    selectedType = (String) parent.getItemAtPosition(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });


    }

    //validate organizationType
    private boolean validateOrganizationType() {
        //if selectedType == null
        if(selectedType == null){
            Toasty.warning(this, "Please select the organization type", Toast.LENGTH_LONG).show();
            return false;
        }
        else{
            return true;
        }
    }


    public void createOrganizationAccount(View view) {
        //check email, password and confirm password;
        if(!ValidateFunction.validateTILField(createOrganizationNameTIL) | !ValidateFunction.validateEmail(createOrganizationAccountEmailTIL)
                | !ValidateFunction.validatePassword(createOrganizationAccountPasswordTIL)
                | !ValidateFunction.validateConfirmPassword(createOrganizationAccountPasswordTIL, createOrganizationAccountConfirmPasswordTIL)
                | !validateOrganizationType()
                | !ValidateFunction.validateTILField(createOrganizationRegistrationNumberTIL)
                | !ValidateFunction.validateTILField(createOrganizationDescriptionTIL)
                | !ValidateFunction.validateTILField(createOrganizationAddressTIL)
                | !ValidateFunction.validateTILField(createOrganizationPhoneTIL)){
            return;
        }

        if(!organizationAgreeCB.isChecked()){
            Toasty.warning(getApplicationContext(), "You must agree our terms & conditions first before signing up.", Toast.LENGTH_LONG).show();
            return;
        }

        //check connectivity
        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_LONG).show();
        }
        else{

            //a progress dialog to view progress of create account
            final ProgressDialog progressDialog = new ProgressDialog(RegisterOrganizationActivity.this);

            //set message for progress dialog
            progressDialog.setMessage("One moment...");

            //show dialog
            progressDialog.show();

            //get email that input by user
            final String organizationEmail = createOrganizationAccountEmailTIL.getEditText().getText().toString().trim();
            //get password that input by user
            final String organizationPassword = createOrganizationAccountPasswordTIL.getEditText().getText().toString().trim();

            //set string from text input layout
            final String organizationName = createOrganizationNameTIL.getEditText().getText().toString().trim();
            final String registrationNumber = createOrganizationRegistrationNumberTIL.getEditText().getText().toString().trim();
            final String organizationDescription = createOrganizationDescriptionTIL.getEditText().getText().toString().trim();
            final String organizationAddress = createOrganizationAddressTIL.getEditText().getText().toString().trim();
            final String organizationPhone = createOrganizationPhoneTIL.getEditText().getText().toString().trim();

            cAuth.createUserWithEmailAndPassword(organizationEmail, organizationPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "createUserWithEmail:success");

                        //get current user
                        final FirebaseUser cUser = cAuth.getCurrentUser();

                        if(cUser != null){
                            //send the email verification to the user's email
                            cUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US);
                                        Date dateObj = Calendar.getInstance().getTime();
                                        final String currentDateTime = simpleDateFormat.format(dateObj);
                                        User newUser = new User();
                                        newUser.setId(cUser.getUid());
                                        newUser.setRole(Variable.ORGANIZATION);
                                        newUser.setRegisterDateTime(currentDateTime);
                                        newUser.setFirstTimeLoggedIn(true);

                                        Variable.USER_REF.child(cUser.getUid()).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Organization organization = new Organization();
                                                    organization.setUserId(cUser.getUid());
                                                    organization.setOrganizationEmail(cUser.getEmail());
                                                    organization.setOrganizationName(organizationName);
                                                    organization.setOrganizationType(selectedType);
                                                    organization.setOrganizationRegistrationNumber(registrationNumber);
                                                    organization.setOrganizationDescription(organizationDescription);
                                                    organization.setOrganizationAddress(organizationAddress);
                                                    organization.setOrganizationPhone(organizationPhone);
                                                    organization.setOrganizationVerifyStatus(false);

                                                    Variable.ORGANIZATION_REF.child(cUser.getUid()).setValue(organization).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                Toasty.success(getApplicationContext(), "Create Organization Account Successfully. " +
                                                                                "Please check your email for verification",
                                                                        Toast.LENGTH_LONG).show();

                                                                //hide the progress dialog
                                                                progressDialog.dismiss();

                                                                //clear the input field
                                                                clearField();

                                                                finish();

                                                                //sign out
                                                                cAuth.signOut();
                                                            }
                                                            else{
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
                                                else{
                                                    //show warning message (create account is successful,
                                                    // but database not include the user's data
                                                    Toasty.warning(getApplicationContext(), "Something went wrong. " +
                                                                    "Please do not use the same email to create account again" +
                                                                    "Please contact Administrator!"
                                                            , Toast.LENGTH_LONG).show();

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
                                    else{
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
                    }
                    else{
                        Log.d(TAG, "createUserWithEmail:failed");

                        //show message
                        Toasty.error(getApplicationContext(),
                                "Create Organization Account Failed. Please Try Again !"
                                , Toast.LENGTH_LONG).show();

                        //hide the progress dialog
                        progressDialog.dismiss();
                    }
                }
            });
        }

    }

    //clear fields that input by user
    private void clearField() {
        createOrganizationNameTIL.getEditText().getText().clear();
        createOrganizationAccountEmailTIL.getEditText().getText().clear();
        createOrganizationAccountPasswordTIL.getEditText().getText().clear();
        createOrganizationAccountConfirmPasswordTIL.getEditText().getText().clear();
        createOrganizationRegistrationNumberTIL.getEditText().getText().clear();
        createOrganizationDescriptionTIL.getEditText().getText().clear();
        createOrganizationAddressTIL.getEditText().getText().clear();
        createOrganizationPhoneTIL.getEditText().getText().clear();
        createOrganizationtypeSpinn.setSelection(0);
    }
}