package uk.ac.cf.cs.aspurling.pool.util;

import uk.ac.cf.cs.aspurling.pool.MessageSystem;
import uk.ac.cf.cs.aspurling.pool.multi.Multiplayer;

public class Utilities {

	public static Settings settings;
	public static Multiplayer multiplayer;
	public static SoundManager soundman;
	public static int cuehit;
	public static int ballhit;
	public static int cushionhit;
	public static int pockethit;
	public static MessageSystem messages;
	public static enum MSG {NORMAL, ERROR, CHAT};
	 
	//Creates a new multiplayer game. If there is already a multiplayer object
	//created then shut it down.
	public static void newMultiplayer() {
		if (multiplayer != null) {
			multiplayer.shutdown();
		}
		multiplayer = new Multiplayer();
	}
	
	public static void newMultiplayer(String address) {
		if (multiplayer != null) {
			multiplayer.shutdown();
		}
		multiplayer = new Multiplayer(address);
	}

	public static void displayMessage(String msg) {
		displayMessage(msg, MSG.NORMAL);
	}
	public static void displayMessage(String msg, MSG type) {
		switch (type) {
		case NORMAL: messages.addMessage(msg); break;
		case ERROR: messages.addMessage(msg, new GLColour(1.0f,0,0)); break;
		case CHAT: messages.addMessage(msg, new GLColour(0.50f,0.75f,0.84f)); break;
		}
	}
}
