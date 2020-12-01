package com.example.als.firstTimeUi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.als.R;
import com.example.als.handler.Connectivity;
import com.example.als.object.Organization;
import com.example.als.object.Variable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class SetUpOrganizationDetailsActivity extends AppCompatActivity {

    private static final String TAG = "SetUpOrganization";
    private Connectivity device;
    private TextInputLayout organizationNameTIL, registrationNumberTIL, descriptionTIL, addressTIL, phoneTIL;
    private Spinner typeSpinner;
    private String selectedType;
    private FirebaseAuth cAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up_organization_details);

        device = new Connectivity(SetUpOrganizationDetailsActivity.this);

        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            cAuth = FirebaseAuth.getInstance();
            organizationNameTIL = findViewById(R.id.organizationNameTextInputLayout);
            typeSpinner = findViewById(R.id.organizationSpinner);
            registrationNumberTIL = findViewById(R.id.organizationRegistrationNumber);
            descriptionTIL = findViewById(R.id.organizationDescription);
            addressTIL = findViewById(R.id.organizationAddress);
            phoneTIL = findViewById(R.id.organizationPhone);

            List<String> organizationTypeList = new ArrayList<>();
            organizationTypeList.add("--Select Organization Type--");
            organizationTypeList.add("Government Organization (GO)");
            organizationTypeList.add("Non-Government Organization (NGO)");

            ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(SetUpOrganizationDetailsActivity.this, android.R.layout.simple_list_item_1, organizationTypeList){
                @Override
                public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    TextView tv = (TextView) view;

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

            myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            typeSpinner.setAdapter(myAdapter);

            typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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
    }

    public void confirmOrganizationProfile(View view) {
        if(!validateOrganizationName() | !validateOrganizationType() | !validateRegistrationNumber() | !validateDescription()
                | !validateAddress() | !validatePhone()){
            return;
        }

        if(!device.haveNetwork()){
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            final FirebaseUser cUser = cAuth.getCurrentUser();
            if(cUser != null){
                final String organizationName = organizationNameTIL.getEditText().getText().toString().trim();
                final String registrationNumber = registrationNumberTIL.getEditText().getText().toString().trim();
                final String organizationDescription = descriptionTIL.getEditText().getText().toString().trim();
                final String organizationAddress = addressTIL.getEditText().getText().toString().trim();
                final String organizationPhone = phoneTIL.getEditText().getText().toString().trim();

                Variable.ORGANIZATION_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Organization organization = snapshot.getValue(Organization.class);

                            if(organization != null){
                                organization.setOrganizationName(organizationName);
                                organization.setOrganizationType(selectedType);
                                organization.setOrganizationRegistrationNumber(registrationNumber);
                                organization.setOrganizationDescription(organizationDescription);
                                organization.setOrganizationAddress(organizationAddress);
                                organization.setOrganizationPhone(organizationPhone);
                                organization.setOrganizationVerifyStatus(false);

                                Map<String, Object> organizationValues = organization.organizationMap();

                                Variable.ORGANIZATION_REF.child(cUser.getUid()).updateChildren(organizationValues).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "setuporganization: success");
                                        Toasty.success(SetUpOrganizationDetailsActivity.this, "Submitted Successfully. The information will be verified within one week",Toast.LENGTH_SHORT,true).show();
                                        finish();
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "setupprofile: failed");
                                                Toasty.success(SetUpOrganizationDetailsActivity.this, "Submitted Failed. Please Try Again",Toast.LENGTH_SHORT,true).show();
                                            }
                                        });
                            }
                            else{
                                Log.d(TAG, "organizationFound: failed");
                            }
                        }
                        else{
                            Log.d(TAG, "invalidorganizationfound: failed");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "databaseerror: " + error.getMessage());
                    }
                });

            }
        }


    }

    //validate organizationName
    private boolean validateOrganizationName() {
        //get organizationName
        String organizationInputName = organizationNameTIL.getEditText().getText().toString().trim();

        //if organizationName == null
        if(organizationInputName.isEmpty()){
            organizationNameTIL.setError("Field can't be empty");
            return false;
        }
        else{
            organizationNameTIL.setError(null);
            return true;
        }
    }

    //validate organizationType
    private boolean validateOrganizationType() {
        //if selectedType == null
        if(selectedType == null){
            Toasty.error(SetUpOrganizationDetailsActivity.this, "Please select the organization type", Toast.LENGTH_SHORT,true).show();
            return false;
        }
        else{
            return true;
        }
    }

    //validate registrationNumber
    private boolean validateRegistrationNumber() {
        //get registrationNumber
        String organizationInputRegistrationNumber = registrationNumberTIL.getEditText().getText().toString().trim();

        //if registrationNumber == null
        if(organizationInputRegistrationNumber.isEmpty()){
            registrationNumberTIL.setError("Field can't be empty");
            return false;
        }
        else{
            registrationNumberTIL.setError(null);
            return true;
        }
    }

    //validate description
    private boolean validateDescription() {
        //get description
        String organizationInputDescription = descriptionTIL.getEditText().getText().toString().trim();

        //if description == null
        if(organizationInputDescription.isEmpty()){
            descriptionTIL.setError("Field can't be empty");
            return false;
        }
        else{
            descriptionTIL.setError(null);
            return true;
        }
    }

    //validate address
    private boolean validateAddress() {
        //get address
        String organizationInputAddress = addressTIL.getEditText().getText().toString().trim();

        //if address == null
        if(organizationInputAddress.isEmpty()){
            addressTIL.setError("Field can't be empty");
            return false;
        }
        else{
            addressTIL.setError(null);
            return true;
        }
    }

    //validate phone
    private boolean validatePhone() {
        //get phone
        String organizationInputPhone = phoneTIL.getEditText().getText().toString().trim();

        //if phone == null
        if(organizationInputPhone.isEmpty()){
            phoneTIL.setError("Field can't be empty");
            return false;
        }
        else{
            phoneTIL.setError(null);
            return true;
        }
    }
}