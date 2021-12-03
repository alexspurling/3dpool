package uk.ac.cf.cs.aspurling.pool;

import java.io.Serializable;

public class Player implements Serializable {
	
	public Ball.Colour colour;
	public int num;
	private int numBallsPotted;
	private boolean hasFreeShot;
	
	public Player() {
		colour = Ball.Colour.NONE;
	}
	
	public void ballPotted() {
		numBallsPotted++;
	}
	
	public int numBallsPotted() {
		return numBallsPotted;
	}
	
	public void setFreeShot(boolean freeshot) {
		hasFreeShot = freeshot;
	}
	
	public boolean getFreeShot() {
		return hasFreeShot;
	}

}
