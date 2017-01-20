package android.bluetooth;

import android.bluetooth.BluetoothDevice;

interface IBluetoothHeadset
{
    int getState(in BluetoothDevice device);
    BluetoothDevice getCurrentHeadset();
    boolean connectHeadset(in BluetoothDevice device);
    void disconnectHeadset(in BluetoothDevice device);
    boolean isConnected(in BluetoothDevice device);
    boolean startVoiceRecognition();
    boolean stopVoiceRecognition();
    boolean setPriority(in BluetoothDevice device, int priority);
    int getPriority(in BluetoothDevice device);
    int getBatteryUsageHint();
    
    boolean createIncomingConnect(in BluetoothDevice device);
    boolean acceptIncomingConnect(in BluetoothDevice device);
    boolean rejectIncomingConnect(in BluetoothDevice device);
    boolean cancelConnectThread();
    boolean connectHeadsetInternal(in BluetoothDevice device);
    boolean disconnectHeadsetInternal(in BluetoothDevice device);
}
