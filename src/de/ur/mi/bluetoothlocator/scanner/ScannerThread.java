package de.ur.mi.bluetoothlocator.scanner;

import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;

public class ScannerThread extends Thread {

	private Handler handler;
	private boolean running = true;
	private Context context;

	public static final long WAIT_TIME = 500;
	public static final String TAG = "ScannerThread";

	public ScannerThread(Handler h, Context c) {
		handler = h;
		context = c;
	}

	@Override
	public void run() {
		while (running) {
			try {
				Thread.sleep(WAIT_TIME);
				scanWifi();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void scanWifi() {
		WifiManager wManager;
		List<ScanResult> wifiList;
		wManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		wManager.startScan();
		wifiList = wManager.getScanResults();
		Message msg = new Message();
		msg.obj = wifiList;
		handler.dispatchMessage(msg);
	}

	public void dismiss() {
		running = false;
	}

}
