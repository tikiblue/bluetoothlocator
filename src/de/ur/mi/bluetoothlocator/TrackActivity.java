package de.ur.mi.bluetoothlocator;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import de.ur.mi.bluetoothlocator.position.WifiPosition;
import de.ur.mi.bluetoothlocator.scanner.ScannerThread;
import de.ur.mi.bluetoothlocator.services.ScanService;
import de.ur.mi.bluetoothlocator.tracker.PositionCalculator;
import de.ur.mi.bluetoothlocator.views.PercentageClickListener;
import de.ur.mi.bluetoothlocator.views.PositionView;

public class TrackActivity extends Activity implements OnClickListener, PercentageClickListener{

	private Button go;
	private PositionView position;
	private PositionCalculator calc;
	private List<WifiPosition> knownPositions = new ArrayList<WifiPosition>();
	
	public static final String TAG = "TRACKER";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tracker);
		calc = new PositionCalculator();
		initGUI();
		new AsyncUpdater().execute();
	}

	private void initGUI() {
		go = (Button)findViewById(R.id.startbutton);
		position = (PositionView)findViewById(R.id.positionview);
		go.setOnClickListener(this);
		position.addPercentageClickListener(this);
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

	private void updatePosition(List<ScanResult> wifiList) {
		double[] pos = calc.calculateNetworkSimilarities(wifiList, knownPositions);
		if(pos == null)return;
		pos = calc.reworkSimilarities(pos);
		Log.d(TAG, "Similarities:");
		for(Double d:pos){
			Log.d(TAG, "sim: "+d);
		}
		position.setPoints(knownPositions, pos);
		position.invalidate();
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
			updatePosition(ScanService.getWifiList());
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

	@Override
	public void onPercentageClicked(float x, float y) {
		Log.d(TAG, "clicked on: "+x+", "+y);
		WifiPosition currentPosition = new WifiPosition(x, y, ScanService.getWifiList());
		knownPositions.add(currentPosition);
	}
}
