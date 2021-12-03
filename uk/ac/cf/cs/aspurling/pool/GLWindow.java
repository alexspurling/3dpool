package uk.ac.cf.cs.aspurling.pool;


import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.opengl.DisplayMode;
import uk.ac.cf.cs.aspurling.pool.util.Utilities;
import uk.ac.cf.cs.aspurling.pool.util.Defaults;
import static org.lwjgl.opengl.GL11.*;
import java.util.*;
import java.nio.ByteBuffer;

public class GLWindow {
	
	private int width;
	private int height;
	private int bpp;
	private String title;
	private boolean fullscreen = false;
	private DisplayMode mode;
	private boolean fixframerate = false;

	public GLWindow(String displayModeStr, boolean fullscreen, String title) throws LWJGLException{
		DisplayMode m = getDisplayModeFromStr(displayModeStr);
		if (m == null) {
			System.err.println("Error retreiving the display mode: " + displayModeStr);
		}
		this.width = m.getWidth();
		this.height = m.getHeight();
		this.bpp = m.getBitsPerPixel();
		this.fullscreen = fullscreen;
		this.title = title;
		createWindow();
	}

	public GLWindow(DisplayMode dispMode, boolean fullscreen, String title) throws LWJGLException{
		this.width = dispMode.getWidth();
		this.height = dispMode.getHeight();
		this.bpp = dispMode.getBitsPerPixel();
		this.fullscreen = fullscreen;
		this.title = title;
		createWindow();
	}
	
	public GLWindow(int width, int height, int bpp, boolean fullscreen, String title) throws LWJGLException{
		this.width = width;
		this.height = height;
		this.bpp = bpp;
		this.fullscreen = fullscreen;
		this.title = title;
		createWindow();
	}
	
	public void toggleFullScreen() {
		setFullScreen(!fullscreen);
	}
	
	
	public void setFullScreen(boolean fs) {
		fullscreen = fs;
		try {
			Display.setFullscreen(fullscreen);
		}catch (LWJGLException e) {
			System.err.println("Error switching to/from fullscreen");
			e.printStackTrace();
		}
	}
	
	public void setTitle(String title) {
		this.title = title;
		Display.setTitle(title);
	}
	
	public boolean isWindowClosing() {
		return Display.isCloseRequested();
	}
	
	public int getWidth() {
		return Display.getDisplayMode().getWidth();
	}
	
	public int getHeight() {
		return Display.getDisplayMode().getHeight();
	}
	
	public int getBPP() {
		return Display.getDisplayMode().getBitsPerPixel();
	}
	
	public void update() {
		if (fixframerate) setFrameRate(60);
		Display.update();
	}
	
	public void setFrameRate(int framerate) {
		Display.setVSyncEnabled(false);
		Display.sync(framerate);
	}
	
	public void fixFrameRate(boolean fix) {
		fixframerate = fix;
		if (!fix) Display.setVSyncEnabled(true);
	}
	
	public void createWindow() throws LWJGLException {
		mode = findAndSetDisplayMode(width, height, bpp);
		boolean displayCreated = false;

		boolean creationSucessful = Utilities.settings.getBoolean("windowcreated", true);
		Utilities.settings.putBoolean("windowcreated", false);
		Utilities.settings.saveSettings();
		
		int trySamples = Utilities.settings.getInt("antialiasing", Defaults.ANTIALIASING);
		
		//Try to set the given number of samples
		while (!displayCreated) {
			try {
				// PixelFormat constructor: alpha bits, z-buffer bits, stencil bits, samples
				if (creationSucessful) {
					System.out.println("Try Samples: " + trySamples);
					PixelFormat pf = new PixelFormat(8, 16, 0, trySamples);
					Display.create(pf);
				}else{
					Display.create();
				}
				displayCreated = true;
			}catch (LWJGLException e) {
				if (trySamples >= 4) {
					trySamples /= 2;
				}else if (trySamples > 0) {
					trySamples = 0;
				}else{
					throw e;
				}
			   /*
		      if ("Could not find a valid pixel format".equals(e.getMessage())) {
		         if (trySamples == 0) {
		         // something's wrong if we can't create a display w/ 0 samples
		            throw e;
		         }
		         displayCreated = false;
		         trySamples /= 2;
		      } else if("Samples > 0 specified but there's no support for GLX_ARB_multisample".equals(e.getMessage())) {
		         // this means it doesn't support multisampling
		         displayCreated = false;
		         trySamples = 0;
		      } else {
		         throw e;
		      }*/
		   }
		}
		Utilities.settings.putBoolean("windowcreated", true);

		ByteBuffer buf = TGALoader.loadImage(ResourceLoader.getResourceAsStream(ref), true);
		Display.setIcon(new ByteBuffer[] {buf});
		
		setFullScreen(fullscreen);
		fixframerate = Utilities.settings.getBoolean("fixframerate", Defaults.FIX_FRAME_RATE);
		setTitle(title);
	}
	
