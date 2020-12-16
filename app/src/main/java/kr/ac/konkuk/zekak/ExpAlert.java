package kr.ac.konkuk.zekak;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static kr.ac.konkuk.zekak.AppMain.ITEMS;
import static kr.ac.konkuk.zekak.AppMain.ITEM_INFO;

public class ExpAlert extends Service { // 핀 on 되어있는 식제품들에 한해서만
    private static final String CHANNEL_ID = "";
    int notificationId = 9009;

    //https://developer.android.com/training/scheduling/alarms?hl=ko
    // Alarmmanager로 하루에 한번 검사
    private AlarmManager alarmManager;
    PendingIntent trigger;
    long from;


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("ExpAlert","알림 서비스를 시작합니다");

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        from = calendar.getTimeInMillis();      // 오늘 현재 시간 받아옴
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {      // startService()가 호출되면 실행


        trigger = PendingIntent.getService(this, 0, intent, 0);

        // 하루에 한 번 검사
        alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, from, AlarmManager.INTERVAL_DAY, trigger);

        checkExpire();      // 하루마다 호출

        return START_STICKY;
    }

    @Override
    public void onDestroy() {       // stopService()가 호출되면 실행
        super.onDestroy();
        alarmManager.cancel(trigger);       // alarmManager 사용하는 방식은 꼭 이렇게 해줘야지 서비스가 꺼짐
    }


    public void checkExpire(){
        // 전체 아이템 중 3일 이내의 유통기한 잔여일 --> 알림 발생시킴

        Log.i("ExpAlert","알림 서비스를 체크");
        for (int i = 0; i < ITEMS.size(); i++) {
            if (ITEMS.get(i).flag) {      // 핀 기능이 켜져있는 아이템들에 한해서만
                int left = calcRemainingDates(ITEMS.get(i).exp);
                if (left <= 3) {
                    sendNotification(ITEMS.get(i).id, ITEMS.get(i).name, left);
                }
            }
        }
    }



    public void sendNotification(int itemID, String name, int remaining){       // 알림 전송하는 실질적인 함수

        Log.i("ExpAlert","알림 보냄");
        Intent intent = new Intent(this, InfoItem.class);
        intent.putExtra("ITEM_ID", itemID);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, ITEM_INFO, intent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("ZEKAK 유통기한 임박 알림")
                .setContentText("유통기한이 임박한 식품이 있어요! " + name + " (" + remaining + "일 전)")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .setAutoCancel(true)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "ZEKAK", NotificationManager.IMPORTANCE_DEFAULT));
        }

        notificationManager.notify(notificationId, builder.build());
    }

    public int calcRemainingDates(String exp) {     // 남은 일 계산하는 함수, InfoItem.java에서 조금 변형(현재시각 여러번 계산하면 비효율적이므로)
        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
            final int oneDay = 24 * 60 * 60 * 1000;

            Date expDate = dateFormat.parse(exp);

            Calendar due = Calendar.getInstance();
            due.setTime(expDate);

            long expire = due.getTimeInMillis();
            long remaining = (expire - from) / oneDay;

            Log.i("ExpAlert", String.valueOf(remaining)+"일 남은 아이템");

            return (int) remaining;
        }
        catch(ParseException e) {
            e.printStackTrace();
        }

        return -1;
    }

}
