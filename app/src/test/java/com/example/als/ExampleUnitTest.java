package com.example.als;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(MockitoJUnitRunner.class)
public class ExampleUnitTest {

    @Mock
    Context mockContext;


    @Test
    public void testsignIn(){
        mockContext = ApplicationProvider.getApplicationContext();
        FirebaseApp.initializeApp(mockContext);
        FirebaseAuth cAuth = FirebaseAuth.getInstance();
        cAuth.signInWithEmailAndPassword("b@gmail.com", "1234567").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                assertEquals(true, task.isSuccessful());
            }
        });
    }

//
//    @Test
//    public void addition_isCorrect() {
//        assertEquals(4, 2 + 2);
//    }
//
//    @Test
//    public void testCreateUserInFirebaseAuth(){
//        mockContext = ApplicationProvider.getApplicationContext();
//        FirebaseApp.initializeApp(mockContext);
//        FirebaseAuth cAuth = FirebaseAuth.getInstance();
//        cAuth.createUserWithEmailAndPassword("a@gmail.com", "123456")
//                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        assertEquals(true, task.isSuccessful());
//                    }
//                });
//
//    }
}