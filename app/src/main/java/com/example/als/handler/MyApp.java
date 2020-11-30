package com.example.als.handler;

import android.app.Application;

import com.example.als.object.Variable;
import com.stripe.android.PaymentConfiguration;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PaymentConfiguration.init(
                getApplicationContext(),
                Variable.STRIPE_KEY
        );
    }
}
