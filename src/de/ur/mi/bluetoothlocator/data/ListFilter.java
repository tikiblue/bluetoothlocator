package de.ur.mi.bluetoothlocator.data;

import java.util.ArrayList;
import java.util.List;

import android.net.wifi.ScanResult;

public class ListFilter {

	public static List<ScanResult> filterList(List<ScanResult> networks, String filter) {
		if(filter == null || filter.trim().length() == 0){
			return networks;
		}
		List<ScanResult> result = new ArrayList<ScanResult>();
		for(ScanResult network : networks){
			if(network.SSID.toLowerCase().contains(filter.toLowerCase())){
				result.add(network);
			}
		}
		return result;
	}
	
}
