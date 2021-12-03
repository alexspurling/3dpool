package uk.ac.cf.cs.aspurling.pool;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Vector;

import uk.ac.cf.cs.aspurling.pool.util.GLColour;
import uk.ac.cf.cs.aspurling.pool.util.TextureLoader;

public class SimulationState implements Serializable {

	public Ball[] balls;
	
	public int numBalls;
	public int curTarget;
	public boolean running = false;	//Are the balls moving?
	public boolean hideCue = false;	//used the hide the cue at the end
	public boolean ballmoveable = true;

	public Player players[];
	public Player curPlayer;
	public Player otherPlayer;
	public Vector<Ball> ballsPotted;
	public Ball firstBallHit;
	
	private static final long serialVersionUID = 83;
	
	public void store(String path) {
		File file = new File(path);
		try {
	        // Serialize to a file
	        ObjectOutput out = new ObjectOutputStream(new FileOutputStream(file));
	        out.writeObject(this);
	        out.close();
	    } catch (IOException e) {
	    	System.err.println("Error while saving state");
	    	System.err.println(e);
	    }
	}
	
	public void restore(String path) {
		File file = new File(path);
		SimulationState state = null;
		if (file.exists()) {
			try {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		        // Deserialize the object
				state = (SimulationState)in.readObject();
				in.close();
			}catch (IOException e) {
		    	System.err.println("Error while loading state: " + e);
			}catch (ClassNotFoundException e) {
				System.err.println("Error loading state object: " + e);
			}
		}
		if (state != null) {
			balls = state.balls;
			numBalls = state.numBalls;
			curTarget = state.curTarget;
			running = state.running;
			hideCue = state.hideCue;
			ballmoveable = state.ballmoveable;
			players = state.players;
			curPlayer = state.curPlayer;
			otherPlayer = state.otherPlayer;
			ballsPotted = state.ballsPotted;
			firstBallHit = state.firstBallHit;
			for (int i = 0; i < balls.length; i++) {
				balls[i].setup();
			}
		}
	}

}
