package uk.ac.cf.cs.aspurling.pool.menu;
import uk.ac.cf.cs.aspurling.pool.util.Utilities;

//This menu item allows a selection to be enabled and disabled

public class MenuItemChecked extends MenuItem {
	
	private String itemLabel;
	private String itemText;
	private boolean checkedValue;

	public MenuItemChecked(String text, MenuManager menu, int fontNum,
			int height, String action) {
		this(text, menu, fontNum, height, action, false);
	}

	public MenuItemChecked(String text, MenuManager menu, int fontNum,
			int height, String action, boolean alignleft) {
		this(text, menu, fontNum, height, action, false, false);
	}
	
	public MenuItemChecked(String text, MenuManager menu, int fontNum,
			int height, String action, boolean alignleft, boolean defaultValue) {
		super(text, menu, fontNum, height, action, alignleft);

		itemLabel = text;
		
		checkedValue = Utilities.settings.getBoolean(action, defaultValue);
		
		setItemText();
	}
	
	private void setItemText() {
		if (checkedValue) {
			itemText = itemLabel + " [x]";
		}else{
			itemText = itemLabel + " [ ]";
		}
		super.text = itemText;
	}
	
	//This method is called when the item is selected
	public void select() {
		checkedValue = !checkedValue;
		setItemText();
		Utilities.settings.putBoolean(action, checkedValue);
		Utilities.settings.saveSettings();
	}
	
	public boolean getChecked() {
		return checkedValue;
	}

}
