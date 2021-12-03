package uk.ac.cf.cs.aspurling.pool;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;

public class Light {

	private boolean enabled = false;
	private float[] ambient;
	private float[] diffuse;
	private float[] specular;
	private float[] position;
	private int lightID;

	public Light(float[] ambient, float[] diffuse, float[] specular, float[] position, int lightID) {
		this.ambient = ambient;
		this.diffuse = diffuse;
		this.specular = specular;
		this.position = position;
		this.lightID = lightID;

	}

	public void render() {
		FloatBuffer temp = BufferUtils.createFloatBuffer(16);
		//temp.position(0);
		int idConst = getLightIDConst(lightID);

		//Enable the light
		if (!enabled) {
			glEnable(idConst);
			glLight(idConst, GL_AMBIENT, (FloatBuffer)temp.put(ambient).flip());
			glLight(idConst, GL_DIFFUSE, (FloatBuffer)temp.put(diffuse).flip());
			glLight(idConst, GL_SPECULAR, (FloatBuffer)temp.put(specular).flip());
			enabled = true;
		}
		//if the light is already enabled we just need to update the position
		glLight(idConst, GL_POSITION, (FloatBuffer)temp.put(position).flip());

		//glPushMatrix();
			//glTranslatef(position[0],position[1],position[2]);
			//Sphere s = new Sphere();
			//s.draw(0.06f,32,32);
		//glPopMatrix();

		//Render children
		//super.render();

		//glDisable(idConst);
	}

	//This converts a numerical light id to the correct
	//OpenGL light ID constant
	private int getLightIDConst(int lightID) {
		switch (lightID) {
		case 0: return GL_LIGHT0;
		case 1: return GL_LIGHT1;
		case 2: return GL_LIGHT2;
		case 3: return GL_LIGHT3;
		case 4: return GL_LIGHT4;
		case 5: return GL_LIGHT5;
		case 6: return GL_LIGHT6;
		case 7: return GL_LIGHT7;
		default: return -1;
		}
	}


}
