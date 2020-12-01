package com.example.als.object;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Donation {

    private String donationId;
    private String donationUserId;
    private String donationEventId;
    private double donationAmount;
    private String donationDateTime;

    public Donation(){
        //
    }

    public String getDonationId() {
        return donationId;
    }

    public void setDonationId(String donationId) {
        this.donationId = donationId;
    }

    public String getDonationUserId() {
        return donationUserId;
    }

    public void setDonationUserId(String donationUserId) {
        this.donationUserId = donationUserId;
    }

    public String getDonationEventId() {
        return donationEventId;
    }

    public void setDonationEventId(String donationEventId) {
        this.donationEventId = donationEventId;
    }

    public double getDonationAmount() {
        return donationAmount;
    }

    public void setDonationAmount(double donationAmount) {
        this.donationAmount = donationAmount;
    }

    public String getDonationDateTime() {
        return donationDateTime;
    }

    public void setDonationDateTime(String donationDateTime) {
        this.donationDateTime = donationDateTime;
    }

    @Exclude
    public Map<String, Object> donationMap(){
        HashMap<String,Object> result = new HashMap<>();

        result.put("donationId", donationId);
        result.put("donationUserId", donationUserId);
        result.put("donationEventId", donationEventId);
        result.put("donationAmount", donationAmount);
        result.put("donationDateTime", donationDateTime);

        return result;
    }
}
