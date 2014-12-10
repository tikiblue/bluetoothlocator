package de.ur.mi.bluetoothlocator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import de.ur.mi.bluetoothlocator.data.CSVWriter;
import de.ur.mi.bluetoothlocator.data.ListFilter;
import de.ur.mi.bluetoothlocator.position.CalibrationData;
import de.ur.mi.bluetoothlocator.scanner.ScannerThread;
import de.ur.mi.bluetoothlocator.tracker.MeanCalculator;
import de.ur.mi.bluetoothlocator.web.JsonReader;

public class CalibrateActivity extends Activity implements OnClickListener {

	public static final String DEFAULT_URL = "http://132.199.139.24/~baa56852/fil/nav/calibration_data.json";

	private Button next;
	private TextView progressText, infoText;
	private ProgressBar progress;

	private String ssidFilter = "";

	private int numberOfScansDone = 0;

	private CalibrationData calibrationData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calibrate);
		initGUI();
		fetchData();
	}

	private void fetchData() {
		ssidFilter = getIntent().getStringExtra("ssid");
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		String url = sp.getString("calibrationurl", DEFAULT_URL);
		new AsyncDownloader().execute(url);
	}

	private void initGUI() {
		next = (Button) findViewById(R.id.okbutton);
		progressText = (TextView) findViewById(R.id.progresstext);
		infoText = (TextView) findViewById(R.id.task);
		progress = (ProgressBar) findViewById(R.id.progress);

		next.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.okbutton:
			new AsyncTracker(this).execute();
			break;
		}
	}

	private void prepareScanning() {
		List<Integer> ids = calibrationData.getIDs();
		List<ScanResult> scan = ScannerThread.scanWifi(this);
		scan = ListFilter.filterList(scan, ssidFilter);
		getSSIDs(ids, scan);
	}

	private void enableScanning() {
		next.setEnabled(true);
		nextScan();
	}

	private void getSSIDs(final List<Integer> ids, final List<ScanResult> scan) {
		AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
		final Integer id = ids.remove(0);
		String title = getString(R.string.setssid).replace("$1", id + "");
		builderSingle.setTitle(title);
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				this, android.R.layout.select_dialog_singlechoice);
		for (ScanResult s : scan) {
			arrayAdapter.add(s.SSID);
		}

		builderSingle.setAdapter(arrayAdapter,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						calibrationData.setSSID(id, scan.remove(which).SSID);
						if (ids.size() == 0 || scan.size() == 0) {
							enableScanning();
						} else {
							getSSIDs(ids, scan);
						}
					}
				});
		builderSingle.show();
	}

	private void nextScan() {

		if (numberOfScansDone == calibrationData.getNumberOfScans()) {
			progress.setProgress(100);
			saveScanData();
			return;
		}

		String progressTxt = (numberOfScansDone + 1) + "/"
				+ calibrationData.getNumberOfScans();
		int percentage = (100 * numberOfScansDone)
				/ calibrationData.getNumberOfScans();
		progressText.setText(progressTxt);
		progress.setProgress(percentage);

		try {
			String position = calibrationData
					.getPositionDescription(numberOfScansDone);
			String task = getString(R.string.gotoposition).replace("$1",
					position);
			infoText.setText(task);
		} catch (JSONException e) {
			e.printStackTrace();
			String message = getString(R.string.jsonerror);
			Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT)
					.show();
			finish();
		}
	}

	private void saveScanData() {
		next.setEnabled(false);
		new AsyncSaver().execute();
	}

	private class AsyncDownloader extends AsyncTask<String, Object, JSONObject> {

		@Override
		protected JSONObject doInBackground(String... params) {
			if (params == null || params.length == 0) {
				return null;
			}
			String url = params[0];
			try {
				JSONObject result = JsonReader.readJsonFromUrl(url);
				return result;
			} catch (IOException | JSONException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (result == null) {
				String message = getString(R.string.downloaderror);
				Toast.makeText(getApplicationContext(), message,
						Toast.LENGTH_SHORT).show();
				finish();
			} else {
				calibrationData = new CalibrationData(result);
				prepareScanning();
			}
		}

	}

	class AsyncTracker extends AsyncTask<Object, Object, Object> {

		private ProgressDialog pd;
		private Context c;
		private ArrayList<List<ScanResult>> scans = new ArrayList<List<ScanResult>>();

		public AsyncTracker(Context c) {
			this.c = c;
		}

		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(c);
			pd.setTitle(R.string.tracking);
			pd.setCancelable(false);
			pd.setProgress(0);
			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pd.setIndeterminate(false);
			pd.show();
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Object... values) {
			int progress = pd.getProgress();
			progress += 100 / ScannerThread.MEAN_STEPS;
			pd.setProgress(progress);
		}

		@Override
		protected Object doInBackground(Object... params) {
			try {
				for (int i = 0; i < ScannerThread.MEAN_STEPS; i++) {
					List<ScanResult> scan = ScannerThread.scanWifi(c);
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
			try {
				calibrationData.setScanResults(numberOfScansDone, wifiList);
			} catch (JSONException e) {
				e.printStackTrace();
				String message = getString(R.string.jsonerror);
				Toast.makeText(getApplicationContext(), message,
						Toast.LENGTH_SHORT).show();
			}
			numberOfScansDone++;
			nextScan();
			super.onPostExecute(result);
		}

	}

	class AsyncSaver extends AsyncTask<Object, Object, String> {

		protected void onPreExecute() {
			infoText.setText(R.string.saving);
		}

		@Override
		protected String doInBackground(Object... params) {
			String filename = "WLAN_" + calibrationData.getRoomName() + ".json";
			String content = calibrationData.getData().toString();
			File root = Environment.getExternalStorageDirectory();
			File file = new File(root, filename);
			try {
				CSVWriter.write(file, content);
				return content;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if (result == null) {
				String message = getString(R.string.saveerror);
				Toast.makeText(getApplicationContext(), message,
						Toast.LENGTH_SHORT).show();
				finish();
			} else {
				infoText.setText(R.string.success);
				super.onPostExecute(result);
			}
		}
	}

}
