package uk.ac.cf.cs.aspurling.pool;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glTranslatef;
import uk.ac.cf.cs.aspurling.pool.util.Quaternion;
import uk.ac.cf.cs.aspurling.pool.util.Vector3D;

public class CameraQ {

	private Quaternion orientation;
	private Vector3D pos;	//camera position
	private Vector3D target;	//point to orbit around
	private Vector3D view;		//vector pointing from camera to target
	private float dist;		//distance to orbit from
	private float orbitAngle;	//angle around target
	private float elevationAngle;	//verticle angle
	
	private Quaternion quatOrbit = new Quaternion();
	private Quaternion quatElev = new Quaternion();

	private float time = 0; //time for interpolation
	private Quaternion initialOrientation = null;
	private Quaternion finalOrientation = null;
	private Vector3D initialPos = null;
	private Vector3D finalPos = null;

	public CameraQ(Vector3D target, float dist) {
		orientation = new Quaternion();
		orientation.setFromAxisAngle(0, 0, -1, 0);
		this.target = target;
		this.dist = dist;
		view = new Vector3D(0,0,-dist);
		updatePos();
	}

	public void centreCamera(Vector3D target) {
		this.target = target;
		initialPos = new Vector3D(pos);
		initialOrientation = orientation;


		//get the angle between the origin and the target point
		double newAngle = Math.atan2(target.z,target.x) + (3 * Math.PI / 2);
		finalOrientation = getOrientation(newAngle, elevationAngle);

		view = getView(newAngle, elevationAngle);
		finalPos = getPos();

		time = 0;
		orbitAngle = (float)newAngle;
	}
	
	public void setTarget(Vector3D target) {
		this.target = target;
		updatePos();
	}
	
	public Vector3D getTarget() {
		return target;
	}

	public void setDistance(float dist) {
		this.dist = dist;
	}

	public void zoom(float zoom) {
		if (dist + zoom > Ball.BALL_RADIUS * 4 && dist + zoom < 1.3f * Table.TABLE_LENGTH) {
			this.dist += zoom;
			updatePos();
		}
	}

	//Returns a unit vector pointing parallel to the table in the
	//same direction as the camera
	public Vector3D getView() {
		Vector3D dir = new Vector3D();
		dir.x = -view.x;
		dir.z = -view.z;
		dir.normalize();
		return dir;
	}

	public void orbit(float angle, float elev) {
		if (time == 0) {
			if (angle != 0 || elev != 0) {
				orbitAngle += angle;
				//Limit elevation to between 0 and 90 degrees
				if (elevationAngle + elev > 0 && elevationAngle + elev < Math.PI/2) {
					elevationAngle += elev;
				}
	
				//Build a quaternion to represent the orientation
				orientation = getOrientation(orbitAngle, elevationAngle);
	
				//Calculate the vector from camera to target from the orientation
				view = getView(orbitAngle, elevationAngle);
			}
			updatePos();
		}
	}

	private Quaternion getOrientation(double angle, double elev) {
		quatElev.setFromAxisAngle(elev, 1, 0, 0);
		quatOrbit.setFromAxisAngle(angle, 0, 1, 0);
		return quatElev.multiply(quatOrbit);
	}

	private Vector3D getView(double angle, double elev) {
		double cosorbit = Math.cos(angle);
		double sinorbit = Math.sin(angle);
		double coselev = Math.cos(elev);
		double sinelev = Math.sin(elev);

		return new Vector3D(-sinorbit * coselev, sinelev, cosorbit * coselev);
	}

	private Vector3D getPos() {
		return target.add(view.multiply(dist));
	}

	private void updatePos() {
		pos = getPos();
	}
	
	public float getAngle() {
		return orbitAngle;
	}
	
	public void setAngle(float a) {
		orbitAngle = a;
	}
	
	public float getElevation() {
		return elevationAngle;
	}
	
	public void setElevation(float a) {
		elevationAngle = a;
	}

	//Interpolates the rotation and position
	//Returns false when done
	public boolean interpolate(float dt) {
		time += dt;
		float time2 = 1-((time-1)*(time-1)); //use a quadratic function to smooth movement
		//float time2 = time; //use a quadratic function to smooth movement
		if (finalOrientation == null) return false;
		//If the camera is already at the final position then stop
		if (orientation.equals(finalOrientation) && pos.equals(finalPos)) time = 1;
		if (time < 1) {
			orientation = QuatSlerp(initialOrientation, finalOrientation, time2);
			pos = initialPos.add(finalPos.subtract(initialPos).multiply(time2));
		}else{
			orientation = finalOrientation;
			pos = finalPos;
			time = 0;
			return false;
		}
		return true;
	}

	private Quaternion QuatSlerp(Quaternion from, Quaternion to, float t) {
		//float           to1[4];
		double omega, cosom, sinom, scale0, scale1;

		//calc cosine
		cosom = from.x * to.x + from.y * to.y + from.z * to.z + from.w * to.w;

		//adjust signs (if necessary)
		if (cosom < 0.0){
			cosom = -cosom;
			to.x = -to.x;
			to.y = -to.y;
			to.z = -to.z;
			to.w = -to.w;
		}

		float DELTA = 0.001f;
		//calculate coefficients
		if ((1.0 - cosom) > DELTA) {
			//standard case (slerp)
			omega = Math.acos(cosom);
			sinom = Math.sin(omega);
			scale0 = Math.sin((1.0 - t) * omega) / sinom;
			scale1 = Math.sin(t * omega) / sinom;
		}else{
			//"from" and "to" quaternions are very close
			//... so we can do a linear interpolation
			scale0 = 1.0 - t;
			scale1 = t;
		}
		//calculate final values
		Quaternion res = new Quaternion();
		res.x = scale0 * from.x + scale1 * to.x;
		res.y = scale0 * from.y + scale1 * to.y;
		res.z = scale0 * from.z + scale1 * to.z;
		res.w = scale0 * from.w + scale1 * to.w;
		return res;
	}

	public void render() {
		float angle = (float)Math.toDegrees(orientation.getRotationAngle());
		Vector3D axis = orientation.getRotationAxis();
		if (axis.isZero()) {
			axis.y = 1;
			angle = 0;
		}
		//rotate around horizontal axis
		glRotatef(angle,(float)axis.x,(float)axis.y,(float)axis.z);
		//translate to camera position
		glTranslatef((float)-pos.x,(float)-pos.y,(float)-pos.z);
		

	}
}
