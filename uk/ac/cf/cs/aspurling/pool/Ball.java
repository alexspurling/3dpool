package uk.ac.cf.cs.aspurling.pool;

import static org.lwjgl.opengl.GL11.*;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.io.Serializable;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.glu.Sphere;
//import org.lwjgl.util.vector.Quaternion;

import uk.ac.cf.cs.aspurling.pool.util.GLColour;
import uk.ac.cf.cs.aspurling.pool.util.Texture;
import uk.ac.cf.cs.aspurling.pool.util.TextureLoader;
import uk.ac.cf.cs.aspurling.pool.util.Utilities;
import uk.ac.cf.cs.aspurling.pool.util.Vector3D;
import uk.ac.cf.cs.aspurling.pool.util.Quaternion;
import uk.ac.cf.cs.aspurling.pool.util.Defaults;


public class Ball implements Collidable, Serializable{

	private static final long serialVersionUID = 84;
	
	public enum State {STOPPED, ROLLING, SLIDING, POCKETED};
	public enum Colour {NONE, RED, YELLOW, BLACK, WHITE};

	public float radius;

	//private float angVel;
	private State state;
	private Colour colour;
	private long pocketTime = 0;	//the time the ball was pocketed
	private int pocketDuration = 1000;	//The number of ms the ball stays in the pocket
	private boolean hidden = false;
	private boolean selected = false;
	
	private Vector3D pos;
	private Vector3D vel;
	private Vector3D accel;
	
	private Vector3D angularMomentum;
	private Quaternion orientation;
	private Vector3D angularVelocity;
	private Quaternion spin; //rate of change of orientation
	
	
	private BallState ballstate;
	
	private Vector3D lastBottomVel;
	
	private Collidable lastCollidedWith;

	//The index of the display list used to draw the balls and the shadow
	private static int dlBall = 0;
	private static int dlShadow = 0;

	public static final float BALL_RADIUS = 0.025f; //0.028575f; //1.125 inches

	private static float BALL_RESITUTION;
	private static float ROLLING_FRICTION; //= 0.02f; //Table speed is 50 (1 / roll fric)
	private static float SLIDING_FRICTION;
	private static final float GRAVITY = 9.8f;
	
	private static final float MASS = 1.0f;
	private static final float intertiaTensor = 2.0f / 5.0f * MASS * BALL_RADIUS * BALL_RADIUS;
	private static final float inverseInertiaTensor = 1 / intertiaTensor;

	transient private Texture texture;
	transient private Texture shadowTexture;
	transient private Material material;

	private int numFrames = 0;

	// TODO FIX THIS
	private boolean showPath = false;
	private Queue<Vector3D> path = new LinkedList<Vector3D>();
	private Vector3D lastPos;
	private Vector3D lastPos2;

	public Ball(Colour c) {
		
		colour = c;
		radius = BALL_RADIUS;
		//Check if display list has already been constructed
		if (dlBall == 0) {
			dlBall = glGenLists(1);
			glNewList(dlBall, GL_COMPILE);
			Sphere s = new Sphere();
			s.setTextureFlag(true);
			String detail = Utilities.settings.getString("graphicsdetail", "high");
			int balldetail = 16;
			if (detail.equals("low")) balldetail = 10;
			if (detail.equals("medium")) balldetail = 16;
			if (detail.equals("high")) balldetail = 32;
			if (detail.equals("very high")) balldetail = 64;
			s.draw(radius,balldetail,balldetail);
			glEndList();
		}
		if (dlShadow == 0) {
			dlShadow = glGenLists(1);
			glNewList(dlShadow, GL_COMPILE);
			//Disk shadow = new Disk();
    		//shadow.draw(0,radius,32,32);
			glBegin(GL_QUADS); 
			{
				glNormal3f(0,1,0);
				glTexCoord2f(0,0);
				glVertex3f(-1,0,-1);
				glTexCoord2f(0,1);
				glVertex3f(-1,0,1);
				glTexCoord2f(1,1);
				glVertex3f(1,0,1);
				glTexCoord2f(1,0);
				glVertex3f(1,0,-1);
			}
			glEnd();
			glEndList();
		};
		
		setup();
		
		pos = new Vector3D(0,0,0);
		vel = new Vector3D(0,0,0);
		accel = new Vector3D(0,0,0);
		orientation = new Quaternion(0,0,0,1);
		angularVelocity = new Vector3D(0,0,0);
		angularMomentum = new Vector3D(0,0,0);
		//angVel = 2.0f;
		//spin = new Vector3D(0,0,0);
		spin = new Quaternion(0,0,0,1);
		//angVel = new Vector3D(0,0,0);
		//rotationAxis = new Vector3D(0,1,0);
		state = State.STOPPED;
		lastBottomVel = new Vector3D();
	}

