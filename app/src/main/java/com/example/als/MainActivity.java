package com.example.als;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.als.firstTimeUi.SetUpAccountImageActivity;
import com.example.als.handler.Connectivity;
import com.example.als.handler.GlideApp;
import com.example.als.notification.Token;
import com.example.als.object.Contributor;
import com.example.als.object.Organization;
import com.example.als.object.User;
import com.example.als.object.Variable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.encoders.ObjectEncoder;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.core.view.MenuItemCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Map;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity{

    //tag for console log
    private static final String TAG = "MainActivity";

    //initialize connectivity variable
    private Connectivity device;

    //static variable firebase auth
    private FirebaseAuth cAuth;

    public static String user_session_id;

    //long value backPressedTime for back button
    private long backPressedTime;

    //Toast for back button
    private Toast backToast;

    private AppBarConfiguration mAppBarConfiguration;

    private ImageView headerImageView;

    private TextView headerUIDTV, headerPositionTV, headerEmailTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ma2);

        Intent session = getIntent();   //get to know previous activity when it intent to this activity
        if(session.hasExtra(Variable.USER_SESSION_ID))
        {
            user_session_id = session.getStringExtra(Variable.USER_SESSION_ID);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, CreateEventActivity.class));
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_raised_event_list, R.id.nav_message, R.id.nav_donation_history)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        headerImageView = headerView.findViewById(R.id.headerProfileImageView);
        headerUIDTV = headerView.findViewById(R.id.headerUIDTextView);
        headerPositionTV = headerView.findViewById(R.id.headerPositionTextView);
        headerEmailTV = headerView.findViewById(R.id.headerEmailTextView);

        //initialize connectivity device
        device = new Connectivity(MainActivity.this);

        if(!device.haveNetwork()){
            Toasty.error(MainActivity.this, device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            //initialize firebase auth
            cAuth = FirebaseAuth.getInstance();
            final FirebaseUser cUser = cAuth.getCurrentUser();

            if(cUser != null){
                headerUIDTV.setText(cUser.getUid());
                headerEmailTV.setText(cUser.getEmail());
                Variable.USER_REF.child(cUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Log.d(TAG, "userfoundindatabase: success");
                            User user = snapshot.getValue(User.class);

                            if(user != null){
                                if(user.isFirstTimeLoggedIn()){
                                    user.setFirstTimeLoggedIn(false);
                                    Map<String,Object> userValues = user.userMap();
                                    Variable.USER_REF.child(cUser.getUid()).setValue(userValues).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "setValuetoDatabase: success");
                                            startActivity(new Intent(MainActivity.this, SetUpAccountImageActivity.class));
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "setValuetoDatabase: failed"); }
                                    });
                                }

                                if(user.getRole() != null){
                                    headerPositionTV.setText(user.getRole());

                                    if(user.getRole().equals(Variable.CONTRIBUTOR)){
                                        Variable.CONTRIBUTOR_REF.child(cUser.getUid()).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(snapshot.exists()){
                                                    Contributor contributor = snapshot.getValue(Contributor.class);

                                                    if(contributor != null){
                                                        if(contributor.getProfileImageName() != null){
                                                            StorageReference imageRef = Variable.CONTRIBUTOR_SR.child(cUser.getUid())
                                                                    .child("profile").child(contributor.getProfileImageName());

                                                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                @Override
                                                                public void onSuccess(Uri uri) {
                                                                    Log.d(TAG, "loadImage: success");
                                                                    GlideApp.with(getApplicationContext())
                                                                            .load(uri)
                                                                            .placeholder(R.drawable.loading_image)
                                                                            .into(headerImageView);
                                                                }
                                                            })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Log.d(TAG, "loadImage:Failed");
                                                                            headerImageView.setImageResource(R.drawable.ic_baseline_person_color_accent_24);
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Log.d(TAG, "databaseError: "+error.getMessage());
                                            }
                                        });
                                    }
                                    else if(user.getRole().equals(Variable.ORGANIZATION)){
                                        Variable.ORGANIZATION_REF.child(cUser.getUid()).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(snapshot.exists()){
                                                    Organization organization = snapshot.getValue(Organization.class);

                                                    if(organization != null){
                                                        if(organization.getOrganizationProfileImageName() != null){
                                                            StorageReference imageRef = Variable.ORGANIZATION_SR.child(cUser.getUid())
                                                                    .child("profile").child(organization.getOrganizationProfileImageName());

                                                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                @Override
                                                                public void onSuccess(Uri uri) {
                                                                    Log.d(TAG, "loadImage: success");
                                                                    GlideApp.with(getApplicationContext())
                                                                            .load(uri)
                                                                            .placeholder(R.drawable.loading_image)
                                                                            .into(headerImageView);
                                                                }
                                                            })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Log.d(TAG, "loadImage:Failed");
                                                                            headerImageView.setImageResource(R.drawable.ic_baseline_person_color_accent_24);
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Log.d(TAG, "databaseError: "+error.getMessage());
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "userfoundindatabase: failed");
                        Log.d(TAG, "databaseerror: failed");
                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    //run after create layout
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
            final FirebaseUser cUser = cAuth.getCurrentUser();

            //if user != null
            if(cUser != null)
            {
                //show success message to console log
                Log.d(TAG, "getCurrentUser: success");
                FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<InstanceIdResult> task) {
                        if(task.isSuccessful()){
                            Variable.TOKEN_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        Token token = snapshot.getValue(Token.class);

                                        if(token != null)
                                        {
                                            token.setToken(task.getResult().getToken());

                                            Map<String, Object> tokenValues = token.tokenMap();

                                            Variable.TOKEN_REF.child(cUser.getUid()).updateChildren(tokenValues).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "Update Token: success");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "Exception: " + e.getMessage());
                                                }
                                            });
                                        }
                                    }
                                    else{
                                        Token token = new Token(task.getResult().getToken());
                                        Variable.TOKEN_REF.child(cUser.getUid()).setValue(token).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "Update Token: success");
                                            }
                                        })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d(TAG, "Exception: " + e.getMessage());
                                                    }
                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d(TAG, "Database Error: " + error.getMessage());
                                }
                            });
