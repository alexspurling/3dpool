package uk.ac.cf.cs.aspurling.pool.multi;

import java.io.Serializable;

public abstract class GameEvent implements Serializable{

	public enum eventType {MESSAGE, STRIKE, MOVECUEBALL, BALLSTATE};
	
	public GameEvent() {
		
	}

}
