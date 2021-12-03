package uk.ac.cf.cs.aspurling.pool.menu;

import uk.ac.cf.cs.aspurling.pool.util.Utilities;

public class MenuItemInput extends MenuItem {
	
	private String itemLabel;
	private StringBuffer itemInput;
	private String itemText;

	public MenuItemInput(String text, MenuManager menu, int fontNum,
			int height, String action) {
		this(text, menu, fontNum, height, action, false);
		
	}

	public MenuItemInput(String text, MenuManager menu, int fontNum,
			int height, String action, boolean alignleft) {
		super(text, menu, fontNum, height, action, alignleft);
		
		itemLabel = text;
		
		itemInput = new StringBuffer(Utilities.settings.getString(action, ""));
		setItemText();
	}
	
	private void setItemText() {
		itemText = itemLabel + ":" + itemInput;
		super.text = itemText;
		Utilities.settings.putString(action, itemInput.toString());
		Utilities.settings.saveSettings();
	}
	
	public void typeChar(char c) {
		itemInput.append(c);
		setItemText();
	}

	public void backspace() {
		if (itemInput.length() > 0) {
			itemInput.deleteCharAt(itemInput.length()-1);
			setItemText();
		}
	}
	
	public String getText() {
		return itemInput.toString();
	}
	
}
