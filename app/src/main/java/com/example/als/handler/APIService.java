package com.example.als.handler;

import com.example.als.notification.MyResponse;
import com.example.als.notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

//get service center
public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAYPJRP7U:APA91bGaGmPvI6iLzXRQ7vMP_CGRmwbOjGuiUb8Neub9eLT5onHF_ft_xfYD3yZLF2J-rLe-fI9KkEMZY0FOGmF2FkhV-72V2oScnhPkWIJRfySZWv1iPHpQtRg2R86E06GJwiryAhxw"
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
