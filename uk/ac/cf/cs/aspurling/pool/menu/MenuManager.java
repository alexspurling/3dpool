package uk.ac.cf.cs.aspurling.pool.menu;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.glu.GLU.gluPerspective;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL12;

import uk.ac.cf.cs.aspurling.pool.GLWindow;
import uk.ac.cf.cs.aspurling.pool.PoolGame;
import uk.ac.cf.cs.aspurling.pool.Ball;
import uk.ac.cf.cs.aspurling.pool.Table;
import uk.ac.cf.cs.aspurling.pool.multi.Multiplayer;
import uk.ac.cf.cs.aspurling.pool.util.KeyboardHandler;
import uk.ac.cf.cs.aspurling.pool.util.Texture;
import uk.ac.cf.cs.aspurling.pool.util.TextureLoader;
import uk.ac.cf.cs.aspurling.pool.util.Utilities;
import uk.ac.cf.cs.aspurling.pool.util.Defaults;


public class MenuManager {

	protected GLWindow window;
	private PoolGame game;
	private boolean visible = false;
	private KeyboardHandler keyboard = new KeyboardHandler();
	
	private Texture logoTex;
	private int logoPos;
	private int menuPos;
	
	private Menu mainMenu;
	private Menu pausedMenu;
	private Menu optionsMenu;
	private Menu gamerulesMenu;
	private Menu gameMenu;
	private Menu simulationMenu;
	private Menu multiplayerMenu;
	private Menu hostMenu;
	private Menu joinMenu;
	public Menu curMenu;
	
	private boolean connecting = false; //Are we trying to connect?
	
	//Keyboard toggles
	private boolean upToggle = false;
	private boolean downToggle = false;
	private boolean leftToggle = false;
	private boolean rightToggle = false;
	private boolean returnToggle = false;
	private boolean escapeToggle = true;
	
	private int menuScroll;
	private int menuSelect;
	
