package de.ur.mi.bluetoothlocator.position;

import java.util.List;

import android.graphics.Point;
import android.net.wifi.ScanResult;

public class WifiPosition {

	private List<ScanResult> wifiList;
	private double x, y;
	
	public WifiPosition(Point position, List<ScanResult> wifiReadings){
		this.x = position.x;
		this.y = position.y;
	}
	
	public WifiPosition(double x, double y, List<ScanResult> wifiReadings){
		wifiList = wifiReadings;
		this.x = x;
		this.y = y;
	}
	
	public List<ScanResult> getWifiReadings(){
		return wifiList;
	}
	
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}
}
