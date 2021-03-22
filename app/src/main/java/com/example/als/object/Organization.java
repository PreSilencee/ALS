package com.example.als.object;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Organization {

    //organization details
    private String userId;
    private String organizationName;
    private String organizationType;
    private String organizationRegistrationNumber;
    private String organizationDescription;
    private String organizationAddress;
    private String organizationEmail;
    private String organizationPhone;
    private String organizationProfileImageName;
    private String organizationVerifyStatus;


    public Organization() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getOrganizationType() {
        return organizationType;
    }

    public void setOrganizationType(String organizationType) {
        this.organizationType = organizationType;
    }

    public String getOrganizationRegistrationNumber() {
        return organizationRegistrationNumber;
    }

    public void setOrganizationRegistrationNumber(String organizationRegistrationNumber) {
        this.organizationRegistrationNumber = organizationRegistrationNumber;
    }

    public String getOrganizationDescription() {
        return organizationDescription;
    }

    public void setOrganizationDescription(String organizationDescription) {
        this.organizationDescription = organizationDescription;
    }

    public String getOrganizationAddress() {
        return organizationAddress;
    }

    public void setOrganizationAddress(String organizationAddress) {
        this.organizationAddress = organizationAddress;
    }

    public String getOrganizationPhone() {
        return organizationPhone;
    }

    public void setOrganizationPhone(String organizationPhone) {
        this.organizationPhone = organizationPhone;
    }

    public String getOrganizationProfileImageName() {
        return organizationProfileImageName;
    }

    public void setOrganizationProfileImageName(String organizationProfileImageName) {
        this.organizationProfileImageName = organizationProfileImageName;
    }

    public String getOrganizationVerifyStatus() {
        return organizationVerifyStatus;
    }

    public void setOrganizationVerifyStatus(String organizationVerifyStatus) {
        this.organizationVerifyStatus = organizationVerifyStatus;
    }

    public String getOrganizationEmail() {
        return organizationEmail;
    }

    public void setOrganizationEmail(String organizationEmail) {
        this.organizationEmail = organizationEmail;
    }

    @Exclude
    public Map<String, Object> organizationMap(){
        HashMap<String,Object> result = new HashMap<>();

        result.put("userId", userId);
        result.put("organizationName", organizationName);
        result.put("organizationEmail", organizationEmail);
        result.put("organizationType", organizationType);
        result.put("organizationRegistrationNumber", organizationRegistrationNumber);
        result.put("organizationDescription", organizationDescription);
        result.put("organizationAddress", organizationAddress);
        result.put("organizationPhone", organizationPhone);
        result.put("organizationProfileImageName", organizationProfileImageName);
        result.put("organizationVerifyStatus", organizationVerifyStatus);

        return result;
    }
}
