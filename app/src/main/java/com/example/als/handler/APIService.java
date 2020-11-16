package com.example.als.handler;

import com.example.als.notification.MyResponse;
import com.example.als.notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAYPJRP7U:APA91bH81eA1ivI5WV97pCHnSduFxLydvhkwkvbDM6ylPAVz9PXVRNMqdRuQRAELUa8Uc78dZxCyWy9zoPmlp1gWmaO40da3CaIQfAu2P9JxSnIqGCjLX_VeEYwG-r7s8NhwNtlsTDTX"
    })
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
