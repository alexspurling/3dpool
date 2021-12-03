package uk.ac.cf.cs.aspurling.pool.util;
import org.lwjgl.input.Keyboard;

public class KeyboardHandler {
	
	private long keyrepeatdelay = 100;
	private long keyrepeatstart = 0;

	public KeyboardHandler() {
	
	}
	
	public boolean isKeyPressed(int key) {
		if (Keyboard.getEventKey() == key) {
			if (Keyboard.getEventKeyState()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isKeyReleased(int key) {
		if (Keyboard.getEventKey() == key) {
			if (!Keyboard.getEventKeyState()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isKeyDown(int key) {
		return Keyboard.isKeyDown(key);
	}
	
	//Returns true for every keyrepeatdelay ms the key is held down
	public boolean isKeyDownRepeat(int key, int delay) {
		if (isKeyDown(key)) {
			if (keyrepeatstart == 0 ||
					System.currentTimeMillis() - keyrepeatstart > delay) {
				keyrepeatstart = System.currentTimeMillis();
				return true;
			}
		}
		return false;
	}

}
