package uk.ac.cf.cs.aspurling.pool.menu;
import java.util.Vector;

public class Menu {
	
	private Vector<MenuItem> menuitems;
	private int curSel;
	private int xpos;
	private int ypos;

	public Menu(int xpos, int ypos) {
		this.xpos = xpos;
		this.ypos = ypos;
		curSel = 0;
		menuitems = new Vector<MenuItem>();
	}

	public void add(MenuItem m) {
		if (menuitems.size() == 0) {
			m.setYPos(ypos);
		}else{
			m.setYPos(menuitems.lastElement().getYPos() + m.getHeight());
		}
		menuitems.add(m);
	}
	
	public void selectItem() {
		menuitems.elementAt(curSel).select();
	}
	
	public void setSelected(int sel) {
		curSel = sel;
		for (MenuItem m: menuitems) {
			m.setSelected(false);
		}
		menuitems.elementAt(curSel).setSelected(true);
	}
	
	public String getActionCommand() {
		return menuitems.elementAt(curSel).getActionCommand();
	}
	
	public void selectNext() {
		menuitems.elementAt(curSel).setSelected(false);
		do {
			curSel++;
			if (curSel == menuitems.size()) curSel = 0;
		}while (!menuitems.elementAt(curSel).getSelectable());
		menuitems.elementAt(curSel).setSelected(true);
	}

	public void selectPrev() {
		menuitems.elementAt(curSel).setSelected(false);
		do {
			if (curSel == 0) curSel = menuitems.size();
			curSel--;
		}while (!menuitems.elementAt(curSel).getSelectable());
		menuitems.elementAt(curSel).setSelected(true);
	}
	
	public void selectLeft() {
		menuitems.elementAt(curSel).selectLeft();
	}
	
	public void selectRight() {
		menuitems.elementAt(curSel).selectRight();
	}
	
	public void render() {
		for (MenuItem m: menuitems) {
			m.render(ypos+m.getHeight());
		}
	}
	
	public int getXPos() {
		return xpos;
	}

	public int getYPos() {
		return ypos;
	}
	
	public MenuItem getCurMenuItem() {
		return menuitems.elementAt(curSel);
	}
	
	public int numMenuItems() {
		return menuitems.size();
	}
	
	public MenuItem getMenuItem(int index) {
		return menuitems.elementAt(index);
	}
}
