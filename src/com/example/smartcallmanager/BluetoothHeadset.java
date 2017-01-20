package com.example.smartcallmanager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.IBluetoothHeadset;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.os.IBinder;
import android.util.Log;

public final class BluetoothHeadset
{

    private static final String TAG = "BluetoothHeadset";

    private IBluetoothHeadset mService;
    private final Context mContext;
    private final ServiceListener mServiceListener;

    public static final int STATE_ERROR        = -1;
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING   = 1;
    public static final int STATE_CONNECTED    = 2;

    public interface ServiceListener
    {
        public void onServiceConnected();
        public void onServiceDisconnected();
    }

    public BluetoothHeadset(Context context, ServiceListener l)
    {
        mContext = context;
        mServiceListener = l;
        if (!context.bindService(new Intent(IBluetoothHeadset.class.getName()), mConnection, 0))
        {
            Log.e(TAG, "Could not bind to Bluetooth Headset Service");
        }
    }

    @Override
	protected void finalize() throws Throwable
	{
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    public synchronized void close()
    {
        if (mConnection != null)
        {
            mContext.unbindService(mConnection);
            mConnection = null;
        }
    }

    public BluetoothDevice getCurrentHeadset()
    {
        if (mService != null)
        {
            try
            {
                return mService.getCurrentHeadset();
            }
            catch (RemoteException e) {Log.e(TAG, e.toString());}
        } 
        else
        {
            Log.w(TAG, "Proxy not attached to service");
        }
        return null;
    }
    
    public boolean isConnected(BluetoothDevice device)
    {
        if (mService != null)
        {
            try
            {
                return mService.isConnected(device);
            } catch (RemoteException e) {Log.e(TAG, e.toString());}
        } 
        else
        {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }  
    
    private ServiceConnection mConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            mService = IBluetoothHeadset.Stub.asInterface(service);
            if (mServiceListener != null) {
                mServiceListener.onServiceConnected();
            }
        }
        public void onServiceDisconnected(ComponentName className)
        {
            mService = null;
            if (mServiceListener != null)
            {
                mServiceListener.onServiceDisconnected();
            }
        }
    };
}
