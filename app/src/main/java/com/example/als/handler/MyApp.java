package com.example.als.handler;

import android.app.Application;

import com.stripe.android.PaymentConfiguration;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_51Hklw2HeAF36bhnSQH6J2DDp2Ph7uHb9EaxurePC3cFhngCrp0fBsAbpBJkpC7gd2yp0odVCHDw587bAfOTqsULP00jEFJE1eW"
        );
    }
}
