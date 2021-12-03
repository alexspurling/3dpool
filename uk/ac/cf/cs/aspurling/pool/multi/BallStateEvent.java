package uk.ac.cf.cs.aspurling.pool.multi;

import uk.ac.cf.cs.aspurling.pool.Ball;

public class BallStateEvent extends GameEvent {

	private Ball[] balls;
	
	public BallStateEvent(Ball[] balls) {
		this.balls = balls;
	}
	
	public boolean compareBallState(Ball[] localBalls) {
		if (localBalls.length != balls.length) return false;
		for (int i = 0; i < balls.length; i++) {
			if (balls[i].getColour() != localBalls[i].getColour()) return false;
			if (balls[i].getState() != localBalls[i].getState()) return false;
			if (!balls[i].getPos().equals(localBalls[i].getPos())) return false;
			if (!balls[i].getVel().equals(localBalls[i].getVel())) return false;
		}
		return true;
	}
	
	public Ball[] getBalls() {
		return balls;
	}

}
