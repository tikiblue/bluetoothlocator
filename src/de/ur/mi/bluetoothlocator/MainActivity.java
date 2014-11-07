package de.ur.mi.bluetoothlocator;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import de.ur.mi.bluetoothlocator.bt.BTDevice;
import de.ur.mi.bluetoothlocator.bt.BlueTooth;
import de.ur.mi.bluetoothlocator.bt.BluetoothLocator;

public class MainActivity extends Activity implements BluetoothLocator, OnClickListener{
	
	private BlueTooth bt;
	private TextView text;
	private Button go;
	private ArrayList<BTDevice> devices = new ArrayList<BTDevice>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initGUI();
		startBlueTooth();
	}

	private void initGUI() {
		text = (TextView)findViewById(R.id.textbox);
		go = (Button)findViewById(R.id.startbutton);
		
		go.setOnClickListener(this);
	}

	private void startBlueTooth() {
		bt = new BlueTooth(this);
		bt.addLocator(this);
		if(bt.blueToothSupported()){
			bt.enableBluetooth();
		}else{
			this.finish();
		}
	}
	
	@Override
	protected void onDestroy() {
		bt.stopDiscovering();
		super.onDestroy();
	}

	@Override
	public void onBluetoothDeviceLocated(BTDevice device) {
		addDeviceToList(device);
		updateTextBox();
	}

	private void addDeviceToList(BTDevice device) {
		if(devices.contains(device)){
			int index = devices.indexOf(device);
			devices.get(index).update(device);
		}else{
			devices.add(device);
		}
	}

	private void updateTextBox() {
		StringBuilder builder = new StringBuilder();
		builder.append(getString(R.string.found_devices));
		for(BTDevice device : devices){
			builder.append("\n"+device.toString());
		}
		text.setText(builder.toString());
	}

	@Override
	public void onClick(View v) {
		bt.startDiscovering();
		go.setEnabled(false);
	}
}