	public void setup() {
		shadowTexture = TextureLoader.get().getTexture("images/shadow.png");
		material = new Material(new GLColour(0.0f,0.0f,0.0f),
								new GLColour(0.9f,0.9f,0.9f),
								new GLColour(0.6f,0.6f,0.6f),
								60);
		if (colour == Ball.Colour.WHITE) {
			texture = TextureLoader.get().getTexture("images/BallWhite.png");
		}else if (colour == Ball.Colour.BLACK) {
			texture = TextureLoader.get().getTexture("images/BallBlack.png");
		}else if (colour == Ball.Colour.YELLOW) {
			texture = TextureLoader.get().getTexture("images/BallYellow.png");
		}else if (colour == Ball.Colour.RED) {
			texture = TextureLoader.get().getTexture("images/BallRed.png");
		}
		
	}

	public void simulate(float dt) {
		//v = u + a*t
		//s = s + v*t
		
		//
		Vector3D torque = new Vector3D();
		
		accel.x = 0;
		accel.y = 0;
		accel.z = 0;
		
		
		numFrames++;
		if (state == State.ROLLING) {
			//Apply Rolling Friction
			accel = vel.unit().multiply(-ROLLING_FRICTION * GRAVITY);
			if (accel.multiply(dt).length2() >= vel.length2()) {
				accel = vel.divide(-dt);
				angularMomentum = new Vector3D();
				state = State.STOPPED;
				//System.out.println(pos);
				//System.out.println("Ball Stopped " + this + " " + numFrames);
			}
			//angVel = vel.cross(new Vector3D(0,1,0)).unit().multiply(vel.length() / radius);
			//spin = vel.unit().multiply(vel.length() / radius);
			//Fix angular velocity to rolling speed
			angularVelocity = vel.cross(new Vector3D(0,-1,0)).unit().multiply(vel.length() / radius);
		}else if (state == State.SLIDING) {
			/*
			//Create sliding velocity vector
			Vector3D slidingVelV = rotationAxis.cross(new Vector3D(0.0,1.0,0.0));
			slidingVelV.normalize();
			slidingVelV.multiplythis(angVel * radius);
			slidingVelV = vel.subtract(slidingVelV);
				//vel.subtract(vel.unit().multiply(angVel * radius));
			float slidingVel = (float)vel.length() - angVel * radius;
			System.out.println(vel.x);
			System.out.println(slidingVel);
			System.out.println(slidingVelV.length());
			//Apply Sliding Friction
			accel = slidingVelV.unit().multiply(-SLIDING_FRICTION * GRAVITY);
			//if (slidingVel < 0) accel.multiplythis(-1);
			if (accel.multiply(dt).length2() > vel.length2()) {
				accel = vel.divide(-dt);
				state = BallState.STOPPED;
				System.out.println("Ball Stopped " + this);
			}

			vel.addthis(accel.multiply(dt));
			/*
			//Apply torque
			if (slidingVelV.length2() > 0) {
				angVel += dt * ((5 * SLIDING_FRICTION * GRAVITY)/(2 * radius));
			}else{
				angVel -= dt * ((5 * SLIDING_FRICTION * GRAVITY)/(2 * radius));
			}
			//System.out.println(vel.length());
			if (Math.abs(slidingVel) < 0.01) {
			//if (Math.abs(slidingVelV.length()) < 0.01) {
				System.out.println(angVel);
				//angVel = (angVel / Math.abs(angVel)) * (float)(vel.length()) / radius;
				//System.out.println(angVel);
				state = BallState.ROLLING;
				System.out.println("Ball Rolling " + this);
			}
			*/
			//float slidingVel = (float)vel.length() - (float)(vel.dot(spin.multiply(radius))/vel.length());
			//Vector3D slide = vel.subtract(spin.multiply(radius));
			/*float slideVelComponent = (float)vel.dot(slide) / (float)vel.length();
			//System.out.println(slideVelComponent);
			//spin = new Vector3D(0.5,0,0.5);
			//spin = vel.unit().multiply(2 * vel.length() / radius);
			//state = BallState.ROLLING;
			accel = slide.unit().multiply(-SLIDING_FRICTION * GRAVITY);
			vel.addthis(accel.multiply(dt));

			Vector3D newSpin = slide.unit().multiply(dt * ((5 * SLIDING_FRICTION * GRAVITY)/(2 * radius)));
			spin.addthis(newSpin);
			*/
			//if (slide.length2() < 0.001) {
			//	state = State.ROLLING;
			//	//System.out.println("Ball Rolling " + this);
			//}
			
			Vector3D bottomPoint = new Vector3D(0,-radius,0);
			Vector3D bottomVel = vel.add(angularVelocity.cross(bottomPoint));
			
			accel =  bottomVel.unit().multiply(-SLIDING_FRICTION * GRAVITY);
			if (accel.multiply(dt).length2() > vel.length2()) {
				accel = vel.divide(-dt);
				angularMomentum.x = 0;
				angularMomentum.y = 0;
				angularMomentum.z = 0;
				
				state = State.STOPPED;
			}
			torque = bottomPoint.cross(bottomVel.unit().multiply(-SLIDING_FRICTION * GRAVITY));
			//if (torque.multiply(dt).length2() > angularMomentum.length2()) {
			//	torque = angularMomentum.divide(-dt);
			//}
			if (bottomVel.length2() < 0.01) {
				state = State.ROLLING;
			}
			//if (lastBottomVel.dot(bottomVel) < 0) {
			//	state = State.ROLLING;
			//	System.out.println("Ball Rolling " + this);
			//}
			lastBottomVel = bottomVel;
		}
		
		//Calculate values
        orientation.normalize();
		spin = (new Quaternion(angularVelocity.x, angularVelocity.y, angularVelocity.z, 0)).multiply(0.5).multiply(orientation);


		//Integrate velocity
		Vector3D velA = vel;
		Vector3D velB = vel.add(accel.multiply(0.5*dt));
		Vector3D velC = vel.add(accel.multiply(dt));
		Vector3D velRK = velA.add(velB.multiply(4)).add(velC).divide(6);

		pos.addthis(velRK.multiply(dt));
		//pos.addthis(vel.multiply(dt));
		//Integrate acceleration
		vel.addthis(accel.multiply(dt));
		//if (!accel.isZero()) System.out.println(accel);
		//Integrate orientation
		//Quaternion spinA = spin;
		//Quaternion spinB = spin.add(torque.multiply(0.5*dt));
		//Quaternion spinC = spin.add(torque.multiply(dt));
		//Quaternion spinRK = spinA.add(spinB.multiply(4)).add(spinC).divide(6);
		
		//orientation.addthis(spinRK.multiply(dt));		
		orientation.addthis(spin.multiply(dt));
		
		//Integrate angular momentum
		Vector3D momA = angularMomentum;
		Vector3D momB = angularMomentum.add(torque.multiply(0.5*dt));
		Vector3D momC = angularMomentum.add(torque.multiply(dt));
		Vector3D momRK = momA.add(momB.multiply(4)).add(momC).divide(6);

		//angularMomentum.addthis(momRK.multiply(dt));
		angularMomentum.addthis(torque.multiply(dt));

		angularVelocity = angularMomentum.multiply(inverseInertiaTensor);
		
		if (path != null) {
			if (path.isEmpty()) {
				lastPos2 = new Vector3D(pos);
				lastPos = new Vector3D(pos);
				path.add(lastPos);
			}else if (!path.element().equals(pos)) {
				if (path.size() >= 1000) path.remove(); //don't want the path to be too long
				if (path.size() == 1) {
					lastPos2 = lastPos;
					lastPos = new Vector3D(pos);
					path.add(lastPos);
				}else if (lastPos.subtract(lastPos2).cross(pos.subtract(lastPos2)).length2() < 1e-17) {
					lastPos = new Vector3D(pos);
					((LinkedList<Vector3D>)path).removeLast();
					path.add(lastPos);
				}else {
					lastPos2 = lastPos;
					lastPos = new Vector3D(pos);
					path.add(lastPos);
				}
			}
		}             
		
		/*
		//Apply rotation
		Vector3D rotationAxis;
		if (spin.isZero()) {
			rotationAxis = new Vector3D(0,1,0);
		}else{
			rotationAxis = spin.cross(new Vector3D(0.0,-1.0,0.0));
			rotationAxis.normalize();
		}
		float angle = (float)spin.length() * dt;

		//Create Quaternion to calculate new orientation
		Quaternion tempQuat = new Quaternion();
		tempQuat.setFromAxisAngle(new Vector4f((float)rotationAxis.x,(float)rotationAxis.y,(float)rotationAxis.z,angle));

		Quaternion.mul(tempQuat,orientation,orientation);
		*/
	}

