package de.ur.mi.bluetoothlocator.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import android.net.wifi.ScanResult;
import android.os.Environment;

public class CSVWriter {
	
	public CSVWriter(){
	}
	
	public void writeToFile(String fileName, List<ScanResult> networks, String note) throws IOException{
		String csvString = CSVConverter.convertToCSV(networks, note);
		appendOrCreate(fileName, csvString);
	}
	
	private void appendOrCreate(String fileName, String csvString) throws IOException {
		File file = getFile(fileName);
		if(file.exists()){
			append(file, csvString);
		}else{
			file.createNewFile();
			write(file, csvString);
		}
	}

	private void append(File file, String csvString) throws IOException {
		String content = getStringFromFile(file);
		write(file, content+"\n"+csvString);
	}

	private void write(File file, String csvString) throws IOException {
		FileWriter writer = new FileWriter(file);
		writer.write(csvString);
		writer.flush();
		writer.close();
	}

	private File getFile(String fileName) {
		if(!fileName.toLowerCase().endsWith(".csv"))fileName+=".csv";
		File root = Environment.getExternalStorageDirectory();
		File file = new File(root, fileName);
		return file;
	}
	
	// ----------------------------- FILE READING -------------------------------------
	// http://www.java2s.com/Code/Java/File-Input-Output/ConvertInputStreamtoString.htm
	// (slightly changed)

	public static String convertStreamToString(InputStream is) throws IOException {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    while ((line = reader.readLine()) != null) {
	      sb.append(line).append("\n");
	    }
	    reader.close();
	    return sb.toString();
	}

	public static String getStringFromFile (File fl) throws IOException {
	    FileInputStream fin = new FileInputStream(fl);
	    String ret = convertStreamToString(fin);
	    //Make sure you close all streams.
	    fin.close();        
	    return ret;
	}
	
	// ----------------------------- FILE READING END----------------------------------
}