	public MenuManager(PoolGame game) {
		this.game = game;
		window = game.window;
		
		logoTex = TextureLoader.get().getTexture("images/Logo.png");
		logoPos = 60;
		
		mainMenu = new Menu(250,250);
		mainMenu.add(new MenuItem("START NEW GAME", this, 1, 40, "newgame"));
		mainMenu.add(new MenuItem("MULTIPLAYER", this, 1, 40, "multiplayer"));
		mainMenu.add(new MenuItem("OPTIONS", this, 1, 40, "options"));
		mainMenu.add(new MenuItem("EXIT", this, 1, 40, "exit"));
		mainMenu.setSelected(0);
		
		pausedMenu = new Menu(250,250);
		pausedMenu.add(new MenuItem("RESUME GAME", this, 1, 40, "resumegame"));
		pausedMenu.add(new MenuItem("START NEW GAME", this, 1, 40, "newgame"));
		pausedMenu.add(new MenuItem("MULTIPLAYER", this, 1, 40, "multiplayer"));
		pausedMenu.add(new MenuItem("OPTIONS", this, 1, 40, "options"));
		pausedMenu.add(new MenuItem("EXIT", this, 1, 40, "exit"));
		pausedMenu.setSelected(0);
		
		optionsMenu = new Menu(250,250);
		optionsMenu.add(new MenuItem("GAME OPTIONS", this, 1, 40, "game"));
		optionsMenu.add(new MenuItem("RULES OPTIONS", this, 1, 40, "rules"));
		optionsMenu.add(new MenuItem("SIMULATION OPTIONS", this, 1, 40, "simulation"));
		optionsMenu.add(new MenuItem("BACK", this, 1, 40, "backmain"));
		optionsMenu.setSelected(0);
		
		gameMenu = new Menu(200,250);
		gameMenu.add(new MenuItemSelection("graphics detail", this, 0, 20, "graphicsdetail", true, new String[]{"low", "medium", "high", "very high"}, 2));
		gameMenu.add(new MenuItemSelection("resolution", this, 0, 20, "graphicsres", true, getDisplayModes(), findDisplayMode(Defaults.DISPLAY_MODE)));
		gameMenu.add(new MenuItemValue("anti aliasing", this, 0, 20, "antialiasing",  0, 4, Defaults.ANTIALIASING, 2, true));
		gameMenu.add(new MenuItemValue("max slow mo", this, 0, 20, "maxupdates",  2, 32, Defaults.MAX_UPDATES, 2, true));
		gameMenu.add(new MenuItemChecked("fix frame rate", this, 0, 20, "fixframerate", true, Defaults.FIX_FRAME_RATE));
		gameMenu.add(new MenuItemChecked("fullscreen mode", this, 0, 20, "fullscreen", true, Defaults.FULLSCREEN));
		gameMenu.add(new MenuItemSelection("table cloth colour", this, 0, 20, "clothcolour", true, new String[]{"green", "blue", "red"}));
		gameMenu.add(new MenuItem("BACK", this, 1, 40, "backoptions"));
		gameMenu.setSelected(0);
		
		gamerulesMenu = new Menu(200,250);
		gamerulesMenu.add(new MenuItemChecked("one shot carry", this, 0, 20, "carry", true, Defaults.ONE_SHOT_CARRY));
		gamerulesMenu.add(new MenuItemChecked("two shots on black", this, 0, 20, "twoshotblack", true, Defaults.TWO_SHOW_BLACK));
		gamerulesMenu.add(new MenuItemChecked("shoot backwards from D", this, 0, 20, "backfromd", true, Defaults.BACK_FROM_D));
		gamerulesMenu.add(new MenuItem("BACK", this, 1, 40, "backoptions"));
		gamerulesMenu.setSelected(0);
		
		simulationMenu = new Menu(200,250);
		simulationMenu.add(new MenuItemChecked("simulation mode enabled", this, 0, 20, "simulationmode", true, Defaults.SIMULATION_MODE));
		simulationMenu.add(new MenuItemValue("table cloth speed", this, 0, 20, "clothspeed", 5, 200, Defaults.CLOTH_SPEED, true));
		simulationMenu.add(new MenuItemValue("cushion bounce", this, 0, 20, "tablecoeffrest", 0, 1, Defaults.CUSHION_RESTITUTION, 0.01f, true));
		simulationMenu.add(new MenuItemValue("ball bounce", this, 0, 20, "ballcoeffrest", 0, 1, Defaults.BALL_RESITUTION, 0.01f, true));
		simulationMenu.add(new MenuItemValue("sliding friction", this, 0, 20, "slidefriction", 0, 1, Defaults.SLIDING_FRICTION, 0.01f, true));
		simulationMenu.add(new MenuItemValue("pocket radius", this, 0, 20, "pocketradius", Table.POCKET_RADIUS * 0.5f, Table.POCKET_RADIUS * 2, Defaults.POCKET_RADIUS, 0.001f, true));
		simulationMenu.add(new MenuItem("BACK", this, 1, 40, "backoptions"));
		simulationMenu.setSelected(0);
		setSimulationOptions();
		
		multiplayerMenu = new Menu(250,250);
		multiplayerMenu.add(new MenuItem("HOST GAME", this, 1, 40, "hostgame"));
		multiplayerMenu.add(new MenuItem("JOIN GAME", this, 1, 40, "joingame"));
		multiplayerMenu.add(new MenuItem("BACK", this, 1, 40, "backmain"));
		multiplayerMenu.setSelected(0);

		hostMenu = new Menu(250,250);
		hostMenu.add(new MenuItem("LISTENING...", this, 1, 40, "listening"));
		hostMenu.add(new MenuItem("BACK", this, 1, 40, "backmultiplayer"));
		hostMenu.setSelected(0);
		
		joinMenu = new Menu(250,250);
		joinMenu.add(new MenuItemInput("enter host name or IP address", this, 0, 40, "connectto"));
		joinMenu.add(new MenuItem("BACK", this, 1, 40, "backmultiplayer"));
		joinMenu.setSelected(0);
		
		curMenu = mainMenu;
		
		menuScroll = Utilities.soundman.addSound("sounds/menuscroll.wav");
		menuSelect = Utilities.soundman.addSound("sounds/menuselect.wav");
		
	}
	
