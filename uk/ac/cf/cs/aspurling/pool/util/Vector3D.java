package uk.ac.cf.cs.aspurling.pool.util;

import java.io.Serializable;
import java.text.NumberFormat;
import java.text.DecimalFormat;

public class Vector3D implements Serializable{
	public double x;
	public double y;
	public double z;
	
	public Vector3D(){
		x = 0;
		y = 0;
		z = 0;
	}
	
	public Vector3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector3D(Vector3D v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}
	
	public void addthis(Vector3D v) {
		x += v.x;
		y += v.y;
		z += v.z;
	}
	
	public void subtractthis(Vector3D v) {
		x -= v.x;
		y -= v.y;
		z -= v.z;
	}

	public void multiplythis(double d) {
		x *= d;
		y *= d;
		z *= d;
	}
	
	public void dividethis(double d) {
		x /= d;
		y /= d;
		z /= d;
	}
	
	public Vector3D add(Vector3D v) {
		return new Vector3D(x + v.x, y + v.y, z + v.z);
	}
	
	public Vector3D subtract(Vector3D v) {
		return new Vector3D(x - v.x, y - v.y, z - v.z);
	}

	public Vector3D multiply(double d) {
		return new Vector3D(x * d, y * d, z * d);
	}
	
	public Vector3D divide(double d) {
		return new Vector3D(x / d, y / d, z / d);
	}
	
	public Vector3D negate() {
		return new Vector3D(-x, -y, -z);
	}
	
	public Vector3D unit() {
		double l = length();
		if (l!=0) {
			return new Vector3D(x / l, y / l, z / l);
		}else{
			return this;
		}
	}
	
	public void normalize() {
		double l = length();
		if (l!=0) {
			x /= l;
			y /= l;
			z /= l;
		}
	}
	
	public double length() {
		return Math.sqrt(x * x + y * y + z * z);
	}
	
	public double length2() {
		return x * x + y * y + z * z;
	}
	
	public double dot(Vector3D v) {
		return x * v.x + y * v.y + z * v.z;
	}
	
	public double angle(Vector3D v) {
		return Math.acos(this.dot(v) / Math.sqrt(this.length2() * v.length2()));
	}
	
	public Vector3D cross(Vector3D v) {
		return new Vector3D(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x);
	}
	
	public boolean isZero() {
		return (Math.abs(x) < 1e-15 && Math.abs(y) < 1e-15 && Math.abs(z) < 1e-15);
	}
	
	public boolean equals(Vector3D v) {
		return (x == v.x && y == v.y && z == v.z);
	}
	
	public String toString() {
		NumberFormat formatter = new DecimalFormat("0.00");
		return ("x: " + formatter.format(x) + ", y: " + formatter.format(y) + ", z: " + formatter.format(z));
	}
	
}
