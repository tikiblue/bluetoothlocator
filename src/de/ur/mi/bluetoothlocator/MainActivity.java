package de.ur.mi.bluetoothlocator;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import de.ur.mi.bluetoothlocator.bt.BTDevice;
import de.ur.mi.bluetoothlocator.bt.BTScanner;
import de.ur.mi.bluetoothlocator.bt.BlueTooth;
import de.ur.mi.bluetoothlocator.interfaces.BluetoothLocator;
import de.ur.mi.bluetoothlocator.services.BTService;

public class MainActivity extends Activity implements OnClickListener{
	
	private TextView text;
	private Button go, refresh;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initGUI();
	}

	private void initGUI() {
		text = (TextView)findViewById(R.id.textbox);
		go = (Button)findViewById(R.id.startbutton);
		refresh = (Button)findViewById(R.id.refreshbutton);
		
		go.setOnClickListener(this);
		refresh.setOnClickListener(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		checkService();
		super.onResume();
	}

	private void checkService() {
		if(serviceRunning()){
			go.setText(getString(R.string.stop_scanning));
		}else{
			go.setText(getString(R.string.start_scanning));
		}
	}

	private void updateTextBox(ArrayList<BTDevice> devices) {
		StringBuilder builder = new StringBuilder();
		builder.append(getString(R.string.found_devices));
		for(BTDevice device : devices){
			builder.append("\n"+device.toString());
		}
		text.setText(builder.toString());
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.startbutton:
			toggleService();
			break;
		case R.id.refreshbutton:
			refreshList();
			break;
		}
	}

	private void refreshList() {
		updateTextBox(BTScanner.getAllFoundDevices());
	}

	private void toggleService() {
		if(serviceRunning()){
			stopService();
		}else{
			startService();
		}
		checkService();
	}

	private boolean serviceRunning() {
		return BTService.running;
	}

	private void startService() {
		Intent i= new Intent(this, BTService.class);
		startService(i); 
	}
	
	private void stopService() {
		Intent i= new Intent(this, BTService.class);
		stopService(i);
	}
}
