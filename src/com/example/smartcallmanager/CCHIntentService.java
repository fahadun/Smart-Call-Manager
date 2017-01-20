package com.example.smartcallmanager;

import java.lang.reflect.Method;
import java.util.List;
import com.android.internal.telephony.ITelephony;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

public class CCHIntentService extends IntentService
{
	private static final String TAG = "CCHIntentService";

	private BluetoothHeadset mBluetoothHeadset;
	private BluetoothAdapter mBluetoothAdapter;
	private Object mProfileListenerObject;

	private com.example.smartcallmanager.BluetoothHeadset mBluetoothHeadsetFahad;
	private boolean mHeadsetOnly = false;
	@SuppressWarnings("unused")
	private boolean mRejection = false;
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate()
	{
		Context context = getBaseContext();
	
		super.onCreate();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		mHeadsetOnly = prefs.getBoolean("headset_only", false);
	
		if (mHeadsetOnly && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB )
		{
			mProfileListenerObject = (BluetoothProfile.ServiceListener)new BluetoothProfile.ServiceListener()
			{
			    public void onServiceConnected(int profile, BluetoothProfile proxy)
			    {
			        if (profile == BluetoothProfile.HEADSET)
			        {
			            mBluetoothHeadset = (BluetoothHeadset) proxy;
			        }
			    }
			    public void onServiceDisconnected(int profile)
			    {
			        if (profile == BluetoothProfile.HEADSET)
			        {
			            mBluetoothHeadset = null;
			        }
			    }
			};	
		}
	}

	public CCHIntentService()
	{
		super("CCHIntentService");
	}

	@SuppressLint("NewApi")
	@Override
	protected void onHandleIntent(Intent intent)
	{
		Context context = getBaseContext();
		List<BluetoothDevice> btDevices;
		boolean returnNow = false;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		if ( mHeadsetOnly )
		{
			if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB )
			{
				mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				if( mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() )
				{
					mBluetoothAdapter.getProfileProxy(this, (BluetoothProfile.ServiceListener)mProfileListenerObject, BluetoothProfile.HEADSET);
				}
				else
				{
					return;
				}
			}
			else
			{
				mBluetoothHeadsetFahad = new com.example.smartcallmanager.BluetoothHeadset(this, null);
			}
		}
		
		try
		{
			Thread.sleep(Integer.parseInt(prefs.getString("delay", "2")) * 1000);
		} catch (InterruptedException e) {}

		if ( mHeadsetOnly )
		{
			if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB )
			{
				String logMessage = "";
				if ( mBluetoothAdapter.isEnabled() == false || mBluetoothHeadset == null )
				{
					returnNow = true;
					logMessage = "onHandleIntent/closeProfileProxy/During waiting for answering, BT may be disabled.";
				}
				else {
					btDevices = mBluetoothHeadset.getConnectedDevices();
					if ( btDevices.isEmpty() ) {
						returnNow = true;
						logMessage = "onHandleIntent/closeProfileProxy/No connected BT devices.";
					}
					else if ( mBluetoothHeadset.getConnectionState(btDevices.get(0)) != BluetoothProfile.STATE_CONNECTED)  {
						returnNow = true;
						logMessage = "onHandleIntent/closeProfileProxy/No connected headset.";
					}
				}
				
				mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);
				
				if( returnNow )
				{
					Log.d(TAG, logMessage);
					return;
				}
	            Log.d(TAG, "onHandleIntent/closeProfileProxy/Headset connected.");
			}
			else {
				if ( mBluetoothHeadsetFahad != null )
				{
					try {
						if ( mBluetoothHeadsetFahad.isConnected(mBluetoothHeadsetFahad.getCurrentHeadset()) == false )
						{
							mBluetoothHeadsetFahad.close();
							return;
						}
						mBluetoothHeadsetFahad.close();
					} catch(Exception e)
					{
						Log.e(TAG, "onHandleIntent/mBluetoothHeadsetFahad : " + e.toString());
						return;
					}
				}
			}
		}
		
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm.getCallState() != TelephonyManager.CALL_STATE_RINGING)
		{
			Log.d(TAG, "onHandleIntent/NO CALL_STATE_RINGING.");
			return;
		}
		
		try {
			answerPhoneAidl(context);			
		}
		catch (Exception e)
		{
			answerPhoneHeadsethook(context);
		}

		if (prefs.getBoolean("use_speakerphone", false))
		{
			enableSpeakerPhone(context);
		}
		return;
	}

	private void enableSpeakerPhone(Context context)
	{
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		audioManager.setSpeakerphoneOn(true);
	}

	private void answerPhoneHeadsethook(Context context)
	{
		Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);		
		buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
		context.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");

		Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);		
		buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
		context.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void answerPhoneAidl(Context context) throws Exception
	{
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		Class c = Class.forName(tm.getClass().getName());
		Method m = c.getDeclaredMethod("getITelephony");
		m.setAccessible(true);
		ITelephony telephonyService;
		telephonyService = (ITelephony)m.invoke(tm);

		telephonyService.silenceRinger();
		telephonyService.answerRingingCall();
	}
}
