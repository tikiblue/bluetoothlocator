package de.ur.mi.bluetoothlocator.services;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import de.ur.mi.bluetoothlocator.scanner.ScannerThread;

public class ScanService extends Service {

	public static boolean running = false;
	private static List<ScanResult> wifiList = new ArrayList<ScanResult>();
	private ScannerThread thread;
	
	private Handler handler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message inputMessage) {
			if (inputMessage.obj instanceof List<?>) {
				wifiList = (List<ScanResult>) inputMessage.obj;
			}
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		running = true;
		thread = new ScannerThread(handler, this);
		thread.start();
	}

	@Override
	public void onDestroy() {
		running = false;
		thread.dismiss();
		super.onDestroy();
	}
	
	public static List<ScanResult> getWifiList(){
		return wifiList;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO for communication return IBinder implementation
		return null;
	}
}
