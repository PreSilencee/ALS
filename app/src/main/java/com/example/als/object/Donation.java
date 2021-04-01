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
    private String donationState;
    private String donationCurrencyCode;
    private String donationPaymentMethod;

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

    public String getDonationState() {
        return donationState;
    }

    public void setDonationState(String donationState) {
        this.donationState = donationState;
    }

    public String getDonationCurrencyCode() {
        return donationCurrencyCode;
    }

    public void setDonationCurrencyCode(String donationCurrencyCode) {
        this.donationCurrencyCode = donationCurrencyCode;
    }

    public String getDonationPaymentMethod() {
        return donationPaymentMethod;
    }

    public void setDonationPaymentMethod(String donationPaymentMethod) {
        this.donationPaymentMethod = donationPaymentMethod;
    }

    @Exclude
    public Map<String, Object> donationMap(){
        HashMap<String,Object> result = new HashMap<>();

        result.put("donationId", donationId);
        result.put("donationUserId", donationUserId);
        result.put("donationEventId", donationEventId);
        result.put("donationAmount", donationAmount);
        result.put("donationDateTime", donationDateTime);
        result.put("donationState", donationState);
        result.put("donationCurrencyCode", donationCurrencyCode);
        result.put("donationPaymentMethod", donationPaymentMethod);

        return result;
    }
}
