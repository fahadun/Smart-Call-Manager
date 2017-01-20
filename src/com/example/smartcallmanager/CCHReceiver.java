package com.example.smartcallmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.TelephonyManager;

public class CCHReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		String phone_state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
		String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

		if(phone_state.equals(TelephonyManager.EXTRA_STATE_RINGING) && prefs.getBoolean("enabled", false))
		{
			if (prefs.getBoolean("no_second_call", false))
			{
				AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
				if (am.getMode() == AudioManager.MODE_IN_CALL)
				{
					return;
				}
			}			

			String which_contacts = prefs.getString("which_contacts", "all");
			if (!which_contacts.equals("all"))
			{
				int is_starred = isStarred(context, number);
				if(which_contacts.equals("contacts") && is_starred < 0)
				{
					return;
				}
				else if(which_contacts.equals("starred") && is_starred < 1)
				{
					return;
				}
			}
			context.startService(new Intent(context, CCHIntentService.class));
		}		
	}
	
	private int isStarred(Context context, String number)
	{
		int starred = -1;
		Cursor c = context.getContentResolver().query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, number),new String[] {PhoneLookup.STARRED},
				null, null, null);
		if (c != null)
		{
			if (c.moveToFirst())
			{
				starred = c.getInt(0);
			}
			c.close();
		}
		return starred;
	}
}
