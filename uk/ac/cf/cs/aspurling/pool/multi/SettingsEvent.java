package uk.ac.cf.cs.aspurling.pool.multi;

import uk.ac.cf.cs.aspurling.pool.util.Utilities;

public class SettingsEvent extends GameEvent {

	String[] keys;
	Object[] values;
	
	public SettingsEvent(String[] keys) {
		this.keys = keys;
		values = new Object[keys.length];
		for (int i = 0; i < keys.length; i++) {
			values[i] = Utilities.settings.getObject(keys[i], null);
		}
	}
	
	//Store the settings as objects in the settings store
	public void storeSettings() {
		if (keys != null && values != null) {
			for (int i = 0; i < keys.length; i++) {
				Utilities.settings.putObject(keys[i], values[i]);
			}
		}
	}

}
