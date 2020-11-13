package com.example.als.object;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.regex.Pattern;

public class Variable {
    public static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=\\S+$)" + //no white spaces
                    ".{6,}" +     //at least 6 character
                    "$");
    public static final DatabaseReference USER_REF =
            FirebaseDatabase.getInstance()
                    .getReference().child("user");
    public static final DatabaseReference CONTRIBUTOR_REF =
            FirebaseDatabase.getInstance()
                    .getReference().child("contributor");
    public static final DatabaseReference ORGANIZATION_REF =
            FirebaseDatabase.getInstance()
                    .getReference().child("organization");
    public static final DatabaseReference EVENT_REF =
            FirebaseDatabase.getInstance().
                    getReference().child("event");
    public static final StorageReference CONTRIBUTOR_SR =
            FirebaseStorage.getInstance()
                    .getReference("contributor");
    public static final StorageReference ORGANIZATION_SR =
            FirebaseStorage.getInstance()
                    .getReference("organization");
    public static final StorageReference EVENT_SR =
            FirebaseStorage.getInstance()
                    .getReference("event");
    public static final String CONTRIBUTOR = "CONTRIBUTOR";
    public static final String ORGANIZATION = "ORGANIZATION";
    public static final String VERIFIED_ORGANIZATION = "VERIFIED ORGANIZATION";
    public static final String PENDING = "PENDING";
    public static final String EVENT_SESSION_ID = "eventSessionId";
    public static final String USER_SESSION_ID = "userSessionId";
    public static final String HOME_EVENT_SESSION_ID = "homeEventSessionId";
    public static final String HOME_USER_SESSION_ID = "homeUserSessionId";
    public static final String HOME_USER_SESSION_POSITION = "homeUserPositionSessionId";
    public static final String HOME_FRAGMENT = "homefragment";
    public static final String STRIPE_PUBLISHABLE_KEY = "pk_test_51Hklw2HeAF36bhnSQH6J2DDp2Ph7uHb9EaxurePC3cFhngCrp0fBsAbpBJkpC7gd2yp0odVCHDw587bAfOTqsULP00jEFJE1eW";
    public static final String STRIPE_SECRET_KEY = "sk_test_51Hklw2HeAF36bhnSQdpmDjcvqRH5zaWeayQd4jH0yfBEAZfzikTuSpbOAEusa6JvFOKNbILjbxVna9CmwlX1pF3F00S97DmFTA";

}