	//Renders the ball
	public void render() {
		boolean sleep = false;

		//Draw the ball path
		if (showPath && path != null) {
			if (path.size() > 1) {
				glDisable(GL_TEXTURE_2D);
				glDisable(GL_LIGHTING);
				if (colour == Colour.WHITE) {
					glColor3f(0.6f,0.6f,0.6f);
				}else if (colour == Colour.BLACK) {
					glColor3f(0f,0f,0f);
				}else if (colour == Colour.RED) {
					glColor3f(0.6f,0,0);
				}else if (colour == Colour.YELLOW) {
					glColor3f(0.8f,0.8f,0.1f);
				}
				glBegin(GL_LINE_STRIP);
					//glVertex3f(0, 0.2f, 0);
					//glVertex3f(0.1f, 0.2f, 0);
					for (Vector3D v: path) {
						glVertex3f((float)v.x, (float)v.y, (float)v.z);
					}
				glEnd();
				glEnable(GL_TEXTURE_2D);
				glEnable(GL_LIGHTING);
			}
		}
		//If the ball is hidden then it has been pocketed so don't draw it
		if (hidden) {
			return;
		}
		if (pocketTime != 0) {
			if (System.currentTimeMillis() - pocketTime > pocketDuration) {
				//ball is hidden so don't draw
				hidden = true;
				return;
			}
		}
		
		//Ball must never leave the table
		if (state != State.POCKETED) assert Math.abs(pos.y - Ball.BALL_RADIUS) < 1e-6 : pos.y;
		
		
		glPushMatrix();
		glTranslatef((float)pos.x,(float)pos.y,(float)pos.z);


        glPushMatrix();
	    	Vector3D vecToLight = pos.subtract(new Vector3D(0,1,0));
	    	float xpos = (float)((radius * -vecToLight.x) / vecToLight.y);
	    	float zpos = (float)((radius * -vecToLight.z) / vecToLight.y);
			//glDisable(GL_TEXTURE_2D);
			glDisable(GL_LIGHTING);
			//glDisable(GL_DEPTH_TEST);
			glEnable(GL_COLOR_MATERIAL);
	
			//glColor3f(1.0f,1.0f,1.0f);
	    	//glTranslatef(xpos,-radius,zpos);
	    	glTranslatef(xpos,-(radius-0.001f),zpos);
	    	glScalef(radius, 1, radius);
	    	//glRotatef(90,-1,0,0);

			//glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
	    	//material.render();
			if (selected) {
				//glColorMaterial()
				glColor3f(0.6f,0.3f,0.1f);
			}else{
				glColor3f(0.6f,0.6f,0.6f);
			}
	    	shadowTexture.bind();
	    	glCallList(dlShadow);

			glDisable(GL_COLOR_MATERIAL);
			//glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
			//glEnable(GL_DEPTH_TEST);
	    	glEnable(GL_LIGHTING);
			//glEnable(GL_TEXTURE_2D);
    	glPopMatrix();

    	/*
		float ax, ay, az;
		float quatScale = (float)Math.sqrt(orientation.x*orientation.x +
											orientation.y*orientation.y +
											orientation.z*orientation.z);
		float quatAngle;
		if (quatScale == 0) {
			ax = 1; ay = 0; az = 0;
			quatAngle = 0;
		}else{
			ax = (float)orientation.x / quatScale;
			ay = (float)orientation.y / quatScale;
			az = (float)orientation.z / quatScale;
			if (orientation.w > 1.0) orientation.w = 1.0;
			if (orientation.z < -1.0) orientation.w = -1.0f;
			quatAngle = 2 * (float)Math.acos(orientation.w);
		}

		quatAngle = (float)Math.toDegrees(quatAngle);

		//if (Math.abs(quatAngle) < 1) {
		//	System.out.println("Angle is zero");
		//	sleep = true;
		//}
		

        glRotatef(quatAngle,ax,ay,az);
        */
    	
    	Vector3D axis = orientation.getRotationAxis();
    	double quatAngle = orientation.getRotationAngle();
		quatAngle = Math.toDegrees(quatAngle);
    	glRotatef((float)quatAngle, (float)axis.x, (float)axis.y, (float)axis.z);

        if (sleep) {
			try {
				Thread.sleep(5000);
			}catch (Exception e) {

			}
		}
        
        
        //Push material and lighting settings
        glPushAttrib(GL_LIGHTING_BIT);
		{
			material.render();

			if (texture != null) texture.bind();
			glCallList(dlBall);
			//glCallList(dlShadow);

		}
		//restore material settings
		glPopAttrib();

		glPopMatrix();
	}

