package uk.ac.cf.cs.aspurling.pool;

import uk.ac.cf.cs.aspurling.pool.util.Quaternion;
import uk.ac.cf.cs.aspurling.pool.util.Vector3D;

public class BallState {
	
	public Vector3D pos = new Vector3D();
	public Vector3D vel = new Vector3D();
	public Vector3D angularMomentum = new Vector3D();
	public Vector3D angularVelocity = new Vector3D();
	public Quaternion orientation = new Quaternion();
	public Quaternion spin = new Quaternion();
	
	/*
	public BallDerivative evaluate(BallState initial, float dt) {
		
	}
	
	
	public BallDerivative evaluate(BallState initial, float dt, BallDerivative d) {
		state.position += derivative.velocity * dt;
		state.momentum += derivative.force * dt;
		state.orientation += derivative.spin * dt;
		state.angularMomentum += derivative.torque * dt;
		state.recalculate();
		
		Derivative output;
		output.velocity = state.velocity;
		output.spin = state.spin;
		forces(input, planes, state, output.force, output.torque);
		return output;
	}
	*/
}