	public void render() {
		if (!visible) return;
		
		glDisable(GL_LIGHTING);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		//Draw back quad
		glDisable(GL_TEXTURE_2D);
		glBegin(GL_QUADS);
		{
			glColor4f(0.5f,0.5f,0.6f,0.85f); //blue grey
			
			glVertex2i(0,0);
			glVertex2i(0,window.getHeight());
			glVertex2i(window.getWidth(),window.getHeight());
			glVertex2i(window.getWidth(),0);
		}
		glEnd();
		glEnable(GL_TEXTURE_2D);
		
		//Draw logo
		logoTex.bind();
		glBegin(GL_QUADS);
		{
			
			glColor3f(1.0f,1.0f,1.0f);
			
			glTexCoord2f(0.0f,0.0f);
			glVertex2i((int)((window.getWidth()/2) - (logoTex.getImageWidth()/2)),(int)(logoPos+logoTex.getImageHeight()));
			glTexCoord2f(1.0f,0.0f);
			glVertex2i((int)((window.getWidth()/2) + (logoTex.getImageWidth()/2)),(int)(logoPos+logoTex.getImageHeight()));
			glTexCoord2f(1.0f,1.0f);
			glVertex2i((int)((window.getWidth()/2) + (logoTex.getImageWidth()/2)),logoPos);
			glTexCoord2f(0.0f,1.0f);
			glVertex2i((int)((window.getWidth()/2) - (logoTex.getImageWidth()/2)),logoPos);
			
		}
		glEnd();
		
		//Render the current menu
		curMenu.render();
		
		glDisable(GL_BLEND);
		glEnable(GL_LIGHTING);
	}
	
