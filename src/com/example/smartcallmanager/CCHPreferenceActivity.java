package com.example.smartcallmanager;

import com.example.smartcallmanager.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class CCHPreferenceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
private CCHNotifier mNotifier;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mNotifier = new CCHNotifier(this);
		mNotifier.updateNotification();
		SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		addPreferencesFromResource(R.xml.preferences);

	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onDestroy()
	{
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if (key.equals("enabled"))
		{
			mNotifier.updateNotification();
		}
		if (key.equals("headset_only"))
		{
			boolean isEnabled = sharedPreferences.getBoolean(key, true);
			getPreferenceScreen().findPreference("use_speakerphone").setEnabled(isEnabled);
			getPreferenceScreen().findPreference("use_speakerphone").setEnabled(!isEnabled);
		}
	}	
	
}
