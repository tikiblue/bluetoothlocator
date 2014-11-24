package de.ur.mi.bluetoothlocator.tracker;

import java.util.List;

import android.net.wifi.ScanResult;
import de.ur.mi.bluetoothlocator.position.WifiPosition;

public class PositionCalculator {
	
	public WifiPosition calculatePosition(List<ScanResult> currentPosition, List<WifiPosition> knownPositions){
		if(knownPositions.size()<3)return null;
		double[] similarity = getSimilarities(currentPosition, knownPositions);
		double[] p = calculatePosition(similarity, knownPositions);
		double[] result = normalizePosition(similarity, p);
		return new WifiPosition(result[0], result[1], currentPosition);
	}

	private double[] normalizePosition(double[] similarity, double[] p) {
		double normalizationValue = 0;
		for(int i=0; i<similarity.length; i++){
			normalizationValue += similarity[i];
		}
		int x = (int)(p[0] / normalizationValue);
		int y = (int)(p[1] / normalizationValue);
		return new double[]{x, y};
	}

	private double[] calculatePosition(double[] similarity,
			List<WifiPosition> knownPositions) {
		double x = 0;
		double y = 0;
		for(int i=0; i<knownPositions.size(); i++){
			x += knownPositions.get(i).getX()*similarity[i];
			y += knownPositions.get(i).getY()*similarity[i];
		}
		return new double[]{x, y};
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
	
}
