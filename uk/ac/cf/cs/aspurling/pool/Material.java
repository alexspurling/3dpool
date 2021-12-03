package uk.ac.cf.cs.aspurling.pool;
import static org.lwjgl.opengl.GL11.*;
import java.io.Serializable;

import uk.ac.cf.cs.aspurling.pool.util.GLColour;

public class Material implements Serializable {

	private GLColour ambient;
	private GLColour diffuse;
	private GLColour specular;
	private GLColour emission;
	private int shininess;
	
	public Material(GLColour ambient, GLColour diffuse, GLColour specular, int shininess) {
		this.ambient = ambient;
		this.diffuse = diffuse;
		this.specular = specular;
		this.shininess = shininess;
		this.emission = new GLColour(0,0,0);
	}
	
	public Material(GLColour ambient, GLColour diffuse, GLColour specular, GLColour emission, int shininess) {
		this.ambient = ambient;
		this.diffuse = diffuse;
		this.specular = specular;
		this.shininess = shininess;
		this.emission = emission;
	}
	
	public void render() {
		//store material settings
		//glPushAttrib(GL_LIGHTING_BIT);
		{
			//glColor4f(ambient.red, ambient.green, ambient.blue, ambient.alpha);
			glMaterial(GL_FRONT, GL_AMBIENT, ambient.toFloatBuffer());
			glMaterial(GL_FRONT, GL_DIFFUSE, diffuse.toFloatBuffer());
			glMaterial(GL_FRONT, GL_SPECULAR, specular.toFloatBuffer());
			glMaterial(GL_FRONT, GL_EMISSION, emission.toFloatBuffer());
			glMateriali(GL_FRONT, GL_SHININESS, shininess);
			
		}
		//Render child nodes
		//super.render();
		//restore material settings
		//glPopAttrib();
	}

}
