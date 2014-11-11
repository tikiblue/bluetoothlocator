package de.ur.mi.bluetoothlocator.services;

import de.ur.mi.bluetoothlocator.bt.BTScanner;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class BTService extends Service {

	public static boolean running = false;
	private BTScanner scanner;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		running = true;
		scanner = new BTScanner(this);
		if (scanner.startBlueTooth()) {
			scanner.startDiscovering();
		}
	}

	@Override
	public void onDestroy() {
		running = false;
		scanner.stop();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO for communication return IBinder implementation
		return null;
	}
}
