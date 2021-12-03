package uk.ac.cf.cs.aspurling.pool;

import uk.ac.cf.cs.aspurling.pool.util.GLColour;

public class Message {

	private String message;
	private GLColour colour;
	private long time;	//time in milliseconds this message was created
	private boolean alive;
	
	public Message(String msg, GLColour col) {
		message = msg;
		colour = col;
		time = System.currentTimeMillis();
		alive = true;
	}
	
	public String getMessage() {
		return message;
	}

	public GLColour getColour() {
		return colour;
	}
	
	public long getTime() {
		return time;
	}
	
	public void setAlive(boolean alive) {
		this.alive = alive;
	}
	
	public boolean isAlive() {
		return alive;
	}

}