//                            Variable.USER_REF.child(cUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                    if(snapshot.exists()){
//                                        User user = snapshot.getValue(User.class);
//
//                                        if(user != null){
//                                            user.setToken(task.getResult().getToken());
//                                            Map<String, Object> userValues = user.userMap();
//
//                                            Variable.USER_REF.child(cUser.getUid()).updateChildren(userValues).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                @Override
//                                                public void onSuccess(Void aVoid) {
//                                                    Log.d(TAG, "Update Token: success");
//                                                }
//                                            })
//                                            .addOnFailureListener(new OnFailureListener() {
//                                                @Override
//                                                public void onFailure(@NonNull Exception e) {
//                                                    Log.d(TAG, "Exception: " + e.getMessage());
//                                                }
//                                            });
//                                        }
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError error) {
//
//                                }
//                            });
                        }
                    }
                });

            }
            else
            {
                //show error message to console log
                Log.d(TAG, "getCurrentUser: failed");

                //show error message to user
                Toasty.error(getApplicationContext(), "Session is expired. Please relogin.", Toast.LENGTH_SHORT,true).show();

                //intent user to login page (relogin)
                Intent i = new Intent(MainActivity.this, LoginActivity.class);

                //clear the background task
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        }
    }

    private void updateToken(String token){
        FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
        Token token1 = new Token(token);
        Variable.TOKEN_REF.child(cUser.getUid()).setValue(token1);
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

    //run when user click back button
    @Override
    public void onBackPressed() {
        //if backpressedtime more than system currenttime millies
        if(backPressedTime + 2000 > System.currentTimeMillis()){
            //dismiss toast
            backToast.cancel();

            //quit the app
            super.onBackPressed();
            return;
        }
        else
        {
            //initialize the message
            backToast = Toasty.info(getApplicationContext(),"Press back again to exit",Toast.LENGTH_SHORT,true);

            //show the message to the user
            backToast.show();
        }

        //backpressedtime = system.currentimemillies
        backPressedTime = System.currentTimeMillis();
    }

}