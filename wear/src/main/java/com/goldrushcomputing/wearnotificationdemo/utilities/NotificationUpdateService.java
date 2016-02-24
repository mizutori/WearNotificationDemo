package com.goldrushcomputing.wearnotificationdemo.utilities;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.goldrushcomputing.wearnotificationdemo.NotificationActivity;
import com.goldrushcomputing.wearnotificationdemo.MainActivity;
import com.goldrushcomputing.wearnotificationdemo.R;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Takamitsu Mizutori on 11/23/2015.
 */
public class NotificationUpdateService extends WearableListenerService {


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null != intent) {
            String action = intent.getAction();
            if (Constants.ACTION_DISMISS.equals(action)) {
                dismissNotification();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                String path = dataEvent.getDataItem().getUri().getPath();
                //Log.d("NUS", "path is " + path);

                if (path.equals("/data")) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
                    String action = dataMapItem.getDataMap().getString("action");
                    if (action != null && action.equals("notification")) {
                        String type = dataMapItem.getDataMap().getString("type");
                        if (type.equals("oxygen")) {
                            int oxygenRatio = dataMapItem.getDataMap().getInt("oxygenRatio");
                            String title;
                            String message = String.format(this.getString(R.string.oxygen_is_x), oxygenRatio);

                            int iconResId;
                            if (oxygenRatio < 8) {
                                title = this.getString(R.string.Oxygen_Low);
                                iconResId = R.mipmap.down_arrow;
                            } else {
                                title = this.getString(R.string.Oxygen_OK);
                                iconResId = R.mipmap.green_check;
                            }

                            sendOxygenRatioNotification(title, message, iconResId);
                        } else if (type.equals("reminder")) {
                            String stringMessage = dataMapItem.getDataMap().getString("reminderMessage");
                            sendReminderNotification(stringMessage);
                        }
                    }
                }
            }
        }
    }

    private void sendOxygenRatioNotification(String title, String message, int iconResId) {
        Intent notificationIntent = new Intent(this, NotificationActivity.class);
        notificationIntent.putExtra(Constants.DEMO_NOTIFICATION_TITLE, title);
        notificationIntent.putExtra(Constants.DEMO_NOTIFICATION_MESSAGE, message);
        notificationIntent.putExtra(Constants.DEMO_NOTIFICATION_ICON_ID, iconResId);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.WearableExtender wearableExtender = new
                NotificationCompat.WearableExtender()
                .setBackground(BitmapFactory.decodeResource(getResources(), R.mipmap.noti_background))
                .setDisplayIntent(pendingNotificationIntent);

        // this intent will open the activity when the user taps the "open" action on the notification
        Intent viewIntent = new Intent(this, MainActivity.class);
        viewIntent.putExtra("trigger", "oxygenRatioNotification");

        /*PendingIntent.FLAG_UPDATE_CURRENT is to pass the extras including "trigger" info.*/
        PendingIntent pendingViewIntent = PendingIntent.getActivity(this, 0, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // this intent will be sent when the user swipes the notification to dismiss it
        Intent dismissIntent = new Intent(Constants.ACTION_DISMISS);
        PendingIntent pendingDeleteIntent = PendingIntent.getService(this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.notification_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setDeleteIntent(pendingDeleteIntent)
                .setContentIntent(pendingViewIntent)
                .setVibrate(new long[]{500, 250, 500, 250})
                .extend(wearableExtender);

        Notification notification = builder.build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(Constants.OXYGEN_NOTIFICATION_ID, notification);
    }


    private void sendReminderNotification(String message) {
        Alarm alarm = new Alarm();
        alarm.cancelAlarm(this, 123);

        //TODO:Debug
        //calendar.setTimeInMillis(System.currentTimeMillis());
        //calendar.add(Calendar.SECOND, alarmId * 3);

                                    /*
                                    calendar.setTime(alarmDate);
                                    calendar.set(Calendar.HOUR_OF_DAY, 10);
                                    calendar.set(Calendar.MINUTE, 00);
                                    calendar.set(Calendar.SECOND, 00);
                                    */

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 10);
        alarm.setAlarm(this, calendar, message, 123); //alarmId = 123
    }


    private void dismissNotification() {
        new DismissNotificationCommand(this).execute();
    }


}
