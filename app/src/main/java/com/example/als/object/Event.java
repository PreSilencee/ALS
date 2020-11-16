package com.example.als.object;

public class Event {

    private String eventId;
    private String eventTitle;
    private String eventDescription;
    private String eventDateTimeCreated;
    private String eventStartDate;
    private String eventEndDate;
    private double eventTargetAmount;
    private double eventCurrentAmount;
    private String eventImageName;
    private String eventHandler;

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getEventDateTimeCreated() {
        return eventDateTimeCreated;
    }

    public void setEventDateTimeCreated(String eventDateTimeCreated) {
        this.eventDateTimeCreated = eventDateTimeCreated;
    }

    public String getEventStartDate() {
        return eventStartDate;
    }

    public void setEventStartDate(String eventStartDate) {
        this.eventStartDate = eventStartDate;
    }

    public String getEventEndDate() {
        return eventEndDate;
    }

    public void setEventEndDate(String eventEndDate) {
        this.eventEndDate = eventEndDate;
    }

    public double getEventTargetAmount() {
        return eventTargetAmount;
    }

    public void setEventTargetAmount(double eventTargetAmount) {
        this.eventTargetAmount = eventTargetAmount;
    }

    public double getEventCurrentAmount() {
        return eventCurrentAmount;
    }

    public void setEventCurrentAmount(double eventCurrentAmount) {
        this.eventCurrentAmount = eventCurrentAmount;
    }

    public String getEventImageName() {
        return eventImageName;
    }

    public void setEventImageName(String eventImageName) {
        this.eventImageName = eventImageName;
    }

    public String getEventHandler() {
        return eventHandler;
    }

    public void setEventHandler(String eventHandler) {
        this.eventHandler = eventHandler;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