	public Collision getCollision(Ball ball2, float dt) {
		
		//if we already collided with this ball then exit
		//if (lastCollidedWith == ball2) return null;
		
		//if this ball has been pocketed then exit
		if (ball2.getState() == State.POCKETED) return null;
		
		//vector from centre of this mass to centre of mass2
		Vector3D C = ball2.pos.subtract(pos);
		double dist = C.length();


		double sumRadii = ball2.radius + radius;

		//Velocity vector of mass1 relative to mass2
		Vector3D movevec = (vel.subtract(ball2.vel)).multiply(dt);
		double movelen = movevec.length();
		//If movement vector is not big enough to reach the mass
		if(movelen < dist - sumRadii) {
			return null;
		}

		//get the dot product of the unit vector of movevec and C
		//in other words the component of C in the direction of movevec
		Vector3D N = movevec.unit();
		double D = N.dot(C);

		//if D is negative then the two masses are moving away from each other
		if(D <= 0) {
			return null;
		}

		//F is the square of the closest that each mass will be from each other
		double F = (dist * dist) - (D * D);

		//if the closest distance is more than sumRadii then there's no way they can collide
		double sumRadiiSquared = sumRadii * sumRadii;
		if(F >= sumRadiiSquared){
			return null;
		}

		//T is the square of the distance between the point when each mass is
		//the closest that they will be from each other and the point where
		//they first touch
		double T = sumRadiiSquared - F;

		//distance is the distance along the movement vector that the mass needs
		//to move so that both masses are touching
		double distance = D - Math.sqrt(T);

		//if the length of the movement vector is less than this distance then
		//the two masses cannot collide
		if(movelen < distance){
			return null;
		}

		//Balls definately collided - begin Collision response code
		double collideTime = distance / movelen;
		return new Collision(this, ball2, collideTime);
	}
	
