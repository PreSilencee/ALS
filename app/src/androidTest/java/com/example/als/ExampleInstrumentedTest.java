package com.example.als;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
//    @Test
//    public void useAppContext() {
//        // Context of the app under test.
//        assertEquals("com.example.als", appContext.getPackageName());
//    }
//
//    @Test
//    public void testConnectionFirebase(){
//        Log.d("testConnectionFirebase: ", String.valueOf(FirebaseApp.initializeApp(appContext)));
//        assertNotNull(FirebaseApp.initializeApp(appContext));
//
//    }

    @Test
    public void testCreateUserInFirebaseAuth(){

        FirebaseApp.initializeApp(appContext);
        FirebaseAuth cAuth = FirebaseAuth.getInstance();
        cAuth.createUserWithEmailAndPassword("a@gmail.com", "123456")
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                assertEquals(true, task.isSuccessful());
            }
        });
        //assertTrue(status);


    }
//
//    @Test
//    public void testSignIn(){
//        FirebaseApp.initializeApp(appContext);
//        FirebaseAuth cAuth = FirebaseAuth.getInstance();
//        boolean status = cAuth.signInWithEmailAndPassword("junioraymond92@gmail.com", "123456").isSuccessful();
//        assertEquals(true, status);
//    }
}