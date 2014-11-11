package de.ur.mi.bluetoothlocator.bt;

import java.util.ArrayList;
import java.util.HashMap;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.ViewDebug.FlagToString;
import de.ur.mi.bluetoothlocator.interfaces.BluetoothLocator;

public class BlueTooth {
	
	private BluetoothAdapter adapter;
	private Context context;
	
	public static final String TAG = "BLUETOOTH";
	public static HashMap<String, Long> lastseen = new HashMap<String, Long>();
	
	private ScannerThread thread;
	private Handler threadHandler = new Handler(Looper.getMainLooper()){
		@Override
        public void handleMessage(Message inputMessage) {
			if(scanning){
				adapter.startDiscovery();
			}
		}
	};
	private boolean scanning = false;
	
	private ArrayList<BluetoothLocator> locators = new ArrayList<BluetoothLocator>();
	
	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	            // Get the BluetoothDevice object from the Intent
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            int  rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
            	BTDevice btDevice = new BTDevice(device, rssi);
            	logTime(device.getAddress());
	            for(int i=0; i<locators.size(); i++){
	            	locators.get(i).onBluetoothDeviceLocated(btDevice);
	            }
	        }
	    }
	};
	
	public BlueTooth(Context c){
		context = c;
		adapter = BluetoothAdapter.getDefaultAdapter();
		registerReceiver();
	}
	
	protected void logTime(String address) {
    	if(lastseen.containsKey(address)){
    		//Log.d(TAG, address+" lastseen: "+(System.currentTimeMillis()-lastseen.get(address)));
    	}
    	lastseen.put(address,System.currentTimeMillis());
	}

	private void registerReceiver() {
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		context.registerReceiver(mReceiver, filter);
	}

	public void addLocator(BluetoothLocator btl){
		if(!locators.contains(btl)){
			locators.add(btl);
		}
	}
	
	public boolean removeLocator(BluetoothLocator btl){
		if(!locators.contains(btl)){
			return locators.remove(btl);
		}else{
			return false;
		}
	}
	
	public boolean blueToothSupported(){
		return (adapter != null);
	}
	
	public void enableBluetooth(){
		if (!adapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    context.startActivity(enableBtIntent);
		}
	}
	
	public void startDiscovering(){
		scanning = true;
		thread = new ScannerThread(threadHandler);
		thread.start();
	}
	
	public void stopDiscovering(){
		scanning = false;
		thread.dismiss();
		context.unregisterReceiver(mReceiver);
	}
	
}
