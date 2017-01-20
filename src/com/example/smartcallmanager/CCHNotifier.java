package com.example.smartcallmanager;

import com.example.smartcallmanager.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class CCHNotifier
{

	private final static int NOTIFICATION_ID = 1;
	
	private Context mContext;
	private NotificationManager mNotificationManager;
	private SharedPreferences mSharedPreferences;
	
	public CCHNotifier(Context context)
	{
		mContext = context;
		mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public void updateNotification()
	{
		if (mSharedPreferences.getBoolean("enabled", false))
		{
			this.enableNotification();
		}
		else
		{
			this.disableNotification();
		}		
	}
	
	@SuppressWarnings("deprecation")
	private void enableNotification()
	{
		Intent notificationIntent = new Intent(mContext, CCHPreferenceActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);

		Notification n = new Notification(R.drawable.ic_launcher, null, 0);
		n.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
		n.setLatestEventInfo(mContext, mContext.getString(R.string.notification_title), mContext.getString(R.string.notification_text), pendingIntent);
		mNotificationManager.notify(NOTIFICATION_ID, n);
	}
	
	private void disableNotification()
	{
		mNotificationManager.cancel(NOTIFICATION_ID);
	}	
}
