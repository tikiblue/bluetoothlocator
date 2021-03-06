package de.ur.mi.bluetoothlocator;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import de.ur.mi.bluetoothlocator.data.ListFilter;
import de.ur.mi.bluetoothlocator.position.WifiPosition;
import de.ur.mi.bluetoothlocator.scanner.ScannerThread;
import de.ur.mi.bluetoothlocator.services.ScanService;
import de.ur.mi.bluetoothlocator.tracker.MeanCalculator;
import de.ur.mi.bluetoothlocator.tracker.PositionCalculator;
import de.ur.mi.bluetoothlocator.views.PercentageClickListener;
import de.ur.mi.bluetoothlocator.views.PositionView;

public class TrackActivity extends Activity implements OnClickListener,
		PercentageClickListener {

	private Button go;
	private PositionView position;
	private PositionCalculator calc;
	private List<WifiPosition> knownPositions = new ArrayList<WifiPosition>();
	
	private boolean updaterRunning = true;
	
	private AsyncUpdater updater = new AsyncUpdater();

	private String ssidFilter = "";

	public static final String TAG = "TRACKER";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tracker);
		calc = new PositionCalculator();
		ssidFilter = getIntent().getStringExtra("ssid");
		initGUI();
		updater.execute();
	}

	private void initGUI() {
		go = (Button) findViewById(R.id.startbutton);
		position = (PositionView) findViewById(R.id.positionview);
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
		if (serviceRunning()) {
			go.setText(getString(R.string.stop_scanning));
			updater = new AsyncUpdater();
			updater.execute();
		} else {
			go.setText(getString(R.string.start_scanning));
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.startbutton:
			toggleService();
			break;
		}
	}

	private void toggleService() {
		if (serviceRunning()) {
			stopService();
			go.setText(getString(R.string.start_scanning));
		} else {
			startService();
			go.setText(getString(R.string.stop_scanning));
		}
	}

	private void updatePosition(List<ScanResult> wifiList) {
		wifiList = ListFilter.filterList(wifiList, ssidFilter);
		double[] pos = calc.calculateNetworkSimilarities(wifiList,
				knownPositions);
		if (pos == null)
			return;
		pos = calc.reworkSimilarities(pos);
		Log.d(TAG, "Similarities:");
		for (Double d : pos) {
			Log.d(TAG, "sim: " + d);
		}
		position.setPoints(knownPositions, pos);
		position.invalidate();
	}

	private boolean serviceRunning() {
		return ScanService.running;
	}

	private void startService() {
		Intent i = new Intent(this, ScanService.class);
		ScanService.running = true;
		startService(i);
		updater = new AsyncUpdater();
		updater.execute();
	}

	private void stopService() {
		Intent i = new Intent(this, ScanService.class);
		stopService(i);
	}

	@Override
	public void onPercentageClicked(float x, float y) {
		Log.d(TAG, "clicked on: " + x + ", " + y);
		updater.cancel(true);
		new AsyncTracker(this, x, y).execute();
	}

	class AsyncUpdater extends AsyncTask<Object, Object, Object> {

		@Override
		protected void onPreExecute() {
			updaterRunning = true;
			super.onPreExecute();
		}
		
		@Override
		protected void onProgressUpdate(Object... values) {
			super.onProgressUpdate(values);
			updatePosition(ScanService.getMeanWifiList());
		}

		@Override
		protected Object doInBackground(Object... params) {
			while (ScanService.running && updaterRunning) {
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

	class AsyncTracker extends AsyncTask<Object, Object, Object> {

		private ProgressDialog pd;
		private Context c;
		private float x, y;
		private ArrayList<List<ScanResult>> scans = new ArrayList<List<ScanResult>>();

		public AsyncTracker(Context c, float x, float y) {
			this.c = c;
			this.x = x;
			this.y = y;
		}

		@Override
		protected void onPreExecute() {
			updaterRunning = false;
			pd = new ProgressDialog(c);
			pd.setTitle(R.string.tracking);
			pd.setCancelable(false);
			pd.setProgress(0);
			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pd.setIndeterminate(false);
			pd.show();
			updater.cancel(true);
			super.onPreExecute();
		}
		
		@Override
		protected void onProgressUpdate(Object... values) {
			int progress = pd.getProgress();
			progress += 100/ScannerThread.MEAN_STEPS;
			pd.setProgress(progress);
		}

		@Override
		protected Object doInBackground(Object... params) {
			try {
				for (int i = 0; i < ScannerThread.MEAN_STEPS; i++) {
					List<ScanResult> scan = ScanService.getWifiList();
					scan = ListFilter.filterList(scan, ssidFilter);
					scans.add(scan);
					Thread.sleep(ScannerThread.WAIT_TIME);
					publishProgress();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			pd.dismiss();
			List<ScanResult> wifiList = MeanCalculator.calculateList(scans);
			WifiPosition currentPosition = new WifiPosition(x, y, wifiList);
			knownPositions.add(currentPosition);
			
			if(ScanService.running){
				updater = new AsyncUpdater();
				updater.execute();
			}
			
			super.onPostExecute(result);
		}

	}
}
