package com.example.als.object;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Follow {
    private String followId;
    private String followDateTime;

    public String getFollowId() {
        return followId;
    }

    public void setFollowId(String followId) {
        this.followId = followId;
    }

    public String getFollowDateTime() {
        return followDateTime;
    }

    public void setFollowDateTime(String followDateTime) {
        this.followDateTime = followDateTime;
    }

    @Exclude
    public Map<String, Object> followMap(){
        HashMap<String,Object> result = new HashMap<>();

        result.put("followId", followId);
        result.put("followDateTime", followDateTime);

        return result;
    }
}
