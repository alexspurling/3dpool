package uk.ac.cf.cs.aspurling.pool.menu;
//This is a general menu item for performing actions when selected

import static org.lwjgl.opengl.GL11.glColor3f;
import uk.ac.cf.cs.aspurling.pool.util.BitmapFont;
import uk.ac.cf.cs.aspurling.pool.util.TextureLoader;

public class MenuItem {

	protected MenuManager menu;
	protected String text;
	private BitmapFont font;
	private int fontNum;
	private int height;
	private int xpos = 0;
	private int ypos = 0;
	private boolean selected;
	protected String action;
	protected boolean alignleft;
	private boolean selectable = true;
	
	public MenuItem(String text, MenuManager menu, int fontNum, int height, String action) {
		this.text = text;
		this.menu = menu;
		font = new BitmapFont(TextureLoader.get().getTexture("images/font5.png"),16,16);
		this.fontNum = fontNum;
		this.height = height;
		this.action = action;
	}

	public MenuItem(String text, MenuManager menu, int fontNum, int height, String action, boolean alignleft) {
		this(text, menu, fontNum, height, action);
		this.alignleft = alignleft;
	}
	
	public void render(int height) {
		if (selected) {
			glColor3f(0.9f,0.78f,0.39f);
		}else if (selectable) {
			glColor3f(0.42f,0.36f,0.16f);
		}else{
			//Menu item is disabled
			glColor3f(0.45f,0.41f,0.38f);
		}
		int fontWidth;
		if (fontNum == 0) {
			fontWidth = 14;
		}else{
			fontWidth = 18;
		}
		if (alignleft) {
			xpos = menu.curMenu.getXPos();
		}else{
			xpos = menu.window.getWidth() / 2 - (fontWidth * text.length() / 2);
		}
		font.drawString(fontNum,text,xpos,ypos,fontWidth);
	}
	
	public void setYPos(int ypos) {
		this.ypos = ypos;
	}
	
	public int getYPos() {
		return ypos;
	}
	
	public int getHeight() {
		return height;
	}
	
	public String getText() {
		return text;
	}
	
	public void setFontNum(int num) {
		fontNum = num;
	}
	
	public void setSelected(boolean sel) {
		selected = sel;
	}

	public void setSelectable(boolean sel) {
		selectable = sel;
	}
	
	public boolean getSelectable() {
		return selectable;
	}
	
	public void setActionCommand(String action) {
		this.action = action;
	}
	
	public String getActionCommand() {
		return action;
	}
	
	public void select() {

	}
	public void selectLeft() {
		
	}
	public void selectRight() {
		
	}

}
