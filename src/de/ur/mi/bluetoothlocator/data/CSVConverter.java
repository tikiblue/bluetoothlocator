package de.ur.mi.bluetoothlocator.data;

import java.util.List;

import android.net.wifi.ScanResult;

public class CSVConverter {

	//private static final String TAG = "CSVWriter";

	public static String convertToCSV(List<ScanResult> networks, String note){
		StringBuilder result = new StringBuilder();
		for(ScanResult scan : networks){
			result.append(formatScan(scan));
			if(note != null){
				result.append(note);
			}
			result.append("\n");
		}
		return result.toString();
	}

	/**
	 * timestamp; SSID; level; BSSID; frequency;
	 */
	private static String formatScan(ScanResult scan) {
		StringBuilder result = new StringBuilder();
		result.append(System.currentTimeMillis());
		result.append("; ");
		result.append(scan.SSID);
		result.append("; ");
		result.append(scan.level);
		result.append("; ");
		result.append(scan.BSSID);
		result.append("; ");
		result.append(scan.frequency);
		result.append("; ");
		return result.toString();
	}
	
}
