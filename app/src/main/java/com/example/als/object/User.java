package com.example.als.object;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class User {

    //string for id
    private String id;
    //string for register date time
    private String registerDateTime;

    //string for loggedIn date time
    private String loggedInDateTime;

    //string for loggedoutdatetime
    private String loggedOutDateTime;

    //role
    private String role;

    //boolean for determine whether the user is first time login
    private boolean isFirstTimeLoggedIn;

    private String token;



    public User() {
    }

    public String getRegisterDateTime() {
        return registerDateTime;
    }

    public void setRegisterDateTime(String registerDateTime) {
        this.registerDateTime = registerDateTime;
    }

    public String getLoggedInDateTime() {
        return loggedInDateTime;
    }

    public void setLoggedInDateTime(String loggedInDateTime) {
        this.loggedInDateTime = loggedInDateTime;
    }

    public String getLoggedOutDateTime() {
        return loggedOutDateTime;
    }

    public void setLoggedOutDateTime(String loggedOutDateTime) {
        this.loggedOutDateTime = loggedOutDateTime;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isFirstTimeLoggedIn() {
        return isFirstTimeLoggedIn;
    }

    public void setFirstTimeLoggedIn(boolean firstTimeLoggedIn) {
        isFirstTimeLoggedIn = firstTimeLoggedIn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Exclude
    public Map<String, Object> userMap(){
        HashMap<String,Object> result = new HashMap<>();
        result.put("id", id);
        result.put("registerDateTime", registerDateTime);
        result.put("loggedInDateTime", loggedInDateTime);
        result.put("loggedOutDateTime", loggedOutDateTime);
        result.put("role", role);
        result.put("firstTimeLoggedIn", isFirstTimeLoggedIn);
        //result.put("token", token);
        return result;
    }
}
