package uk.ac.cf.cs.aspurling.pool.multi;

import uk.ac.cf.cs.aspurling.pool.util.Vector3D;

public class MoveCueBallEvent extends GameEvent {

	Vector3D newPos;
	
	public MoveCueBallEvent(Vector3D ballPos) {
		newPos = ballPos;
	}

	public Vector3D getNewPos() {
		return newPos;
	}
}
