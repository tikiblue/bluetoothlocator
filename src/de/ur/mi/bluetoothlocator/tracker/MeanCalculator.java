package de.ur.mi.bluetoothlocator.tracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.net.wifi.ScanResult;

public class MeanCalculator {

	/**
	 * calculates the average of the scans levels
	 * 
	 * @param scans
	 * @return
	 */
	public static List<ScanResult> calculateList(List<List<ScanResult>> scans) {
		HashMap<String, ScanResult> results = new HashMap<String, ScanResult>();
		for (List<ScanResult> scan : scans) {
			for (ScanResult scanresult : scan) {
				String key = scanresult.BSSID;
				if (!results.containsKey(key)) {
					scanresult.level = calculateMean(scanresult.BSSID, scans);
					results.put(key, scanresult);
				}
			}
		}

		return convertToArrayList(results);
	}

	/**
	 * converts a hashmap to an arraylist (losing the keys)
	 * 
	 * @param results
	 * @return
	 */
	public static List<ScanResult> convertToArrayList(
			HashMap<String, ScanResult> results) {
		List<ScanResult> resultArray = new ArrayList<ScanResult>();

		Set<String> bssids = results.keySet();
		Iterator<String> i = bssids.iterator();
		while (i.hasNext()) {
			String key = i.next();
			resultArray.add(results.get(key));
		}
		return resultArray;
	}

	/**
	 * calculates the mean level of all networks in the given list with the
	 * given bssid
	 * 
	 * @param bssid
	 * @param scans
	 * @return
	 */
	public static int calculateMean(String bssid, List<List<ScanResult>> scans) {
		ArrayList<Integer> levels = new ArrayList<Integer>();
		for (List<ScanResult> scan : scans) {
			for (ScanResult scanresult : scan) {
				if (scanresult.BSSID.equals(bssid)) {
					levels.add(scanresult.level);
				}
			}
		}
		int mean = 0;
		for (Integer i : levels) {
			mean += i;
		}
		mean = mean / levels.size();
		return mean;
	}

}
