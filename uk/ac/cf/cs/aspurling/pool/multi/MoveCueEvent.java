package uk.ac.cf.cs.aspurling.pool.multi;

import uk.ac.cf.cs.aspurling.pool.util.Vector3D;

public class MoveCueEvent extends GameEvent {

	private Vector3D offset;
	
	public MoveCueEvent(Vector3D offset) {
		this.offset = offset;
	}
	
	public Vector3D getCueOffset() {
		return offset;
	}

}
