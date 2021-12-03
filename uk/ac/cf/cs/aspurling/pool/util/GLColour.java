package uk.ac.cf.cs.aspurling.pool.util;
//This class represents a colour in OpenGL
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GLColour implements Serializable {

	public float red;
	public float green;
	public float blue;
	public float alpha;
	private FloatBuffer fbuffer = null;
	
	public GLColour(float r, float g, float b, float a) {
		red = r;
		green = g;
		blue = b;
		alpha = a;
	}
	
	public GLColour(float r, float g, float b) {
		this(r,g,b,1.0f);
	}
	
	public GLColour(float colour[]) throws Exception {
		switch (colour.length) {
		case 4: alpha = colour[3];
		case 3: red = colour[0];
				green = colour[1];
				blue = colour[2];
				break;
		default: throw (new Exception());
		}
	}
	
	//Since LWJGL uses byte buffers as opposed to usual arrays
	public FloatBuffer toFloatBuffer() {
		if (fbuffer == null) {
			ByteBuffer temp = ByteBuffer.allocateDirect(16);
			temp.order(ByteOrder.nativeOrder());
			float mat[] = {red, green, blue, alpha};
			fbuffer = (FloatBuffer)temp.asFloatBuffer().put(mat).flip();
		}
		return fbuffer;
	}

}
