package de.ur.mi.bluetoothlocator.bt;

import android.bluetooth.BluetoothDevice;

public class BTDevice {

	public BluetoothDevice raw;
	private int strength = 0; 
	
	protected BTDevice(BluetoothDevice device, int rssi){
		raw = device;
		strength = rssi;
	}
	
	public int getSignalStrength(){
		return strength;
	}
	
	public String toString(){
		return raw.getName()+" ("+raw.getAddress()+")\t"+strength;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof BTDevice){
			return raw.getAddress().equals(((BTDevice)o).raw.getAddress());
		}
		return super.equals(o);
	}

	public void update(BTDevice device) {
		this.raw = device.raw;
		this.strength = device.strength;
	}
	
}
