package de.ur.mi.bluetoothlocator.bt;

import android.os.Handler;
import android.os.Message;


public class ScannerThread extends Thread{
	
	private Handler handler;
	private boolean running = true;
	
	public static final long WAIT_TIME = 1000;
	
	public ScannerThread(Handler handler){
		this.handler = handler;
	}
	
	@Override
	public void run() {
		while(running){
			try {
				Thread.sleep(WAIT_TIME);
				Message msg = new Message();
				handler.dispatchMessage(msg);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void dismiss(){
		running = false;
	}

}
