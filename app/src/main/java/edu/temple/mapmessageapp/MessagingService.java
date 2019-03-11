package edu.temple.mapmessageapp;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService {

    public MessagingService() {
    }

    private final String CHAT_APP_MESSAGE_ACTION = "ksljtfndgrlk3j54hn6k3j4n5trflkdjfg";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Intent messageIntent = new Intent(CHAT_APP_MESSAGE_ACTION);
        messageIntent.putExtra("message", remoteMessage.getData().get("message"));

        Log.d("Received message", remoteMessage.getData().get("message"));


        sendBroadcast(messageIntent);
    }

}
