package uk.ac.cf.cs.aspurling.pool;

import uk.ac.cf.cs.aspurling.pool.util.Quaternion;
import uk.ac.cf.cs.aspurling.pool.util.Vector3D;

public class BallDerivative {
	
	public Vector3D vel = new Vector3D();
	public Vector3D accel = new Vector3D();
	public Quaternion spin = new Quaternion(); //rate of change of orientation
	public Vector3D torque = new Vector3D();

}
