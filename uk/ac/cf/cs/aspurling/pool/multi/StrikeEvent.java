package uk.ac.cf.cs.aspurling.pool.multi;

import uk.ac.cf.cs.aspurling.pool.util.Vector3D;

public class StrikeEvent extends GameEvent {

	private Vector3D newVel;
	
	public StrikeEvent(Vector3D newVel) {
		this.newVel = newVel;
	}
	
	public Vector3D getStrike() {
		return newVel;
	}

}
