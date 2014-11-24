package de.ur.mi.bluetoothlocator.tracker;

import java.util.List;

import android.net.wifi.ScanResult;
import android.util.Log;
import de.ur.mi.bluetoothlocator.position.WifiPosition;

public class PositionCalculator {
	
	public static final String TAG ="CALC";
	
	public WifiPosition calculatePosition(List<ScanResult> currentPosition, List<WifiPosition> knownPositions){
		if(knownPositions.size()<4)return null;
		double[] similarity = getSimilarities(currentPosition, knownPositions);
		double[] p = calculatePosition(similarity, knownPositions);
		return new WifiPosition(p[0], p[1], currentPosition);
	}

	@Deprecated
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
		double lowestSimilarity = 100;
		for(int i=0; i<knownPositions.size(); i++){
			for(int j=i+1; j<knownPositions.size(); j++){
				List<ScanResult> wifi1 = knownPositions.get(i).getWifiReadings();
				List<ScanResult> wifi2 = knownPositions.get(j).getWifiReadings();
				double sim = getSimilarity(wifi1, wifi2);
				if(sim < lowestSimilarity)lowestSimilarity = sim;
			}
		}
		double multiplikator = 1/lowestSimilarity;
		// we assume that the first 4 positions stored are as follows:
		// top-left
		// top-right
		// bottom-right
		// bottom-left
		double topLeft = similarity[0];
		double topRight = similarity[1];
		double bottomRight = similarity[2];
		double bottomLeft = similarity[3];
		return new double[]{};
		Log.d(TAG, topLeft+"%, "+topRight+"%, "+bottomRight+"%, "+bottomLeft+"%");
		
		topLeft = multiplikator*(topLeft-lowestSimilarity);
		topRight = multiplikator*(topRight-lowestSimilarity);
		bottomRight = multiplikator*(bottomRight-lowestSimilarity);
		bottomLeft = multiplikator*(bottomLeft-lowestSimilarity);
		// the middle on the y-axis = 50%
		double middleY = .5;
		// the difference from the middle can be calculated by comparing top and bottom
		double difference = (bottomLeft-topLeft + bottomRight-topRight)/2;
		middleY += difference;
		// the middle on the x-axis = 50%
		double middleX = .5;
		// the difference from the middle can be calculated by comparing left and right
		difference = (bottomRight-bottomLeft + topRight-topLeft)/2;
		middleY += difference;
		return new double[]{middleX*100, middleY*100};
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
