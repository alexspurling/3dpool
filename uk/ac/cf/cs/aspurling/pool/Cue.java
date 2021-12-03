package uk.ac.cf.cs.aspurling.pool;
import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.glu.Cylinder;
import org.lwjgl.opengl.glu.Disk;

import uk.ac.cf.cs.aspurling.pool.util.GLColour;
import uk.ac.cf.cs.aspurling.pool.util.Texture;
import uk.ac.cf.cs.aspurling.pool.util.TextureLoader;
import uk.ac.cf.cs.aspurling.pool.util.Utilities;
import uk.ac.cf.cs.aspurling.pool.util.Vector3D;
import uk.ac.cf.cs.aspurling.pool.util.Defaults;

public class Cue {
	
	private static final float CUE_LENGTH = 1.2f;		//1.2m cue length
	private static final float CUE_WIDTH_TIP = 0.0065f;	//6.5mm cue tip radius
	private static final float CUE_WIDTH_END = 0.015f;	//15mm cue end radius

	private int dlCue; //Cue display list
	private Texture cueTex;
	
	//Cue position and orientation information
	private Vector3D target;
	private Vector3D offset;
	private float angle;
	private float dist;
	
	public Cue(Vector3D target) {
		this.target = target;
		resetDist();
		offset = new Vector3D();
		
		Material cueMat = new Material(new GLColour(0.1f,0.1f,0.1f),
				  					   new GLColour(0.9f,0.9f,0.9f),
				  					   new GLColour(0.4f,0.4f,0.4f),
				  					   50);
		
		dlCue = glGenLists(1);
		glNewList(dlCue, GL_COMPILE);
		cueMat.render();
		Cylinder c = new Cylinder();
		Disk d = new Disk();
		c.setTextureFlag(true);
		
		int cueDetail = 16;
		String detail = Utilities.settings.getString("graphicsdetail", Defaults.DETAIL);
		if (detail.equals("low")) cueDetail = 8;
		if (detail.equals("medium")) cueDetail = 12;
		if (detail.equals("high")) cueDetail = 16;
		if (detail.equals("very high")) cueDetail = 24;
		
		c.draw(CUE_WIDTH_TIP, CUE_WIDTH_END, CUE_LENGTH, cueDetail, 1);
		d.draw(0, CUE_WIDTH_TIP, cueDetail, 1);
		
		glEndList();
		
		cueTex = TextureLoader.get().getTexture("images/Cue.png");
	}
	
	//Change the angle of the cue around the target
	public void orbit(float angle) {
		this.angle += angle;
	}
	
	//Set the angle around the cue target
	public void setAngle(float angle) {
		this.angle = angle;
	}
	
	//Set the location of the centre of the cue target
	public void setTarget(Vector3D target) {
		this.target = target;
	}
	
	//Set the distance of the cue from the ball
	public boolean setDist(float dist) {
		this.dist += dist;
		if (this.dist < Ball.BALL_RADIUS) {
			this.dist = Ball.BALL_RADIUS;
			return true;
		}
		return false;
	}
	
	//Set the cue to the initial distance from the ball
	public void resetDist() {
		dist = Ball.BALL_RADIUS * 2;
	}
	
	public void setOffset(Vector3D off) {
		Vector3D newOffset = offset.add(off);
		double dist = Math.sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y);
		if (dist > -Ball.BALL_RADIUS + CUE_WIDTH_TIP && dist < Ball.BALL_RADIUS - CUE_WIDTH_TIP) {
			offset.addthis(off);
		}
	}
	
	public Vector3D getOffset() {
		return offset;
	}
	
	public void resetOffset() {
		offset.x = 0; offset.y = 0; offset.z = 0;
	}
	
	//Returns the unit vector of the direction of the cue
	public Vector3D getView() {
		return new Vector3D(Math.sin(Math.toRadians(angle)), 0, -Math.cos(Math.toRadians(angle)));
	}
	
	public void render() {
		glPushMatrix();
		{
			//Render Cue
			glEnable(GL_TEXTURE_2D);
			cueTex.bind();
			//cloth.render();
			glTranslatef((float)target.x, (float)target.y, (float)target.z);
			glRotatef(angle,0,-1,0);
			glRotatef(6,-1,0,0);
			glTranslatef((float)offset.x, (float)offset.y, dist);
			glCallList(dlCue);
		}
		glPopMatrix();
	}

}
