package de.ur.mi.bluetoothlocator.position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.wifi.ScanResult;

public class CalibrationData {

	private JSONObject raw;
	private HashMap<Integer, String> ssids = new HashMap<Integer, String>();
	
	/**
	 * Class with all the keys used in the JSON file
	 * @author Basti
	 *
	 */
	public static class Keys {
		public static final String ROOM = "room";
		public static final String NAME = "description";
		public static final String WIDTH = "gridWidth";
		public static final String HEIGHT = "gridHeight";
		public static final String DATA = "calibration_data";
		public static final String X = "x";
		public static final String Y = "y";
		public static final String SENSORS = "sensors";
		public static final String ID = "id";
		public static final String LEVEL = "avg_value";
	}
	
	public CalibrationData(JSONObject rawData){
		raw = rawData;
	}
	
	public JSONObject getData(){
		return raw;
	}
	
	public String getRoomName(){
		try {
			return raw.getJSONObject(Keys.ROOM).getString(Keys.NAME);
		} catch (JSONException e) {
			e.printStackTrace();
			return "N/A";
		}
	}
	
	public int getNumberOfScans(){
		try{
			int width = raw.getJSONObject(Keys.ROOM).getInt(Keys.WIDTH);
			int height = raw.getJSONObject(Keys.ROOM).getInt(Keys.HEIGHT);
			return width*height;
		}catch(JSONException e){
			e.printStackTrace();
			return 0;
		}
	}
	
	public String getPositionDescription(int numberOfScan) throws JSONException{
		JSONObject scan = getScan(numberOfScan);
		int x = scan.getInt(Keys.X);
		int y = scan.getInt(Keys.Y);
		return "("+x+"/"+y+")";
	}
	
	public JSONObject getScan(int numberOfScan) throws JSONException{
		return raw.getJSONObject(Keys.ROOM).getJSONArray(Keys.DATA).getJSONObject(numberOfScan);
	}

	public void setScanResults(int numberOfScan, List<ScanResult> list) throws JSONException {
		JSONObject scan = getScan(numberOfScan);
		JSONArray sensors = scan.getJSONArray(Keys.SENSORS);
		for(int j=0; j<sensors.length(); j++){
			JSONObject sensor = sensors.getJSONObject(j);
			Integer id = sensor.getInt(Keys.ID);
			int level = getLevelOfSSID(getSSID(id), list);
			if(level<0){
				sensor.put(Keys.LEVEL, level);
			}
		}
	}

	private int getLevelOfSSID(String ssid, List<ScanResult> list) {
		for(ScanResult scan : list){
			if(scan.SSID.toLowerCase().equals(ssid.toLowerCase())){
				return scan.level;
			}
		}
		return 0;
	}

	public List<Integer> getIDs(){
		ArrayList<Integer> result = new ArrayList<Integer>();
		int max = getNumberOfScans();
		for(int i=0; i<max; i++){
			try {
				JSONObject scan = getScan(i);
				JSONArray sensors = scan.getJSONArray(Keys.SENSORS);
				for(int j=0; j<sensors.length(); j++){
					JSONObject sensor = sensors.getJSONObject(j);
					Integer id = sensor.getInt(Keys.ID);
					if(!result.contains(id)){
						result.add(id);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public void setSSID(int id, String ssid){
		ssids.put(id, ssid);
	}
	
	public String getSSID(int id){
		if(ssids.containsKey(id)){
			return ssids.get(id);
		}else{
			return "N/A";
		}
	}
}
