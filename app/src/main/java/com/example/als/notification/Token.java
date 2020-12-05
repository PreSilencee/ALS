package com.example.als.notification;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Token {
    private String token;

    public Token(String token){
        this.token = token;
    }

    public Token(){
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Exclude
    public Map<String, Object> tokenMap(){
        HashMap<String,Object> result = new HashMap<>();
        result.put("token", token);
        return result;
    }
}