	public void handleCollision(Collision collision, float dt) {
		Ball ball2 = (Ball)collision.object2;

		//Reposition balls so that they touch - ignore any acceleration
		pos.addthis(vel.multiply(collision.time * dt));
		ball2.pos.addthis(ball2.vel.multiply(collision.time * dt));
		
		//Find the normalized vector n from the center of
		//circle1 to the center of circle2
		Vector3D N2 = ball2.pos.subtract(pos).unit();

		//newDist should now be equal to sumRadii
		double newDist = (pos.subtract(ball2.pos)).length();
		
		//System.out.println(newDist);
		//System.out.println(newDist - radius * 2);
		if (Math.abs(newDist - radius * 2) > 0.0000000000000001) {
			System.out.println("New Dist: " + newDist);
		}

		//Find the length of the component of each of the movement
		//vectors along N2 using dot product.
		double a1 = vel.dot(N2);
		double a2 = ball2.vel.dot(N2);

		double optimizedP = (a1 - a2);

		//Find new velocity vectors
		Vector3D v1n = vel.subtract(N2.multiply(optimizedP)); //New velocity vector of mass1
		Vector3D v2n = ball2.vel.add(N2.multiply(optimizedP)); //New velocity vector of mass2

		//Apply the new velocities and then reposition the masses backwards along their paths
		//so that they end up in the correct position
		//Ball inelasticicy is applied by simply multiplyng the new velocity vectors by the
		//coefficient of restitution

		this.setVel(v1n.multiply(BALL_RESITUTION));
		pos.subtractthis(v1n.multiply(collision.time * dt));
		ball2.setVel(v2n.multiply(BALL_RESITUTION));
		ball2.pos.subtractthis(v2n.multiply(collision.time * dt));
		
		//Need to set which ball we last collided with so we don't collide with it again
		lastCollidedWith = ball2;
		ball2.setLastCollidedWith(this);
		
	}
	
