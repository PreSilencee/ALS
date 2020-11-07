package com.example.als.object;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Contributor {
    //normal donator details
    private String name;
    private String phone;
    private String profileImageName;

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

    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String,Object> result = new HashMap<>();
        result.put("name", name);
        result.put("phone", phone);
        result.put("profileImageName", profileImageName);

        return result;
    }
}
