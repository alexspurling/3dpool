package uk.ac.cf.cs.aspurling.pool;
//This class is able to perform matrix transformations
//to reprsent the view changes for the keyboard and mouse
//controls

import static org.lwjgl.opengl.GL11.*;

public class Camera {

	private final float DEG_TO_RAD = (float)Math.PI/180;
	//rotation around y axis
	private float yrot = 0;
	//rotation around horizontal axis
	private float hrot = 0;
	//translation forwards/backwards
	float xpos = 0;
	float ypos = 0;
	float zpos = 0;
	
	public Camera() {
		//set up list of children
		super();
	}
	
	//Rotate around y axis
	public void rotateY(float angle) {
		yrot += angle;
	}
	
	//Rotate around the horizontal axis
	public void rotateH(float angle) {
		hrot += angle;
		if (hrot > 90) hrot = 90;
		if (hrot < -90) hrot = -90;
	}
	
	//Movement forwards is calculated based on the current
	//vertical rotation
	public void moveForwards(float distance) {
		xpos -= (float)Math.sin(yrot*DEG_TO_RAD) * distance;
		zpos += (float)Math.cos(yrot*DEG_TO_RAD) * distance;
	}

	//Movement backwards
	public void moveBackwards(float distance) {
		xpos += (float)Math.sin(yrot*DEG_TO_RAD) * distance;
		zpos -= (float)Math.cos(yrot*DEG_TO_RAD) * distance;
	}
	
	//Movement left
	public void moveLeft(float distance) {
		xpos -= (float)Math.sin((yrot-90)*DEG_TO_RAD) * distance;
		zpos += (float)Math.cos((yrot-90)*DEG_TO_RAD) * distance;
	}

	//Movement right
	public void moveRight(float distance) {
		xpos -= (float)Math.sin((yrot+90)*DEG_TO_RAD) * distance;
		zpos += (float)Math.cos((yrot+90)*DEG_TO_RAD) * distance;
	}

	//Vertical movement 
	public void moveUp(float distance) {
		ypos -= distance;
	}

	//Vertical movement 
	public void moveDown(float distance) {
		ypos += distance;
	}
	
	//The render routine pushes the current matrix, applies the
	//transformations, renders all its children then pops the
	//matrix back off the stack
	public void render() {
		//glPushMatrix();
		{
			//rotate around y axis
			glRotatef(yrot,0,1,0);
			//rotate around horizontal axis
			glRotatef(hrot,(float)Math.cos(yrot * DEG_TO_RAD),0.0f,(float)Math.sin(yrot * DEG_TO_RAD));
			//translate to player position
			glTranslatef(xpos,ypos,zpos);
		}
		//super.render();	//render children
		//glPopMatrix();
		
	}

}
