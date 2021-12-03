package uk.ac.cf.cs.aspurling.pool;
import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.glu.Disk;

import uk.ac.cf.cs.aspurling.pool.util.Vector3D;

public class Pocket implements Collidable {
	
	float x;
	float z;
	float radius;
	
	private static int dlPocket = 0;
	
	public Pocket(float x, float z, float radius) {
		this.x = x;
		this.z = z;
		this.radius = radius;
		
		if (dlPocket == 0) {
			dlPocket = glGenLists(1);
			glNewList(dlPocket, GL_COMPILE);
			Disk pocket = new Disk();
			pocket.draw(0,radius,32,1);
			glEndList();
		}

	}
	
	public void render() {
		
		glDisable(GL_LIGHTING);
		glDisable(GL_TEXTURE_2D);
		glDisable(GL_DEPTH_TEST);
		glColor3f(0.0f,0.0f,0.0f);
		glPushMatrix();
		{
		    glTranslatef(x,0,z);
		    glRotatef(90,-1,0,0);
		    glCallList(dlPocket);
		}
		glPopMatrix();
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_LIGHTING);
		glEnable(GL_TEXTURE_2D);
		
	}
	
	public Collision getCollision(Ball ball, float dt) {
		Vector3D pos = ball.getPos();
		Vector3D vel = ball.getVel();
		Vector3D pocketPos = new Vector3D(x,Ball.BALL_RADIUS,z);
		Vector3D posToPocket = pocketPos.subtract(pos);

		Vector3D velN = vel.unit();
		double D = posToPocket.dot(velN);
		//if ball is moving away from pocket
		if (D < 0) return null;
		
		double distToPocket = posToPocket.length();
		double velLen = vel.length() * dt;
		if (distToPocket > velLen + radius) return null; //not close or fast enough

		//F is the square of the closest that the ball will be from the pocket
		double F = (distToPocket * distToPocket) - (D * D);
		double radius2 = radius * radius;
		
		//Ball won't ever get close enough to be pocketed
		if (F > radius2) return null;
		
		//dist is the distance along the movement vector the ball needs to move
		//in order to reach the pocket
		double dist = D - Math.sqrt(radius2 - F);
		
		//velocity is not big enough to reach the pocket
		if (velLen < dist) return null;
		
		return new Collision(this, ball, dist/velLen);
	}
	
	public void handleCollision(Collision collision, float dt) {
		Ball ball = (Ball)collision.object2;
		ball.setState(Ball.State.POCKETED);
		ball.setPos(new Vector3D(x,0,z));
		ball.setVel(new Vector3D(0,0,0));
	}
	
	protected static void deleteDisplayLists() {
		glDeleteLists(dlPocket, 1);
		dlPocket = 0;
	}

}
