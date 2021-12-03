package uk.ac.cf.cs.aspurling.pool;

public interface Collidable {
	
	//Returns a Collision object representing the collision
	//time between this object and a ball if a collision will
	//occur in the next dt seconds
	public Collision getCollision(Ball c, float dt);
	
	//Handles the collision given
	public void handleCollision(Collision c, float dt);
}
