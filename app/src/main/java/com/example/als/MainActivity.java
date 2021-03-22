package com.example.als;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.als.adapter.ViewPagerAdapter;
import com.example.als.firstTimeUi.SetUpAccountImageActivity;
import com.example.als.handler.Connectivity;
import com.example.als.notification.Token;
import com.example.als.object.User;
import com.example.als.object.Variable;
import com.example.als.ui.SearchActivity;
import com.example.als.ui.more.SettingFragment;
import com.example.als.ui.home.HomeFragment;
import com.example.als.ui.message.MessageFragment;
import com.example.als.ui.raised_event.EventFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

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

    //view pager
    ViewPager mainViewPager;
    //maintablayout
    TabLayout mainTabLayout;

    //toolbar view
    View toolbarView;

    //toolbar
    Toolbar customizeToolbar;

    //image button
    ImageButton searchImageBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent session = getIntent();   //get to know previous activity when it intent to this activity
        if(session.hasExtra(Variable.USER_SESSION_ID))
        {
            user_session_id = session.getStringExtra(Variable.USER_SESSION_ID);
        }

        //find id from XML
        mainViewPager = findViewById(R.id.mainViewPager);
        mainTabLayout = findViewById(R.id.mainTabLayout);
        toolbarView = findViewById(R.id.toolbarView);
        customizeToolbar = findViewById(R.id.customizeToolbar);
        searchImageBtn = findViewById(R.id.searchImageButton);

        //set up customize toolbar
        setSupportActionBar(customizeToolbar);

        //set up view pager
        setupViewPager(mainViewPager);

        //set up tab layout into view pager
        mainTabLayout.setupWithViewPager(mainViewPager);

        //set up tab icon
        setUpTabIcons();

        //change color of tab icon if selected
        mainTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition() == 0){
                    toolbarView.setVisibility(View.VISIBLE);
                    mainTabLayout.getTabAt(0).getIcon().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
                }
                else{
                    toolbarView.setVisibility(View.GONE);
                    mainTabLayout.getTabAt(0).getIcon().setColorFilter(getResources().getColor(R.color.colorGray), PorterDuff.Mode.SRC_IN);
                }

                if(tab.getPosition() == 1){
                    mainTabLayout.getTabAt(1).getIcon().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
                }
                else{
                    mainTabLayout.getTabAt(1).getIcon().setColorFilter(getResources().getColor(R.color.colorGray), PorterDuff.Mode.SRC_IN);
                }

                if(tab.getPosition() == 2){
                    mainTabLayout.getTabAt(2).getIcon().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
                }
                else{
                    mainTabLayout.getTabAt(2).getIcon().setColorFilter(getResources().getColor(R.color.colorGray), PorterDuff.Mode.SRC_IN);
                }

                if(tab.getPosition() == 3){
                    mainTabLayout.getTabAt(3).getIcon().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
                }
                else{
                    mainTabLayout.getTabAt(3).getIcon().setColorFilter(getResources().getColor(R.color.colorGray), PorterDuff.Mode.SRC_IN);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //
            }
        });

        searchImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
            }
        });

        //initialize connectivity device
        device = new Connectivity(MainActivity.this);

        //get firebase authentication instance
        cAuth = FirebaseAuth.getInstance();

        if(!device.haveNetwork()){
            Toasty.error(MainActivity.this, device.NetworkError(), Toast.LENGTH_SHORT,true).show();
        }
        else{
            //initialize firebase auth
            cAuth = FirebaseAuth.getInstance();
            final FirebaseUser cUser = cAuth.getCurrentUser();

            if(cUser != null){
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
                                    Variable.USER_REF.child(cUser.getUid()).setValue(userValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Log.d(TAG, "setValuetoDatabase: success");
                                                startActivity(new Intent(MainActivity.this, SetUpAccountImageActivity.class));
                                            }
                                            else{
                                                Log.d(TAG, "setValuetoDatabase: failed");
                                            }
                                        }
                                    });
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

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        adapter.addFragment(new HomeFragment(), "");
        adapter.addFragment(new EventFragment(), "");
        adapter.addFragment(new MessageFragment(), "");
        adapter.addFragment(new SettingFragment(), "");

        viewPager.setAdapter(adapter);
    }

    private void setUpTabIcons(){
        mainTabLayout.getTabAt(0).setIcon(R.drawable.ic_outline_home_24);
        mainTabLayout.getTabAt(1).setIcon(R.drawable.ic_outline_event_note_24);
        mainTabLayout.getTabAt(2).setIcon(R.drawable.ic_outline_chat_24);
        mainTabLayout.getTabAt(3).setIcon(R.drawable.ic_baseline_view_headline_24);
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
            if(cUser != null) {
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

                                            Variable.TOKEN_REF.child(cUser.getUid()).updateChildren(tokenValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Log.d(TAG, "Update Token: success");
                                                    }
                                                    else{
                                                        Log.d(TAG, "Update Token: failed");
                                                    }
                                                }
                                            });
                                        }
                                    }
                                    else{
                                        Token token = new Token(task.getResult().getToken());
                                        Variable.TOKEN_REF.child(cUser.getUid()).setValue(token).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Log.d(TAG, "Update Token: success");
                                                }
                                                else{
                                                    Log.d(TAG, "Update Token: failed");
                                                }
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d(TAG, "Database Error: " + error.getMessage());
                                }
                            });
                        }
                    }
                });

            }
            else {
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

    @Override
    protected void onResume() {
        super.onResume();
        if(!device.haveNetwork())
        {
            Toasty.error(getApplicationContext(),device.NetworkError(),Toast.LENGTH_SHORT,true).show();
        }
    }

    //run when user click back button
    @Override
    public void onBackPressed() {
        //if back pressed time more than system currenttime millies
        if(backPressedTime + 2000 > System.currentTimeMillis()){
            //dismiss toast
            backToast.cancel();

            //quit the app
            super.onBackPressed();
            return;
        }
        else {
            //initialize the message
            backToast = Toasty.info(getApplicationContext(),"Press back again to exit",Toast.LENGTH_LONG);

            //show the message to the user
            backToast.show();
        }

        //backpressedtime = system.currentimemillies
        backPressedTime = System.currentTimeMillis();
    }

}