	public DisplayMode[] findDisplayModes(boolean filter) {
		DisplayMode[] allModes;
		Vector<DisplayMode> filteredModes = new Vector<DisplayMode>();
		
		try {
			allModes = Display.getAvailableDisplayModes();
			if (!filter) return allModes;
			for (int i = 0; i < allModes.length; i++) {
				if (allModes[i].getWidth() >= 640 && allModes[i].getFrequency() == 60) {
					filteredModes.add(allModes[i]);
				}
			}
			Collections.sort(filteredModes, new ModeComparator());
			DisplayMode[] filtModes = new DisplayMode[filteredModes.size()];
			for (int i = 0; i < filtModes.length; i++) {
				filtModes[i] = filteredModes.elementAt(i);
			}
			return filtModes;
		}catch (LWJGLException e) {
			System.err.println("Error retrieving display modes");
		}
		return null;
	}

	public DisplayMode getDisplayMode() {
		return mode;
	}
	
	public void setDisplayMode(DisplayMode mode) {
		try {
			Display.setDisplayMode(mode);
			this.width = mode.getWidth();
			this.height = mode.getHeight();
			this.bpp = mode.getBitsPerPixel();
			this.mode = mode;
		}catch (LWJGLException e) {
			System.err.println("Error setting display mode");
		}
	}
	
	private DisplayMode findAndSetDisplayMode(int width, int height, int bpp) {
		try {
			DisplayMode[] modes = Display.getAvailableDisplayModes();
			for (int i = 0; i < modes.length; i++) {
				if (modes[i].getWidth() == width && modes[i].getHeight() == height && modes[i].getBitsPerPixel() >= bpp && modes[i].getFrequency() <= 85) {
					Display.setDisplayMode(modes[i]);
					return modes[i];
				}
			}
		} catch (LWJGLException e) {
			System.err.println("Error retrieving display modes");
			e.printStackTrace();
		}

		return null;
	}
	
	public DisplayMode getDisplayModeFromStr(String modeStr) {
		DisplayMode[] dispModes = findDisplayModes(false);
		for (int i = 0; i < dispModes.length; i++) {
			if (dispModes[i].toString().equals(modeStr)) {
				return dispModes[i];
			}
		}
		return null;
	}
	
	//Enter the orthographic mode by first recording the current state, 
	//next changing us into orthographic projection.
	
	public void enterOrtho() {
		// store the current state of the renderer
		glPushAttrib(GL_DEPTH_BUFFER_BIT | GL_ENABLE_BIT);
		glPushMatrix();
		glLoadIdentity();
		glMatrixMode(GL_PROJECTION); 
		glPushMatrix();	
		
		// now enter orthographic projection
		glLoadIdentity();		
		glOrtho(0, width, height, 0, -1, 1);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_LIGHTING);  
	}

	//Leave the orthographic mode by restoring the state we store
	public void leaveOrtho() {
		// restore the state of the renderer
		glPopMatrix();
		glMatrixMode(GL_MODELVIEW);
		glPopMatrix();
		glPopAttrib();
	}
	
	public void closeWindow() {
		Display.destroy();
	}
}


class ModeComparator implements Comparator {
     public int compare(Object o1, Object o2) {
          DisplayMode m1 = (DisplayMode)o1;
          DisplayMode m2 = (DisplayMode)o2;
          return 10 * (m1.getWidth() - m2.getWidth()) + (m1.getHeight() - m2.getHeight());
     }
}