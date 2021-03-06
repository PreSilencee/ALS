package com.example.als.object;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.regex.Pattern;

public class Variable {

    //password pattern
    public static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=\\S+$)" + //no white spaces
                    ".{6,}" +     //at least 6 character
                    "$");

    //database reference
    public static final DatabaseReference TOKEN_REF =
            FirebaseDatabase.getInstance()
                    .getReference().child("Tokens");
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
    public static final DatabaseReference MESSAGE_REF =
            FirebaseDatabase.getInstance().
                    getReference().child("message");
    public static final DatabaseReference DONATION_REF =
            FirebaseDatabase.getInstance()
                    .getReference().child("donation");
    public static final DatabaseReference FOLLOW_REF =
            FirebaseDatabase.getInstance()
                    .getReference().child("follow");


    //storage reference
    public static final StorageReference CONTRIBUTOR_SR =
            FirebaseStorage.getInstance()
                    .getReference("contributor");
    public static final StorageReference ORGANIZATION_SR =
            FirebaseStorage.getInstance()
                    .getReference("organization");
    public static final StorageReference EVENT_SR =
            FirebaseStorage.getInstance()
                    .getReference("event");
    public static final StorageReference MESSAGE_SR =
            FirebaseStorage.getInstance().getReference("message");

    //string
    public static final String CONTRIBUTOR = "CONTRIBUTOR";
    public static final String ORGANIZATION = "ORGANIZATION";
    public static final String VERIFIED = "VERIFIED";
    public static final String PENDING = "PENDING";
    public static final String AVAILABLE = "AVAILABLE";
    public static final String DECLINED = "DECLINED";

    //sessionId
    public static final String EVENT_SESSION_ID = "eventSessionId";
    public static final String USER_SESSION_ID = "userSessionId";
    public static final String HOME_EVENT_SESSION_ID = "homeEventSessionId";
    public static final String HOME_EVENT_NAME_SESSION_ID = "homeEventNameSessionId";
    public static final String HOME_USER_SESSION_ID = "homeUserSessionId";
    public static final String MESSAGE_USER_SESSION_ID = "messageUserSessionId";
    public static final String DONATION_SESSION_ID = "donationSessionId";
    public static final String SEARCH_EVENT_SESSION_ID = "searchEventSessionId";

    public static final String MESSAGE_TYPE_TEXT = "text";
    public static final String MESSAGE_TYPE_FILE = "file";
    public static final String MESSAGE_TYPE_IMAGE = "image";

    //use for AESCrypt
    public static final String ALGORITHM = "AES";
    public static final byte[] encryptionKey = {9, 115, 51, 86, 105, 4, -31, -23, -68, 88, 17, 20, 3, -105, 119, -53};

    //message
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    public static final String MESSAGE_CHANNEL_ID = "MESSAGE_CHANNEL_ID";
    public static final String MESSAGE_CHANNEL_NAME = "MESSAGE_CHANNEL_NAME";
    public static final String MESSAGE_CHANNEL_DESC = "MESSAGE_CHANNEL_DESCRIPTION";

    public static final String SUCCESS = "Success";
    public static final String FAILED = "Failed";

    public static final String SEARCH_ITEM = "searchItem";
    public static final String FRAGMENT_STATE = "fragmentState";
    public static final String VISIBLE = "VISIBLE";
    public static final String NOTVISIBLE = "NOTVISIBLE";

}