	public static void loadSettings(boolean newGame) {
		ROLLING_FRICTION = 1 / Utilities.settings.getFloat("clothspeed",Defaults.CLOTH_SPEED);
		SLIDING_FRICTION = Utilities.settings.getFloat("slidefriction",Defaults.SLIDING_FRICTION);
		BALL_RESITUTION = Utilities.settings.getFloat("ballcoeffrest",Defaults.BALL_RESITUTION);
		if (newGame) {
			glDeleteLists(dlBall, 1);
			dlBall = 0;
		}
	}
	
	public static void loadDefaults(boolean newGame) {
		ROLLING_FRICTION = 1 / Defaults.CLOTH_SPEED;
		SLIDING_FRICTION = Defaults.SLIDING_FRICTION;
		BALL_RESITUTION = Defaults.BALL_RESITUTION;
		if (newGame) {
			glDeleteLists(dlBall, 1);
			dlBall = 0;
		}
	}

	public void setLastCollidedWith(Collidable c) {
		lastCollidedWith = c;
	}

	/*
	public void setTexture(Texture tex) {
		texture = tex;
	}
	
	public void setShadowTexture(Texture tex) {
		shadowTexture = tex;
	}

	public void setMaterial(Material mat) {
		material = mat;
	}
	*/

	public void setVel(Vector3D vel) {
		this.vel = vel;
		assert vel.y == 0 : vel.y; //Balls can't leave the table
		if (!vel.isZero()) {
			//rotationAxis = vel.unit().cross(new Vector3D(0.0,-1.0,0.0));
			//rotationAxis.normalize();
			//Uncomment if rolling is required
			//spin = vel.unit().multiply(spin.length());
			//if ball has changed velocity then it will probably be sliding
			state = State.SLIDING;
			//path = new LinkedList<Vector3D>();
		}else if (state != State.POCKETED) {
			//rotationAxis = new Vector3D(1,0,0);
			state = State.STOPPED;
			//spin = vel.unit().multiply(spin.length());
		}
	}
	public Vector3D getVel() {
		return vel;
	}

	public void setPos(Vector3D pos) {
		//If this ball is not pocketed make sure it stays on the table
		if (state != State.POCKETED) assert Math.abs(pos.y - Ball.BALL_RADIUS) < 1e-6;
		this.pos = pos;
	}
	public Vector3D getPos() {
		return pos;
	}
	
	public void applyTorque(Vector3D torque, float dt) {
		angularMomentum.addthis(torque.multiply(dt));
	}
	
	public void setAngularMomentum(Vector3D l) {
		angularMomentum = l;
	}

	public Vector3D getAngularMomentum() {
		return angularMomentum;
	}
	
	public State getState() {
		return state;
	}

	public void setState(State s) {
		state = s;
		if (s == State.POCKETED) {
			pocketTime = System.currentTimeMillis();
		}else{
			pocketTime = 0;
			hidden = false;
		}
	}
	
	public Colour getColour() {
		return colour;
	}

	public void setColour(Colour c) {
		colour = c;
	}
	
	public void clearPath() {
		path = new LinkedList<Vector3D>();
	}
	
	public void select(boolean s) {
		selected = s;
	}
	
	public void toggleShowPath() {
		showPath = !showPath;
	}
};
