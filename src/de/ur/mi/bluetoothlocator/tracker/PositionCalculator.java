package de.ur.mi.bluetoothlocator.tracker;

import java.util.List;

import android.net.wifi.ScanResult;
import de.ur.mi.bluetoothlocator.position.WifiPosition;

public class PositionCalculator {
	
	public static final String TAG ="CALC";
	
	public double[] calculateNetworkSimilarities(List<ScanResult> currentPosition, List<WifiPosition> knownPositions){
		if(knownPositions.size()<2)return null;
		double[] similarity = getSimilarities(currentPosition, knownPositions);
		return similarity;
	}

	private double[] getSimilarities(List<ScanResult> currentPosition,
			List<WifiPosition> knownPositions) {
		int max = knownPositions.size();
		double[] result = new double[max];
		for(int i=0; i<max; i++){
			result[i] = getSimilarity(currentPosition, knownPositions.get(i).getWifiReadings());
		}
		return result;
	}

	private double getSimilarity(List<ScanResult> currentPosition,
			List<ScanResult> wifiReadings) {
		int bssidsBothContain = 0;
		double similarity = 0;
		for(ScanResult network : currentPosition){
			ScanResult otherNetwork = getNetwork(wifiReadings, network);
			if(otherNetwork != null){
				bssidsBothContain++;
				similarity += calculateNetworkSimilarity(network, otherNetwork);
			}
		}
		return similarity/bssidsBothContain;
	}

	private double calculateNetworkSimilarity(ScanResult network,
			ScanResult otherNetwork) {
		double level1 = Math.abs(network.level);
		double level2 = Math.abs(otherNetwork.level);
		if(level1>level2){
			return level2/level1;
		}else{
			return level1/level2;
		}
	}

	private ScanResult getNetwork(List<ScanResult> wifiReadings, ScanResult network) {
		for(ScanResult otherNetwork : wifiReadings){
			if(otherNetwork.BSSID.equals(network.BSSID))return otherNetwork;
		}
		return null;
	}

	public double[] reworkSimilarities(double[] sims) {
		double minSimilarity = 1;
		for(int i=0; i<sims.length; i++){
			if(sims[i]<minSimilarity)minSimilarity = sims[i];
		}
		double multi = 1/(1-minSimilarity);
		double[] results = new double[sims.length];
		for(int i=0; i<results.length; i++){
			results[i] = (sims[i]-minSimilarity)*multi;
		}
		return results;
	}
	
}
