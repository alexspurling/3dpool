package uk.ac.cf.cs.aspurling.pool.menu;
import uk.ac.cf.cs.aspurling.pool.util.Utilities;

//This menu item enabled a selection from a given list of string values

public class MenuItemSelection extends MenuItem {

	private String itemLabel;
	private String itemText;
	private int selOption;
	private String[] options;
	
	public MenuItemSelection(String text, MenuManager menu, int fontNum,
			int height, String action, String[] options) {
		this(text, menu, fontNum, height, action, false, options);
	}
	
	public MenuItemSelection(String text, MenuManager menu, int fontNum,
			int height, String action, boolean alignleft, String[] options) {
		this(text, menu, fontNum, height, action, alignleft, options, 0);
	}

	public MenuItemSelection(String text, MenuManager menu, int fontNum,
			int height, String action, boolean alignleft, String[] options, int defaultSel) {
		super(text, menu, fontNum, height, action, alignleft);
		this.options = options;
		
		itemLabel = text;
		
		selOption = defaultSel;

		//Load the selected option from the settings if possible
		String storedValue = Utilities.settings.getString(action, null);
		if (storedValue != null) {
			for (int i = 0; i < options.length; i++) {
				if (storedValue.equals(options[i])) {
					selOption = i;
					break;
				}
			}
		}
		setItemText();
	}
	
	private void setItemText() {
		itemText = itemLabel + ": " + options[selOption];
		super.text = itemText;
		//Save the new setting
		Utilities.settings.putString(action, options[selOption]);
		Utilities.settings.saveSettings();
	}
	
	public void selectLeft() {
		if (selOption == 0) selOption = options.length;
		selOption--;
		setItemText();
	}
	
	public void selectRight() {
		selOption++;
		if (selOption == options.length) selOption = 0;
		setItemText();
	}

}
