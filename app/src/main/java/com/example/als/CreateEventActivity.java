package com.example.als;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.als.handler.Connectivity;
import com.example.als.object.Event;
import com.example.als.object.Variable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class CreateEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{

    private static final String TAG = "CreateEvent";
    private Connectivity device;
    private FirebaseAuth cAuth;

    private static final int PERMISSION_CODE = 150;
    private TextView promptEventImageTV;
    private Uri imageUri;
    private ImageView eventImageView;
    private TextInputLayout eventNameTIL, eventDescriptionTIL, eventTargetFundsTIL;
    private TextView eventStartDateTV, eventEndDateTV;
    private boolean onClickStartDateImageBtnStatus = false;
    private boolean onClickEndDateImageBtnStatus = false;

    Toolbar customizeCreateEventToolbar;
    TextView publishTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        device = new Connectivity(CreateEventActivity.this);

        customizeCreateEventToolbar = findViewById(R.id.customizeCreateEventToolbar);
        publishTextView = findViewById(R.id.publishTextView);
        setSupportActionBar(customizeCreateEventToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Create Event");

        if(!device.haveNetwork()){
            //show error message
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            cAuth = FirebaseAuth.getInstance();
        }

        promptEventImageTV = findViewById(R.id.promptImageTextView);
        eventImageView = findViewById(R.id.eventMainImage);
        eventNameTIL = findViewById(R.id.eventNameTextInputLayout);
        eventDescriptionTIL = findViewById(R.id.eventDescriptionTextInputLayout);
        eventStartDateTV = findViewById(R.id.eventStartDateTextView);
        eventEndDateTV = findViewById(R.id.eventEndDateTextView);
        eventTargetFundsTIL = findViewById(R.id.eventTargetFundTextInputLayout);

        ImageButton startDateImageBtn = findViewById(R.id.startDateImageButton);
        ImageButton endDateImageBtn = findViewById(R.id.endDateImageButton);

        startDateImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickStartDateImageBtnStatus = true;
                showDatePickerDialog();
            }
        });

        endDateImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickEndDateImageBtnStatus = true;
                showDatePickerDialog();
            }
        });

        eventStartDateTV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //
            }

            @Override
            public void afterTextChanged(Editable s) {
                //get event start date
                String eventStartDateInput = eventStartDateTV.getText().toString().trim();
                if(eventStartDateInput.isEmpty())
                {
                    eventStartDateTV.setError("Please set the start date for the event");
                }
                else{
                    eventStartDateTV.setError(null);
                }
            }
        });

        eventEndDateTV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //
            }

            @Override
            public void afterTextChanged(Editable s) {
                //get event end date
                String eventEndDateInput = eventEndDateTV.getText().toString().trim();

                //if event end date == null
                if(eventEndDateInput.isEmpty())
                {
                    eventEndDateTV.setError("Please set the end date for the event");
                }
                else{
                    eventEndDateTV.setError(null);
                }
            }
        });


        eventNameTIL.getEditText().addTextChangedListener(createEventTextWatcher);
        eventDescriptionTIL.getEditText().addTextChangedListener(createEventTextWatcher);
        eventStartDateTV.addTextChangedListener(createEventTextWatcher);
        eventEndDateTV.addTextChangedListener(createEventTextWatcher);
        eventTargetFundsTIL.getEditText().addTextChangedListener(createEventTextWatcher);


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //if device no network
        if(!device.haveNetwork()){
            //show error message
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            //get current user
            FirebaseUser cUser = cAuth.getCurrentUser();

            //if user != null
            if(cUser != null)
            {
                //show success message to console log
                Log.d(TAG, "getCurrentUser: success");
            }
            else
            {
                //show error message to console log
                Log.d(TAG, "getCurrentUser: failed");

                //show error message to user
                Toasty.error(getApplicationContext(), "Session is expired. Please relogin.", Toast.LENGTH_SHORT,true).show();

                //intent user to login page (relogin)
                Intent i = new Intent(CreateEventActivity.this, LoginActivity.class);

                //clear the background task
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //if device no network
        if(!device.haveNetwork()){
            //show error message
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            //get current user
            FirebaseUser cUser = cAuth.getCurrentUser();

            //if user != null
            if(cUser != null)
            {
                //show success message to console log
                Log.d(TAG, "getCurrentUser: success");
            }
            else
            {
                //show error message to console log
                Log.d(TAG, "getCurrentUser: failed");

                //show error message to user
                Toasty.error(getApplicationContext(), "Session is expired. Please relogin.", Toast.LENGTH_SHORT,true).show();

                //intent user to login page (relogin)
                Intent i = new Intent(CreateEventActivity.this, LoginActivity.class);

                //clear the background task
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        }
    }

    @Override
    protected void onStop() {
        //if device no network
        if(!device.haveNetwork()){
            //show error message
            Toasty.error(getApplicationContext(),device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            //get current user
            FirebaseUser cUser = cAuth.getCurrentUser();

            //if user != null
            if(cUser != null)
            {
                //show success message to console log
                Log.d(TAG, "getCurrentUser: success");
            }
            else
            {
                //show error message to console log
                Log.d(TAG, "getCurrentUser: failed");

                //show error message to user
                Toasty.error(getApplicationContext(), "Session is expired. Please relogin.", Toast.LENGTH_SHORT,true).show();

                //intent user to login page (relogin)
                Intent i = new Intent(CreateEventActivity.this, LoginActivity.class);

                //clear the background task
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        }
        super.onStop();
    }

    private void showDatePickerDialog(){
        DatePickerDialog dPD = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        Date currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        c.add( Calendar.DAY_OF_MONTH, +5 );
        long minDate = c.getTime().getTime();

        dPD.getDatePicker().setMinDate(minDate);
        dPD.setCancelable(false);
        dPD.show();
    }

    public void chooseImage(View view) {
        if(Build.VERSION.SDK_INT >=23)
        {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CODE);
            }
            else
            {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(this);
            }
        }
        else{
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
    }

    private TextWatcher createEventTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String eventName = eventNameTIL.getEditText().getText().toString().trim();
            String eventDescription = eventDescriptionTIL.getEditText().getText().toString().trim();
            String eventStartDate = eventStartDateTV.getText().toString().trim();
            String eventEndDate = eventEndDateTV.getText().toString().trim();
            String eventTargetFund = eventTargetFundsTIL.getEditText().getText().toString().trim();

            publishTextView.setEnabled(!eventName.isEmpty() && !eventDescription.isEmpty() && !eventStartDate.isEmpty() && !eventEndDate.isEmpty() && !eventTargetFund.isEmpty());

            if(!publishTextView.isEnabled()){
                publishTextView.setTextColor(getColor(R.color.colorGray));
            }
            else{
                publishTextView.setTextColor(getColor(R.color.colorAccent));
                publishTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    public void createEvent(View view) {
        if(!validateEventMainImage() | !validateEventName() | !validateEventDescription() | !validateEventStartDate() | !validateEventEndDate() | !validateEventTargetFund()){
            return;
        }

        final FirebaseUser cUser = cAuth.getCurrentUser();

        if(cUser != null){
            final String eventName = eventNameTIL.getEditText().getText().toString().trim();
            final String eventDescription = eventDescriptionTIL.getEditText().getText().toString().trim();
            final String eventStartDate = eventStartDateTV.getText().toString().trim();
            final String eventEndDate = eventEndDateTV.getText().toString().trim();
            final double eventTargetFund = Double.parseDouble(eventTargetFundsTIL.getEditText().getText().toString().trim());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US);
            Date dateObj = Calendar.getInstance().getTime();
            final String dateTimeCreated = simpleDateFormat.format(dateObj);
            //get extension from image uri

            //initialize pattern of date
            SimpleDateFormat imageSimpleDateFormat = new SimpleDateFormat("ddMMyyyyhhmmss", Locale.US);
            //initialize date object
            Date imageDateObj = Calendar.getInstance().getTime();
            //initialize string for current date time use the pattern above
            final String imageDateTime = imageSimpleDateFormat.format(imageDateObj);
            String extension = imageUri.toString().substring(imageUri.toString().lastIndexOf("."));
            final String eventImageName = "event"+imageDateTime+extension;

            //a progress dialog to view progress of create account
            final ProgressDialog progressDialog = new ProgressDialog(CreateEventActivity.this);

            //set message for progress dialog
            progressDialog.setMessage("Creating Event...");

            //show dialog
            progressDialog.show();

            Variable.EVENT_SR.child(eventImageName).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "uploadEventImage: failed");
                    Event newEvent = new Event();
                    newEvent.setEventTitle(eventName);
                    newEvent.setEventDescription(eventDescription);
                    newEvent.setEventStartDate(eventStartDate);
                    newEvent.setEventEndDate(eventEndDate);
                    newEvent.setEventTargetAmount(eventTargetFund);
                    newEvent.setEventCurrentAmount(0);
                    newEvent.setEventDateTimeCreated(dateTimeCreated);
                    newEvent.setEventHandler(cUser.getUid());
                    newEvent.setEventImageName(eventImageName);
                    newEvent.setEventVerifyStatus(false);

                    DatabaseReference pushedEvent = Variable.EVENT_REF.push();
                    String id = pushedEvent.getKey();
                    newEvent.setEventId(id);
                    Variable.EVENT_REF.child(newEvent.getEventId()).setValue(newEvent).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Log.d(TAG, "uploadEventData: success");
                            Toasty.success(CreateEventActivity.this, "Create Event Successfully",Toast.LENGTH_SHORT, true).show();
                            finish();
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Log.d(TAG, "uploadEventData: failed");
                                    Toasty.error(CreateEventActivity.this, "Something went Wrong. Please Contact Administrator",Toast.LENGTH_SHORT, true).show();
                                }
                            });

                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Log.d(TAG, "uploadEventImage: failed");
                            Toasty.error(CreateEventActivity.this, "Create Event Failed. Please Try Again",Toast.LENGTH_SHORT, true).show();
                        }
                    });
        }
        else{
            //show error message to console log
            Log.d(TAG, "getCurrentUser: failed");

            //show error message to user
            Toasty.error(getApplicationContext(), "Session is expired. Please relogin.", Toast.LENGTH_SHORT,true).show();

            //intent user to login page (relogin)
            Intent i = new Intent(CreateEventActivity.this, LoginActivity.class);

            //clear the background task
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK && result.getUri() != null) {
                promptEventImageTV.setVisibility(View.GONE);
                eventImageView.setVisibility(View.VISIBLE);
                imageUri = result.getUri();
                eventImageView.setImageURI(imageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d(TAG, String.valueOf(error));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "permission: granted");
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(this);
            } else {
                Log.d(TAG, "permission: denied");
            }
        }
    }

    public void chooseImageAgain(View view) {
        if(Build.VERSION.SDK_INT >=23)
        {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CODE);
            }
            else
            {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(this);
            }
        }
        else{
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
    }

    @Override
    public void onDateSet(DatePicker view, final int year, int month, final int dayOfMonth) {
        month = month + 1;
        String choosenDate = month + "/" + dayOfMonth + "/" + year;
        if(onClickStartDateImageBtnStatus){
            eventStartDateTV.setText(choosenDate);
            onClickStartDateImageBtnStatus = false;
        }
        else if(onClickEndDateImageBtnStatus){
            eventEndDateTV.setText(choosenDate);
            onClickEndDateImageBtnStatus = false;
        }
    }

    //validate event name
    private boolean validateEventName(){
        //get event name
        String eventNameInput = eventNameTIL.getEditText().getText().toString().trim();

        //if event name == null
        if(eventNameInput.isEmpty())
        {
            eventNameTIL.getEditText().setError("Field can't be empty");
            return false;
        }
        else{
            eventNameTIL.setError(null);
            return true;
        }
    }

    //validate event description
    private boolean validateEventDescription(){
        //get event description
        String eventDescriptionInput = eventDescriptionTIL.getEditText().getText().toString().trim();

        //if event description == null
        if(eventDescriptionInput.isEmpty())
        {
            eventDescriptionTIL.getEditText().setError("Field can't be empty");
            return false;
        }
        else{
            return true;
        }
    }

    //validate event start date
    private boolean validateEventStartDate(){
        //get event start date
        String eventStartDateInput = eventStartDateTV.getText().toString().trim();

        //if event start date == null
        if(eventStartDateInput.isEmpty())
        {
            eventStartDateTV.setError("Please set the start date for the event");
            return false;
        }
        else{
            eventStartDateTV.setError(null);
            return true;
        }
    }

    //validate event end date
    private boolean validateEventEndDate(){
        //get event end date
        String eventEndDateInput = eventEndDateTV.getText().toString().trim();

        //if event end date == null
        if(eventEndDateInput.isEmpty())
        {
            eventEndDateTV.setError("Please set the end date for the event");
            return false;
        }
        else{
            eventEndDateTV.setError(null);
            return true;
        }
    }

    //validate event main image
    private boolean validateEventMainImage(){
        if(imageUri == null){
            Toasty.error(CreateEventActivity.this, "Please set the main image for the event", Toast.LENGTH_SHORT, true).show();
            return false;
        }
        else{
            return true;
        }
    }

    //validate event target fund
    private boolean validateEventTargetFund(){
        //get event name
        String eventTargetFundInput = eventTargetFundsTIL.getEditText().getText().toString().trim();

        //if event name == null
        if(eventTargetFundInput.isEmpty())
        {
            eventTargetFundsTIL.getEditText().setError("Field can't be empty");
            return false;
        }
        else{
            eventTargetFundsTIL.setError(null);
            return true;
        }
    }
}