	public void processInput() {

		//Handle up and down events
		if (keyboard.isKeyDownRepeat(Keyboard.KEY_UP, 150)) {
			curMenu.selectPrev();
			Utilities.soundman.playEffect(menuScroll);
		}
		if (keyboard.isKeyDownRepeat(Keyboard.KEY_DOWN, 150)) {
			curMenu.selectNext();
			Utilities.soundman.playEffect(menuScroll);
		}
		
		//If the current item is a Selection and a Value menu item type
		int repeatRate = 100;
		if (curMenu.getCurMenuItem() instanceof MenuItemSelection) {
			repeatRate = 150;
		}else if (curMenu.getCurMenuItem() instanceof MenuItemValue) {
			repeatRate = 70;
		}
		if (keyboard.isKeyDownRepeat(Keyboard.KEY_LEFT, repeatRate)) {
			curMenu.selectLeft();
		}
		if (keyboard.isKeyDownRepeat(Keyboard.KEY_RIGHT, repeatRate)) {
			curMenu.selectRight();
		}
		
		while (Keyboard.next()) {

			if (keyboard.isKeyPressed(Keyboard.KEY_RETURN)) {
	
				Utilities.soundman.playEffect(menuSelect);
				curMenu.selectItem();
				//Process menu actions
				String action = curMenu.getActionCommand();
				if (action.equals("newgame")) {
					setVisible(false);
					game.setupScene();
				}else if (action.equals("resumegame")) {
					setVisible(false);
					game.paused = false;
				}else if (action.equals("multiplayer")) {
					curMenu = multiplayerMenu;
				}else if (action.equals("options")) {
					curMenu = optionsMenu;
				}else if (action.equals("game")) {
					curMenu = gameMenu;
				}else if (action.equals("rules")) {
					curMenu = gamerulesMenu;
				}else if (action.equals("simulation")) {
					curMenu = simulationMenu;
				}else if (action.equals("simulationmode")) {
					setSimulationOptions();
				}else if (action.equals("backmain")) {
					curMenu.setSelected(0);
					displayMainMenu();
				}else if (action.equals("backoptions")) {
					curMenu.setSelected(0);
					curMenu = optionsMenu;
				}else if (action.equals("backmultiplayer")) {
					curMenu.setSelected(0);
					curMenu = multiplayerMenu;
				}else if (action.equals("fullscreen")) {
					window.toggleFullScreen();
				}else if (action.equals("graphicsres")) {
					//setDisplayMode(Utilities.settings.getString("graphicsres","800 x 600 x 32 @60Hz"));
					//setDisplayMode(((MenuItemSelection)curMenu.getCurMenuItem()).getValue());
				}else if (action.equals("hostgame")) {
					curMenu = hostMenu;
					Utilities.newMultiplayer();
					connecting = true;
				}else if (action.equals("joingame")) {
					curMenu = joinMenu;
				}else if (action.equals("connectto")) {
					//Connect to the address
					String addr = ((MenuItemInput)curMenu.getCurMenuItem()).getText();
					Utilities.newMultiplayer(addr);
					connecting = true;
				}else if (action.equals("exit")) {
					game.shuttingdown = true;
				}
			}


			if (!escapeToggle) {
				if (keyboard.isKeyPressed(Keyboard.KEY_ESCAPE)) {
					if (curMenu == pausedMenu) {
						setVisible(false);
						game.paused = false;
					}else if (curMenu == optionsMenu) {
						displayMainMenu();
					}else if (curMenu != mainMenu) {
						curMenu.setSelected(0);
						curMenu = optionsMenu;
					}
				}
			}
			
			if (!keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
				escapeToggle = false;
			}
			
			//If we are dealing with an input menu item then we need to 
			//process the input characters
			if (curMenu.getCurMenuItem() instanceof MenuItemInput) {
				char inputChar = Keyboard.getEventCharacter();
				if (Character.isLetterOrDigit(inputChar) || inputChar == '.') {
					((MenuItemInput)curMenu.getCurMenuItem()).typeChar(inputChar);
				}
				if (keyboard.isKeyPressed(Keyboard.KEY_BACK)) {
					((MenuItemInput)curMenu.getCurMenuItem()).backspace();
				}
			}
		}
		
		
		//Process connections
		if (connecting) {
			if (Utilities.multiplayer.getSocket().isConnected()) {
				//Start multiplayer game!
				setVisible(false);
				connecting = false;
				game.startMultiplayerGame(0); //player 0 goes first
			}
		}
	}
	
	//Display the correct menu depending on whether the game is over or paused
	private void displayMainMenu() {
		if (game.gameOver) {
			curMenu = mainMenu;
		}else if (game.paused) {
			curMenu = pausedMenu;
		}else{
			curMenu = mainMenu;
		}
	}
	
	public void setVisible(boolean v) {
		visible = v;
		escapeToggle = true;
		if (visible) {
			displayMainMenu();
			Mouse.setGrabbed(false);
		}else{
			Mouse.setGrabbed(true);
			//load settings with true if this is a new game
			if (game.paused) {
				game.loadSettings(false);
			}else{
				game.loadSettings(true);
			}
		}
	}
	
	private String[] getDisplayModes() {
		DisplayMode[] dispModes = window.findDisplayModes(true);
		String[] dispModesStr = new String[dispModes.length];
		for (int i = 0; i < dispModes.length; i++) {
			dispModesStr[i] = dispModes[i].toString();
		}
		return dispModesStr;
	}
	
	private int findDisplayMode(String dispModeStr) {
		String[] d = getDisplayModes();
		for (int i = 0; i < d.length; i++) {
			if (d[i].equals(dispModeStr)) return i;
		}
		return 0;
	}
	
	private void setSimulationOptions() {
		boolean sel = Utilities.settings.getBoolean("simulationmode", Defaults.SIMULATION_MODE);
		for (int i = 1; i < simulationMenu.numMenuItems()-1; i++) {
			simulationMenu.getMenuItem(i).setSelectable(sel);
		}
	}
	
	public boolean getVisible() {
		return visible;
	}

}
