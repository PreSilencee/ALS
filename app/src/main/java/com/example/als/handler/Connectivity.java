package com.example.als.handler;

import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Connectivity {

    //context for connectivity class
    private Context context;

    //constructor (context)
    public Connectivity(Context context){
        this.context = context;
    }

    //return boolean method (Check whether have wifi or mobile)
    public boolean haveNetwork(){

        boolean have_WIFI = false;
        boolean have_MobileData = false;

        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Service.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
        for(NetworkInfo info : networkInfo){
            if(info.getTypeName().equalsIgnoreCase("WIFI")){
                if(info.isConnected()){
                    have_WIFI = true;
                }
            }

            if (info.getTypeName().equalsIgnoreCase("MOBILE")) {
                if(info.isConnected()){
                    have_MobileData = true;
                }
            }
        }

        return have_WIFI | have_MobileData;
    }

    //return network error
    public String NetworkError(){
        return "No Network Connection";
    }
}
