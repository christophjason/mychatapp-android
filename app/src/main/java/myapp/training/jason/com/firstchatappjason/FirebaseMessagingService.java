package myapp.training.jason.com.firstchatappjason;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.Tag;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = FirebaseMessagingService.class.getName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "From : " + remoteMessage.getFrom());
        String CHANNEL_ID = "myapp.training.jason.com.firstchatappjason";

        if(remoteMessage.getNotification() != null){

            String notification_title = remoteMessage.getNotification().getTitle();
            String notification_message =  remoteMessage.getNotification().getBody();

            String click_action = remoteMessage.getNotification().getClickAction();

            String from_user_id = remoteMessage.getData().get("from_user_id");

            Intent intent = new Intent(click_action);
            intent.putExtra("uid", from_user_id);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(notification_title)
                    .setContentText(notification_message)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);


            int notificationId = (int) System.currentTimeMillis();

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
            notificationManager.notify(notificationId, mBuilder.build());

        }
    }
}
