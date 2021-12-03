package uk.ac.cf.cs.aspurling.pool.menu;
import uk.ac.cf.cs.aspurling.pool.util.Utilities;
import java.text.DecimalFormat;

//This menu item allows a selection to be enabled and disabled
public class MenuItemValue extends MenuItem {
	
	private String itemLabel;
	private String itemText;
	private double value;
	private float min;
	private float max;
	private double increment;

	public MenuItemValue(String text, MenuManager menu, int fontNum,
			int height, String action, float min, float max) {
		this(text, menu, fontNum, height, action, min, max, false);
	}

	public MenuItemValue(String text, MenuManager menu, int fontNum,
			int height, String action, float min, float max, boolean alignleft) {
		this(text, menu, fontNum, height, action, min, max, 0, alignleft);
	}

	public MenuItemValue(String text, MenuManager menu, int fontNum,
			int height, String action, float min, float max, float defaultValue,
			boolean alignleft) {
		this(text, menu, fontNum, height, action, min, max, defaultValue, 1, alignleft);
	}
	
	public MenuItemValue(String text, MenuManager menu, int fontNum,
			int height, String action, float min, float max, float defaultValue, 
			float increment, boolean alignleft) {
		super(text, menu, fontNum, height, action, alignleft);
		itemLabel = text;
		this.min = min;
		this.max = max;
		this.increment = increment;
		
		value = Utilities.settings.getFloat(action, defaultValue);
		
		setItemText();
	}
	
	private void setItemText() {
		//If the increment is an integer then display the value as an integer
		if (Math.floor(increment) == increment) {
			itemText = itemLabel + ": " + (int)value;
		}else{
			DecimalFormat df = new DecimalFormat("0.00000");
			String formatStr = df.format(increment);
			formatStr = formatStr.replaceFirst("\\d(0+)$", "0");
			df = new DecimalFormat(formatStr);
			itemText = itemLabel + ": " + df.format(value);
		}
		super.text = itemText;
	}
	
	public void selectLeft() {
		value -= increment;
		if (value < min) value = min;
		if (value > max) value = max;
		setItemText();
		//Save the new setting
		Utilities.settings.putFloat(action, (float)value);
		Utilities.settings.saveSettings();
	}
	public void selectRight() {
		value += increment;
		if (value < min) value = min;
		if (value > max) value = max;
		setItemText();
		//Save the new setting
		Utilities.settings.putFloat(action, (float)value);
		Utilities.settings.saveSettings();
	}

}
