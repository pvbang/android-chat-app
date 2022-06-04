package com.example.chatapplication.utilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_PREFERENCE_NAME = "chatAPPPreference";

    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_USER_ID = "userID";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_IMAGE_BACKGROUND = "imageBackground";
    public static final String KEY_NUMBER = "number";
    public static final String KEY_BIRTHDAY = "birthday";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_INTRODUCE_YOURSEFT = "introduceYourself";

    public static final String KEY_IS_SIGN_IN = "isSignIn";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";
    public static final String KEY_AVAILABLILITY = "availability";

    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";

    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_LAST_USER = "lastUser";
    public static final String KEY_LAST_READ = "lastRead";

    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static final String KEY_COLLECTION_FRIENDS = "friends";
    public static final String KEY_COLLECTION_WAIT_FRIENDS = "wait_friends";
    public static final String KEY_COLLECTION_REQUEST_FRIENDS = "request_friends";

    public static final String REMOTE_MSG_CALLING_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CALLING_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_CALLING_TYPE = "type";
    public static final String REMOTE_MSG_CALLING_INVITATION = "invitation";
    public static final String REMOTE_MSG_CALLING_MEETING_TYPE = "meetingType";
    public static final String REMOTE_MSG_CALLING_TOKEN = "callingToken";
    public static final String REMOTE_MSG_CALLING_DATA = "data";
    public static final String REMOTE_MSG_CALLING_REGISTRATION_IDS = "registration_ids";

    public static final String REMOTE_MSG_CALLING_INVITATION_RESPONSE = "invitationResponse";
    public static final String REMOTE_MSG_CALLING_INVITATION_ACCEPTED = "accepted";
    public static final String REMOTE_MSG_CALLING_INVITATION_REJECTED = "rejected";
    public static final String REMOTE_MSG_CALLING_INVITATION_CANCELLED = "cancelled";

    public static final String REMOTE_MSG_CALLING_ROOM = "callingRoom";

    public static HashMap<String, String> remoteMegHeaders = null;
    public static HashMap<String, String> getRemoteMsgHeaders() {
        if (remoteMegHeaders == null) {
            remoteMegHeaders = new HashMap<>();
            remoteMegHeaders.put(REMOTE_MSG_AUTHORIZATION, "key=AAAAP4n8JBM:APA91bG21wIt0nsMcvZ0okc85qENTmRcleRxy8SyNUUm1fjEdFTSnFkHIANXU27eaYWL70ImiMCSJ5Po0h1kNzsj96_MoGpjZ7iGOwdsHIU0Gq9E_Gq3d5DslKvUUukdssp8pziWfQSJ");
            remoteMegHeaders.put(REMOTE_MSG_CONTENT_TYPE, "application/json");
        }
        return remoteMegHeaders;
    }

    public static HashMap<String, String> getRemoteCallingHeaders() {
        HashMap<String, String> remoteCallingHeaders = new HashMap<>();
        remoteCallingHeaders.put(Constants.REMOTE_MSG_CALLING_AUTHORIZATION, "key=AAAAP4n8JBM:APA91bGuuEY9MrBM7Eum7jf-565QEPWEW0qwvyXK8MZaZMEermp8C_7CjGbyMFF0OKAdMKySVJRZO6EfxsVTH6cyLy9w2CwZToET2SmTkECJUZ6dSBq2hT6Ll_iqPpcq0qG4GNx9VK5T");
        remoteCallingHeaders.put(Constants.REMOTE_MSG_CALLING_CONTENT_TYPE, "application/json");

        return remoteCallingHeaders;
    }

}
