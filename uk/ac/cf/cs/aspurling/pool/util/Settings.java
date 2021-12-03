package uk.ac.cf.cs.aspurling.pool.util;
//Class for loading and saving settings. The class can save and load
//booleans, ints, floats, doubles and Strings to a specified file

import java.io.ObjectOutput; 
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

@SuppressWarnings("unchecked")
public class Settings {

	private String settingsFile;
	private Map hashTable;
	
	public Settings(String file) {
		settingsFile = file;
		File theFile = new File(settingsFile);
		if (theFile.exists()) {
			try {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		        // Deserialize the object
		        hashTable = (Map)in.readObject();
		        in.close();
			}catch (IOException e) {
		    	System.err.println("Error while loading settings");
			}catch (ClassNotFoundException e) {
				System.err.println("Error loading settings object");
			}
		}else{
			hashTable = new HashMap(16);
		}
	}
	

	public void putBoolean(String key, boolean bit) {
		hashTable.put(key, new Boolean(bit));
	}
	
	public void putInt(String key, int num) {
		hashTable.put(key, new Integer(num));
	}
	
	public void putFloat(String key, float num) {
		hashTable.put(key, new Float(num));
	}
	
	public void putDouble(String key, double num) {
		hashTable.put(key, new Double(num));
	}
	
	public void putString(String key, String str) {
		hashTable.put(key, str);
	}
	
	public void putObject(String key, Object obj) {
		hashTable.put(key, obj);
	}
	
	public boolean getBoolean(String key, boolean def) {
		try {
			return ((Boolean)hashTable.get(key)).booleanValue();
		}catch (Exception e) {
			return def;
		}
	}
	
	public int getInt(String key, int def) {
		try {
			return ((Integer)hashTable.get(key)).intValue();
		}catch (Exception e) {
			return (int)getFloat(key, (float)def);	//if not found try finding a float
		}
	}
	
	public float getFloat(String key, float def) {
		try {
			return ((Float)hashTable.get(key)).floatValue();
		}catch (Exception e) {
			return def;
		}
	}
	
	public String getString(String key, String def) {
		if (hashTable.containsKey(key)) {
			return ((String)hashTable.get(key));
		}else{
			return def;
		}
	}
	
	public Object getObject(String key, Object def) {
		if (hashTable.containsKey(key)) {
			return hashTable.get(key);
		}else{
			return def;
		}
	}
	
	public void saveSettings() {
		try {
	        // Serialize to a file
	        ObjectOutput out = new ObjectOutputStream(new FileOutputStream(settingsFile));
	        out.writeObject(hashTable);
	        out.close();
	    } catch (IOException e) {
	    	System.err.println("Error while saving settings");
	    }
	}


}
