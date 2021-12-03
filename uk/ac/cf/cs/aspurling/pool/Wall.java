package uk.ac.cf.cs.aspurling.pool;
import uk.ac.cf.cs.aspurling.pool.util.Vector3D;

public class Wall implements Collidable {

	private Vector3D point1;
	private Vector3D point2;
	private Vector3D plane;
	private Vector3D normal;	//points towards the play area
	
	//This tells the collision handler which collision to respond to
	private int collideWith = 0;
	private Collidable lastCollidedWith;
	
	public Wall(Vector3D point1, Vector3D point2) {
		this.point1 = point1;
		this.point2 = point2;
		//this.point1.y += Ball.BALL_RADIUS;
		//this.point2.y += Ball.BALL_RADIUS;
		plane = point2.subtract(point1);
		normal = (new Vector3D(0, 1, 0)).cross(plane);
		normal.normalize();
	}

	public Collision getCollision(Ball ball, float dt) {
		Vector3D vel = ball.getVel();
		Vector3D pos = new Vector3D(ball.getPos());
		pos.y = 0;
		
		double velDotN = vel.dot(normal);
		if (velDotN >= 0) return null; //moving away from wall
		
		//shortest distance from wall to ball
		double distToWall = Math.abs(pos.subtract(point1).dot(normal));
		
		//distance ball will move towards wall
		double movelen = (-velDotN * dt);
		if (movelen < distToWall - ball.radius) return null;	//not close or fast enough
		

		//ball will intersect with plane
		double collideTime = (distToWall - ball.radius) / movelen;
		
		//System.out.println(this + ", " + collideTime);
		if (collideTime == 0.15749715362212285) {
			int i = 0;
		}
		//if collideTime is less than 0 then distToWall is less than the ball radius and
		//therefore the ball must lie outside the plane of the wall
		if (collideTime > 0) {
			//Check the ball does not leave the limits of the plane
	
			//Check if the point of collision lies between the two points
			//Vector3D collisionPos = pos.add(velN.multiply(movelen));
			Vector3D collisionPos2 = pos.add(vel.multiply(dt * collideTime));
			double proj = collisionPos2.subtract(point1).dot(plane.unit());
			if (proj > 0 && proj < plane.length()) {
				//Collision occurs with face of line
				collideWith = 0;
				return new Collision(this, ball, collideTime);
			}
			
		}

		//Otherwise the final position of the ball lies outside the line
		//segment - we need to check if the ball will collide with one of
		//the end points
		
		double radius2 = ball.radius * ball.radius;
		Vector3D velN = vel.unit();	//unit of velocity vector
		
		Vector3D ballToPoint1 = point1.subtract(pos);
		//D1 is the distance from pos to the closest point the ball gets
		//to from the point
		double D1 = velN.dot(ballToPoint1);

		//If D1 is less than 0 then the ball is moving away from the point
		if (D1 > 0) {
			//find the shortest distance the ball could be from the point
			double distToPoint1 = ballToPoint1.length();
			double closestDist12 = ballToPoint1.length2() - (D1 * D1);
	
			//if this is less than radius then it could intersect the point
			if (closestDist12 < radius2) {
				//Now we need to find the point at which the circle intersects
				//the point
				double T1 = Math.sqrt(radius2 - closestDist12);
				double totalMovDist = vel.multiply(dt).length();
				
				//if T is larger than D then the ball must already be intersecting this point
				if (T1 > D1) {
					int i = 0;
				}
				//does the ball intersect with the line segment within this time step?
				if (D1 - T1 < totalMovDist) {
					collideWith = 1;
					return new Collision(this, ball, (D1 - T1) / totalMovDist);
				}
			}
		}
		
		Vector3D ballToPoint2 = point2.subtract(pos);
		double D2 = velN.dot(ballToPoint2);
		
		//If D2 is less than 0 then the ball is moving away from the point
		if (D2 > 0) {
			//find the shortest distance the ball could be from the point
			double distToPoint2 = ballToPoint2.length();
			double closestDist22 = ballToPoint2.length2() - (D2 * D2);
	
			//if this is less than radius then it could intersect the point
			if (closestDist22 < radius2) {
				//Now we need to find the point at which the circle intersects
				//the point
				double T2 = Math.sqrt(radius2 - closestDist22);
				double totalMovDist = vel.multiply(dt).length();

				//if T is larger than D then the ball must already be intersecting this point
				if (T2 > D2) {
					int i = 0;
				}
				//does the ball intersect with the line segment within this time step?
				if (D2 - T2 < totalMovDist) {
					collideWith = 2;
					return new Collision(this, ball, (D2 - T2) / totalMovDist);
				}
			}
		}

		//Ball intersects plane somewhere outside the line segment
		return null;
	}

	public void handleCollision(Collision collision, float dt) {
		Ball ball = (Ball)collision.object2;
		Vector3D pos = ball.getPos();
		pos.y = 0;
		Vector3D vel = ball.getVel();
		Vector3D collisionNormal;
		
		//reposition ball to touch cushion
		pos.addthis(vel.multiply(collision.time * dt));
		
		//if we are colliding with one of the points then the normal
		//of the collision will be the vector between the position of
		//the ball the the point. Other wise the normal is the normal
		//of the cushion plane
		if (collideWith == 1) {
			collisionNormal = pos.subtract(point1).unit();
		}else if (collideWith == 2) {
			collisionNormal = pos.subtract(point2).unit();
		}else{
			collisionNormal = normal;
		}
		
		//Calculate reflection vector for new velocity
		vel = vel.subtract(collisionNormal.multiply((1 + Table.CUSHION_RESTITUTION) * vel.dot(collisionNormal)));
		//vel = vel.subtract(collisionNormal.multiply(2 * vel.dot(collisionNormal)));
		
		//Factor in coefficient of resitution
		//vel.multiplythis(Table.COEFF_RESITUTION);
			
		//reposition the ball back along the new velocity vector
		pos.subtractthis(vel.multiply(collision.time * dt));
		
		pos.y = Ball.BALL_RADIUS;
		ball.setPos(pos);
		ball.setVel(vel);
		//Vector3D force = collisionNormal.multiply((1 + Table.COEFF_RESTITUTION) * vel.dot(collisionNormal) * 0.2);
		//Vector3D colPoint = new Vector3D(0, Table.CUSHION_HEIGHT - ball.radius,0);
		//ball.applyTorque(colPoint.cross(force), dt);
		
		//Vector3D newangMem = collisionNormal.multiply((1 + Table.COEFF_RESTITUTION) * vel.dot(collisionNormal) * (0.2f * Ball.BALL_RADIUS + (Table.CUSHION_HEIGHT - Ball.BALL_RADIUS))).subtract(ball.getAngularMomentum());
		
		//ball.setAngularMomentum((colPoint.cross(force)).subtract(ball.getAngularMomentum()));
		
		Vector3D angMom = ball.getAngularMomentum();
		Vector3D reflect = angMom.subtract(collisionNormal.multiply(collisionNormal.dot(angMom)));
		ball.setAngularMomentum(angMom.subtract(reflect.multiply(1 + Table.CUSHION_RESTITUTION)));
		
		
		//Need to set which ball we last collided with so we don't collide with it again
		lastCollidedWith = ball;
		ball.setLastCollidedWith(this);
		
	}

}
