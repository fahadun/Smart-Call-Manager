package com.example.smartcallmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CCHBootReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		CCHNotifier notifier = new CCHNotifier(context);
		notifier.updateNotification();
	}

}
