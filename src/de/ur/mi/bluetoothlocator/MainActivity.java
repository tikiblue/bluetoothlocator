package de.ur.mi.bluetoothlocator;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import de.ur.mi.bluetoothlocator.bt.ScannerThread;
import de.ur.mi.bluetoothlocator.services.ScanService;

public class MainActivity extends Activity implements OnClickListener{
	
	private TextView text;
	private Button go;
	private EditText ssid;
	
	private String ssidFilter = "";
	
	private TextWatcher tw = new TextWatcher() {
	    public void afterTextChanged(Editable s){
			ssidFilter = ssid.getText().toString();
	    }
	    public void  beforeTextChanged(CharSequence s, int start, int count, int after){}
	    public void  onTextChanged (CharSequence s, int start, int before,int count) {} 
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initGUI();
	}

	private void initGUI() {
		text = (TextView)findViewById(R.id.textbox);
		go = (Button)findViewById(R.id.startbutton);
		ssid = (EditText)findViewById(R.id.ssid);
		
		go.setOnClickListener(this);
		ssid.addTextChangedListener(tw);
		ssidFilter = ssid.getText().toString();
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
			new AsyncUpdater().execute();
		}else{
			go.setText(getString(R.string.start_scanning));
		}
	}

	private void updateTextBox(List<ScanResult> networks) {
		networks = formatList(networks);
		StringBuilder builder = new StringBuilder();
		builder.append(getString(R.string.found_devices)+"\n");
		for(ScanResult network : networks){
			builder.append("\n"+formatNetwork(network));
		}
		text.setText(builder.toString());
	}

	private List<ScanResult> formatList(List<ScanResult> networks) {
		List<ScanResult> result = new ArrayList<ScanResult>();
		for(ScanResult network : networks){
			if(network.SSID.toLowerCase().contains(ssidFilter.toLowerCase())){
				result.add(network);
			}
		}
		return result;
	}

	private String formatNetwork(ScanResult network) {
		return network.SSID+" ("+network.level+")";
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.startbutton:
			toggleService();
			break;
		}
	}

	private void toggleService() {
		if(serviceRunning()){
			stopService();
			go.setText(getString(R.string.start_scanning));
		}else{
			startService();
			go.setText(getString(R.string.stop_scanning));
		}
	}

	private boolean serviceRunning() {
		return ScanService.running;
	}

	private void startService() {
		Intent i= new Intent(this, ScanService.class);
		ScanService.running = true;
		startService(i);
		new AsyncUpdater().execute();
	}
	
	private void stopService() {
		Intent i= new Intent(this, ScanService.class);
		stopService(i);
	}
	
	class AsyncUpdater extends AsyncTask<Object, Object, Object>{

		@Override
		protected void onProgressUpdate(Object... values) {
			super.onProgressUpdate(values);
			updateTextBox(ScanService.getWifiList());			
		}
		
		@Override
		protected Object doInBackground(Object... params) {
			while(ScanService.running){
				try {
					Thread.sleep(ScannerThread.WAIT_TIME);
					publishProgress(null);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
		
	}
}
