package com.example.als.object;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Contributor {
    //normal donator details
    private String userId;
    private String email;
    private String name;
    private String phone;
    private String profileImageName;

    public Contributor(){
        //
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfileImageName() {
        return profileImageName;
    }

    public void setProfileImageName(String profileImageName) {
        this.profileImageName = profileImageName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Exclude
    public Map<String, Object> contributorMap(){
        HashMap<String,Object> result = new HashMap<>();

        result.put("userId", userId);
        result.put("name", name);
        result.put("email", email);
        result.put("phone", phone);
        result.put("profileImageName", profileImageName);

        return result;
    }
}
