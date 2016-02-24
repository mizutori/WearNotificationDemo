package com.goldrushcomputing.wearnotificationdemo.utilities;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.goldrushcomputing.wearnotificationdemo.NotificationActivity;
import com.goldrushcomputing.wearnotificationdemo.MainActivity;
import com.goldrushcomputing.wearnotificationdemo.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Takamitsu Mizutori on 01/22/2016.
 */
public class Alarm extends BroadcastReceiver{
	private static final int HELLO_ID = 1;
	static final String TAG = "Alarm";


	public void onReceive(Context context, Intent intent){
		String title = intent.getStringExtra("title");
		// Put here YOUR code.
		//Toast.makeText(context, "Today's run is " + title, Toast.LENGTH_SHORT).show(); // For example
		Log.e(TAG, "onReceive called");

		sendReminderNotification(context, title);
	}

	private void sendReminderNotification(Context context, String title) {
		Intent notificationIntent = new Intent(context, NotificationActivity.class);
		notificationIntent.putExtra(Constants.DEMO_NOTIFICATION_TITLE, context.getResources().getString(R.string.Reminder));
		notificationIntent.putExtra(Constants.DEMO_NOTIFICATION_MESSAGE, title);
		PendingIntent pendingNotificationIntent = PendingIntent.getActivity(
				context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.WearableExtender wearableExtender = new
				NotificationCompat.WearableExtender()
				.setBackground(BitmapFactory.decodeResource(context.getResources(), R.mipmap.noti_background))
				.setDisplayIntent(pendingNotificationIntent);

		// this intent will open the activity when the user taps the "open" action on the notification
		Intent viewIntent = new Intent(context, MainActivity.class);
		viewIntent.putExtra("trigger", "reminderNotification");

        /*PendingIntent.FLAG_UPDATE_CURRENT is to pass the extras including "trigger" info.*/
		PendingIntent pendingViewIntent = PendingIntent.getActivity(context, 0, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		// this intent will be sent when the user swipes the notification to dismiss it
		Intent dismissIntent = new Intent(Constants.ACTION_DISMISS);
		PendingIntent pendingDeleteIntent = PendingIntent.getService(context, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.mipmap.notification_icon)
				.setContentTitle(context.getString(R.string.Reminder))
				.setContentText(title)
				.setDeleteIntent(pendingDeleteIntent)
				.setContentIntent(pendingViewIntent)
				.setVibrate(new long[]{500, 250, 500, 250})
				.extend(wearableExtender);

		Notification notification = builder.build();

		NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
		notificationManagerCompat.notify(Constants.REMINDER_NOTIFICATION_ID, notification);
	}


	public void setAlarm(Context context, Calendar calendar, String title, int alarmId)
	{
		AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, Alarm.class);

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
		String timeInString = formatter.format(calendar.getTime());
		i.setData(Uri.parse("wearnotificationdemo://" + alarmId));
		i.setAction("" + alarmId);
		i.putExtra("title", title);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
		am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
	}

	public void cancelAlarm(Context context)
	{
		Intent intent = new Intent(context, Alarm.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
	}

	public void cancelAlarm(Context context, int alarmId)
	{
		Intent i = new Intent(context, Alarm.class);
		i.setData(Uri.parse("wearnotificationdemo://" + alarmId));
		i.setAction("" + alarmId);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, i, 0);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
	}

}



