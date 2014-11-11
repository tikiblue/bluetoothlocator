package de.ur.mi.bluetoothlocator.bt;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import de.ur.mi.bluetoothlocator.interfaces.BluetoothLocator;

public class BTScanner implements BluetoothLocator{

	private BlueTooth bt;
	private static ArrayList<BTDevice> devices = new ArrayList<BTDevice>();
	private Context context;
	
	public static final String TAG = "BTScanner";
	
	public BTScanner(Context c){
		context = c;
	}
	
	@Override
	public void onBluetoothDeviceLocated(BTDevice device) {
		addDeviceToList(device);
		notifyServer(device);
	}

	private void notifyServer(BTDevice device) {
		// TODO Send a message to the database
		Log.d(TAG, "found: "+device.toString());
	}

	private void addDeviceToList(BTDevice device) {
		if(devices.contains(device)){
			int index = devices.indexOf(device);
			devices.get(index).update(device);
		}else{
			devices.add(device);
		}
	}

	public boolean startBlueTooth() {
		bt = new BlueTooth(context);
		bt.addLocator(this);
		if(bt.blueToothSupported()){
			bt.enableBluetooth();
			return true;
		}else{
			return false;
		}
	}
	
	public void startDiscovering(){
		bt.startDiscovering();
	}
	
	public void stop(){
		bt.stopDiscovering();
	}
	
	public static ArrayList<BTDevice> getAllFoundDevices(){
		return devices;
	}
}
