package kr.ac.konkuk.zekak;

import android.app.PendingIntent;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ExpAlert {
    String product;
    int remaining;

//    Intent intent = new Intent(this, AlertDetails.class);
//    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
//
//    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
//            .setSmallIcon(R.drawable.manual_add_icon)  // TODO: 아이콘 바꾸기
//            .setContentTitle("ZEKAK 유통기한 임박 알림")
//            .setContentText("유통기한이 임박한 식품이 있어요"+product+"("+remaining+"일 전)");
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            // Set the intent that will fire when the user taps the notification
//            .setContentIntent(pendingIntent)
//            .setAutoCancel(true);
//
//    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//
//    // notificationId is a unique int for each notification that you must define
//    notificationManager.notify(notificationId, builder.build());


}
