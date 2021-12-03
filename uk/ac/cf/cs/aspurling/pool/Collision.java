package uk.ac.cf.cs.aspurling.pool;

public class Collision implements Comparable {
	
	public Collidable object1;
	public Collidable object2;
	public double time;
	
	public Collision(Collidable c1, Ball c2, double time) {
		object1 = c1;
		object2 = c2;
		this.time = time;
	}
	
	public int compareTo(Object collision2) throws ClassCastException {
	    if (!(collision2 instanceof Collision)) {
	    	throw new ClassCastException("Collision object expected.");
	    }
	    double time2 = ((Collision)collision2).time;
	    if (time < time2) return -1;
	    if (time == time2) return 0;
	    return 1;
	}
	
	public void handleCollision(float dt) {
		object1.handleCollision(this, dt);
	}

}
