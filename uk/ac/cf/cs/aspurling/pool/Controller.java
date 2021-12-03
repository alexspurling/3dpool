package uk.ac.cf.cs.aspurling.pool;
//This Controller class is the main entry class which sets
//up the OpenGL window and the Scene class

import org.lwjgl.LWJGLException;
import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.GL12;
import static org.lwjgl.opengl.glu.GLU.*;
import org.lwjgl.input.Keyboard;

import uk.ac.cf.cs.aspurling.pool.util.Settings;
import uk.ac.cf.cs.aspurling.pool.util.Utilities;
import uk.ac.cf.cs.aspurling.pool.util.Defaults;

public class Controller {

	private boolean done = false;	//set to true to exit the game loop
	private GLWindow glWindow;		//the window object
	private PoolGame scene;			//the scene object
	
	public float glVersion;			//stores the version of OpenGL the user can support
	
	public static void main(String[] args) {
		System.out.println("Starting...");
		Controller controller = new Controller();	//create a new instance of this class
		System.out.println("Initialising...");
		controller.initialise();				//initialise the window and scene
		System.out.println("Running...");
		controller.run();						//enter the main loop
		System.out.println("Shutting Down...");
		controller.shutdown();					//when the loop exits, shutdown
	}

	//Create and initialise window and scene
	private void initialise() {
		
		//Load the settings object into the utilities class
		Utilities.settings = new Settings("options.cfg");
		
		try {
			String dispModeStr = Utilities.settings.getString("graphicsres", Defaults.DISPLAY_MODE);
			boolean fullscreen = Utilities.settings.getBoolean("fullscreen", Defaults.FULLSCREEN);
			glWindow = new GLWindow(dispModeStr,fullscreen,"3D Pool in Java");
		}catch(LWJGLException e) {
			System.err.println("Error creating window: " + e);
			System.exit(0);
		}
		String version = glGetString(GL_VERSION);
		glVersion = Float.valueOf(version.split("\\.")[0]);
		glVersion += Float.valueOf(version.split("[\\.\\s]")[1]) * 0.1f;
		
		glInit();
		scene = new PoolGame(glWindow);
	}

	//Initialise GL parameters
	public void glInit() {
		// Go into orthographic projection mode.
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		gluPerspective(
				45.0f,
				(float)glWindow.getWidth() / (float)glWindow.getHeight(),
				0.06f,
				5.0f);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		//set clear color to black
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		//Set back and from surfaces to filled
		glPolygonMode(GL_FRONT, GL_FILL);
		glPolygonMode(GL_BACK, GL_FILL);

		glClearDepth(1.0); // Depth Buffer Setup
		glEnable(GL_DEPTH_TEST); // Enables Depth Testing
		glDepthFunc(GL_LEQUAL); // The Type Of Depth Testing To Do
		glEnable(GL_CULL_FACE);
		//Set nice perspective calculations
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
		glShadeModel(GL_SMOOTH);

		//Enable lighting - lighting is global in this application
		glEnable(GL_LIGHTING);
		if (glVersion >= 1.2) {
			glLightModeli(GL12.GL_LIGHT_MODEL_COLOR_CONTROL, GL12.GL_SEPARATE_SPECULAR_COLOR);
		}

		//Enable alpha blending
		glEnable(GL_BLEND);
		glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		//Set textures to reapeat
		glEnable(GL_TEXTURE_2D);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
	}

	private void run() {

		//Start game loop
		
		do {

			scene.processInput();
			int maxUpdates = Utilities.settings.getInt("maxupdates", Defaults.MAX_UPDATES);
			int numUpdates = scene.getNumUpdates();
			for (int i = 1; i <= numUpdates; i++) {
				scene.update((float)(0.01 / (double)maxUpdates));	//update with a time step of 10ms
			}
			//try {Thread.sleep(100);} catch (Exception e) {}
			scene.render();
			glWindow.update();
			//glWindow.setFrameRate(60); //fix framerate

			if (scene.shuttingdown || glWindow.isWindowClosing()) done = true;
		} while (!done);
	}

	//shutdown the scene and window
	private void shutdown() {
		scene.shutdown();
		glWindow.closeWindow();
	}

}
