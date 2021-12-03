package uk.ac.cf.cs.aspurling.pool.util;
//Create with the help of:
//http://www.gamedev.net/reference/programming/features/quatcam/page2.asp
    
import java.io.Serializable;

public class Quaternion implements Serializable {
	
	public double x, y, z, w;

	public Quaternion() {
		
	}
	
	public Quaternion(double x, double y, double z, double w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	public double length() {
		return Math.sqrt(x * x + y * y + z * z + w * w);
	}
	
	//Get the length squared
	public double length2() {
		return x * x + y * y + z * z + w * w;
	}
	
	public void normalize() {
		double length = length();
		x /= length;
		y /= length;
		z /= length;
		w /= length;
	}
	
	public Quaternion unit() {
		double length = length();
		return new Quaternion(x / length, y / length, z /length, w /length);
	}
	
	public Quaternion conjugate() {
		return new Quaternion(-x, -y, -z, w);
	}

	public Quaternion add(Quaternion b) {
		return new Quaternion(x + b.x, y + b.y, z + b.z, w + b.w);
	}
	
	public void addthis(Quaternion b) {
		x += b.x;
		y += b.y;
		z += b.z;
		w += b.w;
	}
	
	public Quaternion multiply(Quaternion b) {
		Quaternion m = new Quaternion();

		m.x = w*b.x + x*b.w + y*b.z - z*b.y;
		m.y = w*b.y - x*b.z + y*b.w + z*b.x;
		m.z = w*b.z + x*b.y - y*b.x + z*b.w;
		m.w = w*b.w - x*b.x - y*b.y - z*b.z;
		
		return m;
	}
	
	public Quaternion multiply(double m) {
		return new Quaternion(x * m, y * m, z * m, w * m);
	}

	public Quaternion divide(double d) {
		return new Quaternion(x / d, y / d, z / d, w / d);
	}
	
	public boolean equals(Quaternion q) {
		return (x == q.x && y == q.y && z == q.z && w == q.w);
	}
	
	public void setFromAxisAngle(double angle, double x, double y, double z) {
		double sinAngle = Math.sin(angle*0.5);
		this.x = x * sinAngle;
		this.y = y * sinAngle;
		this.z = z * sinAngle;
		this.w = Math.cos(angle*0.5);
	}
	
	public void setFromAxisAngle(double angle, Vector3D axis) {
		setFromAxisAngle(angle, axis.x, axis.y, axis.z);
	}
	
	public double getRotationAngle() {
		if (-1 <= w && w <= 1) {
			return 2 * Math.acos(w);
		}else{
			//w is not between -1 and 1 and so this quaternion cannot
			//represent a rotation
			return 0;
		}
	}
	
	public Vector3D getRotationAxis() {
		Vector3D axis = new Vector3D();
		axis.x = x;
		axis.y = y; 
		axis.z = z;
		return axis;
	}
	
}
