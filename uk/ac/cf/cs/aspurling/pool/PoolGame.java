package uk.ac.cf.cs.aspurling.pool;
//This object creates and renders all the objects in the scene

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import uk.ac.cf.cs.aspurling.pool.menu.MenuManager;
import uk.ac.cf.cs.aspurling.pool.util.*;

import java.util.Random;
import java.util.Vector;
import java.util.Queue;
import java.util.LinkedList;
import java.util.regex.Pattern;

public class PoolGame extends Simulation{

	public boolean shuttingdown = false;
	//mouse control constants
	private final float ZOOM_SENSITIVITY = 0.001f;
	private final float MOUSE_SENSITIVITY = 0.1f;

	//variables for calulating FPS
	private int frameCount = 0;
	private long lastTime = 0;
	private int lastCount = 0;

	//Keyboard handler
	private KeyboardHandler keyboard = new KeyboardHandler();
	private boolean chatting = false;
	private StringBuffer chat;
	long chatTime;

	//Simulation parameters
	private int numUpdates;			//number of updates to perform per frame
	private int numBalls;
	private int curTarget;
	private boolean interpolating = false;
	private boolean running = false;	//Are the balls moving?
	private boolean hideCue = false;	//used the hide the cue at the end
	public boolean paused = false;		//Are we in the middle of the game and paused?
	public boolean gameOver = false;
	private boolean overheadview = false;
	private boolean displayHelp = false;
	private boolean ballmoveable = false;
	private boolean simulationMode = false;
	private boolean displayStats = false;

	//These objects make up the simulation
	private Ball[] balls;
	private CameraQ cameraQ;
	private Table table;
	private Light light1;
	//private Light light2;
	//private Light light3;
	private Cue cue;
	private BitmapFont font;
	private SimulationState state;
	private MenuManager menu;
	
	//Multiplayer parameters
	private boolean multiplayer = false;
	private Player thisPlayer;	//this is the player we are
	private int lastPlayerNum;

	//variables for the gameplay
	private Player players[];
	private Player curPlayer;
	private Player otherPlayer;
	private Vector<Ball> ballsPotted;
	private Ball firstBallHit;
	private String message = "";
	private long messageTime = 0;
	private long messageDuration = 4000;
	private Queue<Float> velQueue;
	


	//When a scene is created all the scene objects are created
	//with the setupScene method
	public PoolGame(GLWindow window) {
		super(window);

		Utilities.soundman = new SoundManager();
		Utilities.soundman.initialize(16);
		Utilities.cuehit = Utilities.soundman.addSound("sounds/cuehit.wav");
		Utilities.ballhit = Utilities.soundman.addSound("sounds/ballhit.wav");
		Utilities.cushionhit = Utilities.soundman.addSound("sounds/cushionhit.wav");
		Utilities.pockethit = Utilities.soundman.addSound("sounds/pockethit.wav");

		cameraQ = new CameraQ(new Vector3D(0,0,0), 1.8f);
		setupScene();

		//This locks the mouse within the window and enables
		//the use of the mouse to look around
		Mouse.setGrabbed(true);
		
		menu = new MenuManager(this);
		menu.setVisible(true);
		
		interpolating = false;
	
		//cameraQ.orbit(0,(float)Math.PI / 6);
		cameraQ.setElevation((float)(Math.PI / 2));
		cameraQ.setDistance(2.0f);
		cameraQ.centreCamera(new Vector3D(0,0,0));
		cameraQ.interpolate(1.0f);
		
		Utilities.messages = new MessageSystem(window);
		
	}


	//The main method for setting up all the scene objects
	public void setupScene() {

		numUpdates = Utilities.settings.getInt("maxupdates", Defaults.MAX_UPDATES);
		
		numBalls = 16;
		//object used to store and restore the simulation state
		state = new SimulationState();

		//Load the stored settings
		loadSettings(true);
		
		//Reset some of the game states
		hideCue = false;	//unhide the cue
		gameOver = false;	//game is not over
		paused = false;
		multiplayer = false;
		ballmoveable = true;

		//Set up table
		table = new Table();


		//Set up lighting
		float lightAmbient[] = {0.0f, 0.0f, 0.0f, 1.0f};
		float lightDiffuse[] = {0.7f, 0.7f, 0.7f, 1.0f};
		float lightSpecular[] = {0.6f, 0.6f, 0.6f, 1.0f};
		float lightPosition[] = {0.0f, 1.0f, 0.0f, 1.0f}; //position of light
		light1 = new Light(lightAmbient, lightDiffuse, lightSpecular, lightPosition, 0);

		//float lightPosition2[] = {0.0f, 1.0f, -1.0f, 1.0f}; //position of light
		//light2 = new Light(lightAmbient, lightDiffuse, lightSpecular, lightPosition2, 1);
		
		//float lightPosition3[] = {0.0f, 1.0f, 1.0f, 1.0f}; //position of light
		//light3 = new Light(lightAmbient, lightDiffuse, lightSpecular, lightPosition3, 2);
		
		//Set up balls
		balls = new Ball[numBalls];

		for (int i = 0; i < numBalls; i++) {
			if (i == 0) {
				balls[i] = new Ball(Ball.Colour.WHITE);
			}else if (i == 5) {
				balls[i] = new Ball(Ball.Colour.BLACK);
			}else if (i % 2 == 0){
				balls[i] = new Ball(Ball.Colour.YELLOW);
			}else{
				balls[i] = new Ball(Ball.Colour.RED);
			}
		}

		running = false;

		//Set the list of balls that have been potted
		ballsPotted = new Vector<Ball>();

		//Reset the velocity queue:
		velQueue = new LinkedList<Float>();

		//Set the cue ball on the head spot
		balls[0].setPos(table.getHeadSpot());
		//balls[0].setPos(table.getHeadSpot().add(new Vector3D(0.04f,0,0))); 
		

		//Create cue
		cue = new Cue(new Vector3D(balls[0].getPos()));

		//cameraQ = new CameraQ(new Vector3D(balls[0].getPos()), 0.65f);
		curTarget = 0;
		

		cameraQ.setElevation((float)(Math.PI / 6));
		cameraQ.setDistance(1.5f);
		cameraQ.centreCamera(new Vector3D(balls[0].getPos()));
		//cameraQ.interpolate(1.0f);
		interpolating = true;
		
		/*
		cameraQ = new CameraQ(new Vector3D(balls[0].getPos()), 1.3f);
		//cameraQ = new CameraQ(balls[0].getPos(), 0.85f);
		cameraQ.orbit(0,(float)Math.PI / 6);
		*/

		if (numBalls > 1) {
			balls[1].setPos(table.getFootSpot().add(new Vector3D(0,0,0.00f)));
			createRack(true);
		}

		font = new BitmapFont(TextureLoader.get().getTexture("images/font5.png"),16,16);

		players = new Player[2];
		players[0] = new Player();
		players[1] = new Player();
		players[0].num = 1;
		players[1].num = 2;
		curPlayer = players[0];
		otherPlayer = players[1];
		
		
		//strikeCue(4);
	}

	//This method updates the position of various animated
	//objects given a time step in seconds
	public void update(float dt) {

		final int MAX_SIM_COLLISIONS = 100;

		//If paused, don't do anything
		if (paused) return;
		
		if (interpolating) {
			//This makes sure that the interpolation update rate is constant no matter
			//what the rest of the simulation runs at
			int maxUpdates = Utilities.settings.getInt("maxupdates", Defaults.MAX_UPDATES);
			interpolating = cameraQ.interpolate(dt * (float)maxUpdates / (float)numUpdates);
		}
		
		if (!running) {
			//If we are playing a multiplayer game then listen for game events
			if (multiplayer) {
				if (!Utilities.multiplayer.checkBallState(balls)) {
					Utilities.displayMessage("Ball states are out of synch!", Utilities.MSG.ERROR);
				}

				if (thisPlayer != curPlayer) {
					//Check if a new cue ball position has been received
					Vector3D newCueBallPos = Utilities.multiplayer.getNewCueBallPos();
					if (newCueBallPos != null) {
						balls[0].setPos(newCueBallPos);
					}
					
					//Check if the cue ball has been struck
					Vector3D oppStrike = Utilities.multiplayer.getCueStrike();
					if (oppStrike != null) {
						//Opponent has taken their turn
						strikeCue(oppStrike);
					}
				}
				
				//Receive and display any chat messages
				Utilities.multiplayer.getChat();
			}
			//Don't need to do anything else if the simulation is not running
			return;
		}

		//Find earliest collision to occur within this time step
		int numSimCol = 0;
		Collision curCol;
		double lastCollisionTime = 0;
		do {
			curCol = findEarliestCollision(dt, lastCollisionTime);
			if (curCol != null) {
				lastCollisionTime = curCol.time;
				curCol.handleCollision(dt);
				numSimCol++;
				if (curCol.object1 instanceof Pocket) {
					//something has been pocketed add it to the list
					ballsPotted.add((Ball)curCol.object2);
					Utilities.soundman.playEffect(Utilities.pockethit);
				}else if (curCol.object1 instanceof Ball) {
					//Play the sound of the ball hit
					//get the total speed of both balls
					float vol = (float)(((Ball)(curCol.object1)).getVel().length() + ((Ball)(curCol.object2)).getVel().length());
					vol /= 14f;
					Utilities.soundman.playEffect(Utilities.ballhit, vol);

					//Set which ball was hit first in this turn
					if (firstBallHit == null) firstBallHit = (Ball)curCol.object2;
				}else if (curCol.object1 instanceof Wall) {
					float vol = (float)(((Ball)(curCol.object2)).getVel().length());
					vol /= 6f;
					Utilities.soundman.playEffect(Utilities.cushionhit, vol);
				}
			}
			//Loop until there are no more collisions or
		} while (curCol != null && numSimCol < MAX_SIM_COLLISIONS);
		if (numSimCol > 0) {
			//System.out.println("Number of collisions: " + numSimCol);
		}

		boolean allBallsStopped = true;
		//update the balls' state
		for(int i = 0; i < balls.length; i++) {
			if (balls[i].getState() != Ball.State.POCKETED) {
				balls[i].simulate(dt);
				allBallsStopped = allBallsStopped && (balls[i].getState() == Ball.State.STOPPED);
			}
		}

		for (int i = 0; i < balls.length; i++) {
			balls[i].setLastCollidedWith(null);
		}
		if (allBallsStopped) {
			turnEnded();
		}
	}

	//This function returns the next collision that will occur
	private Collision findEarliestCollision(float dt, double fromTime) {
		Collision earliestCol = null;
		Collision curCol;

		for (int i = 0; i < numBalls; i++) {
			//if the ball is stopped or pocketed it can't collide with anything
			if (balls[i].getState() != Ball.State.STOPPED &&
				balls[i].getState() != Ball.State.POCKETED) {
				//Check for table collisions
				curCol = table.getCollision(balls[i], dt);
				if (curCol != null) {
					//assert curCol.time > 0 && curCol.time < 1;
					//System.out.println(curCol.time + ", " + curCol.object1 + ", " + curCol.object2);
					if (curCol.time > fromTime || curCol.time < 0) {
						if (earliestCol == null) {
							earliestCol = curCol;
						}else{
							if (curCol.time < earliestCol.time) {
								earliestCol = curCol;
							}
						}
					}
				}
				//Check for collisions with other balls
				for (int j = 0; j < numBalls; j++) {
					//Can't collide if ball is pocketed
					if (balls[i].getState() != Ball.State.POCKETED) {
						curCol = balls[i].getCollision(balls[j], dt);
						if (curCol != null) {
							if (curCol.time > fromTime) {
								if (earliestCol == null) {
									earliestCol = curCol;
								}else{
									if (curCol.time < earliestCol.time) {
										earliestCol = curCol;
									}
								}
							}
							//System.out.println(i + "+" + j + ": " + earliestCol.time);
						}
					}
				}
			}
		}
		if (earliestCol != null) {
			//System.out.println(earliestCol.time);
		}
		return earliestCol;
	}

	//This method is called at the end of each turn ie when all balls have stopped
	private void turnEnded() {
		running = false;
		int foulNum = 0;
		String foulMessage = "";
		ballmoveable = false;
		boolean ballPotted = false;
		boolean colourAssigned = curPlayer.colour != Ball.Colour.NONE;
		boolean chooseColour = false;
		
		//Count the number of balls potted in the turn
		int numReds = 0;
		int numYellows = 0;

		//Check if the player hit the right ball
		if (colourAssigned) {
			if (firstBallHit == null || firstBallHit.getColour() != curPlayer.colour) {
				foulNum = 1;
				foulMessage = "own ball not hit first";
			}
		}else{
			if (firstBallHit == null || firstBallHit.getColour() == Ball.Colour.BLACK) {
				foulNum = 1;
				foulMessage = "own ball not hit first";
			}
		}

		for (Ball b: ballsPotted) {
			System.out.println(b.getColour() + " ball potted");
			ballPotted = true;
			if (b.getColour() == Ball.Colour.WHITE) {
				foulNum = 2;
				foulMessage = "white potted";
			}
			if (b.getColour() == Ball.Colour.BLACK) {
				gameOver = true;
				if (curPlayer.colour != Ball.Colour.BLACK) {
					foulNum = 3;
					foulMessage = "black potted";
					//break;
				}
			}
			//only worth checking if a serious foul has not been committed
			//if (foulNum <= 1) {
				if (!colourAssigned) {
					//players haven't been assign a colour yet
					//A colour can't be assigned if a foul has been made
					if (b.getColour() == Ball.Colour.RED) {
						numReds++;
						//if (foulNum == 0) {
							if (curPlayer.colour == Ball.Colour.YELLOW) {
								//Player has already potted a yellow ball
								chooseColour = true;
							}else{
								curPlayer.colour = Ball.Colour.RED;
								otherPlayer.colour = Ball.Colour.YELLOW;
							}
						//}
					}else if(b.getColour() == Ball.Colour.YELLOW) {
						numYellows++;
						//if (foulNum == 0) {
							if (curPlayer.colour == Ball.Colour.RED) {
								//Player has already potted a red ball
								chooseColour = true;
							}else{
								curPlayer.colour = Ball.Colour.YELLOW;
								otherPlayer.colour = Ball.Colour.RED;
							}
						//}
					}
				}else{
					if (b.getColour() == curPlayer.colour) {
						//Player has potted his own ball
						curPlayer.ballPotted();
					}else if (b.getColour() == otherPlayer.colour) {
						//player has potted opponent's ball
						otherPlayer.ballPotted();
						//Not worth recording this foul if a more serious foul has occured
						if (foulNum <= 1) {
							foulNum = 1;
							foulMessage = "opponent's ball potted";
						}
					}
				}
			//}
		}
		switch (foulNum) {
			case 1:
				System.out.println("Foul! " + foulMessage);
				break;
			case 2:
				System.out.println("Foul! " + foulMessage);
				balls[0].setPos(table.getHeadSpot());
				balls[0].setVel(new Vector3D(0,0,0));
				balls[0].setState(Ball.State.STOPPED);
				ballmoveable = true;
				break;
			case 3: System.out.println("Foul! " + foulMessage); break;
		}
		if (foulNum > 0) {
			curPlayer.setFreeShot(false);
			if (otherPlayer.colour == Ball.Colour.BLACK) {
				//If the two shot black rule is enabled then give the other player
				//a free shot
				if (Utilities.settings.getBoolean("twoshotblack", false)) {
					otherPlayer.setFreeShot(true);
				}
			}else{
				otherPlayer.setFreeShot(true);
			}
			message = "FOUL!\n" + foulMessage;
			messageTime = System.currentTimeMillis();
		}

		if (chooseColour) {
			System.out.println("You must choose a colour!");
		}

		if (curPlayer.colour == Ball.Colour.RED) {
			for (int i = 1; i <= numReds; i++) {
				curPlayer.ballPotted();
			}
			for (int i = 1; i <= numYellows; i++) {
				otherPlayer.ballPotted();
			}
		}else if (curPlayer.colour == Ball.Colour.YELLOW) {
			for (int i = 1; i <= numYellows; i++) {
				curPlayer.ballPotted();
			}
			for (int i = 1; i <= numReds; i++) {
				otherPlayer.ballPotted();
			}
		}

		System.out.println("Player 1 balls: " + players[0].numBallsPotted());
		System.out.println("Player 2 balls: " + players[1].numBallsPotted());
		
		//Count the number of non white balls that have been pocketed
		int numBallsPotted = 0;
		for (Ball b: balls) {
			if (b.getState() == Ball.State.POCKETED &&
				b.getColour() != Ball.Colour.WHITE) numBallsPotted++;
		}
		//Make sure the balls potted has been correctly calculated
		assert players[0].numBallsPotted() + players[1].numBallsPotted() == numBallsPotted;

		//A player can only be on the black at the end of a turn. If they pot
		//the black at the same time as their last coloured ball they loose
		if (curPlayer.numBallsPotted() == 7) {
			//Player has potted all of his balls and is now on the black
			curPlayer.colour = Ball.Colour.BLACK;
			//If the two shot on black rule is not enabled then remove any free shot
			if (!Utilities.settings.getBoolean("twoshotblack", false)) {
				curPlayer.setFreeShot(false);
			}
		}
		if (otherPlayer.numBallsPotted() == 7) {
			//Player has potted all of his balls and is now on the black
			otherPlayer.colour = Ball.Colour.BLACK;
			//If the one shot on black rule is not enabled then remove any free shot
			if (!Utilities.settings.getBoolean("twoshotblack", false)) {
				otherPlayer.setFreeShot(false);
			}
		}

		if (gameOver) {
			if (foulNum == 0) {
				message = "GAME OVER\n" + "player " + curPlayer.num + " wins";
			}else{
				message = "GAME OVER\n" + "player " + otherPlayer.num + " wins";
			}
			messageTime = System.currentTimeMillis();

			hideCue = true;
			cameraQ.setElevation((float)(Math.PI / 2));
			cameraQ.setDistance(2.0f);
			interpolate(new Vector3D(0,Ball.BALL_RADIUS,0));
		}else{
			curTarget = 0;
			interpolate(new Vector3D(balls[0].getPos()));
		}

		System.out.println("Turn ended");

		//Swap players
		if ((foulNum != 0) || (!ballPotted && foulNum == 0) && (!curPlayer.getFreeShot())) {
			if (curPlayer == players[0]) {
				curPlayer = players[1];
				otherPlayer = players[0];
			}else{
				curPlayer = players[0];
				otherPlayer = players[1];
			}
		}else{
			//If the one shot carry rule is enabled
			if (Utilities.settings.getBoolean("carry", true)) {
				//Then only remove any free shot if the current player has not potted a ball
				if (!ballPotted) {
					curPlayer.setFreeShot(false);
				}
			}else{
				//Otherwise the player always looses his free shot after potting a ball
				curPlayer.setFreeShot(false);
			}
		}

		String goFor = "";
		if (curPlayer.colour == Ball.Colour.NONE) {
			goFor = "red or yellow";
		}else{
			goFor = curPlayer.colour.toString().toLowerCase();
		}
		Utilities.displayMessage("Player " + curPlayer.num + "'s turn. Go for " + goFor);

		ballsPotted.clear();
		firstBallHit = null;
		cue.resetOffset();
		
		//Send over the state of the balls at the end of this turn
		if (multiplayer) {
			Utilities.multiplayer.sendBallState(balls);
		}
		
	}


	//This method renders the scene and all the game components
	public void render() {
		glMatrixMode(GL_MODELVIEW);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		glLoadIdentity();
		if (!overheadview) {
			cameraQ.render();
		}else{
			//change to overhead view
			glRotatef(-90,0,0,1);
			glRotatef(90,1,0,0);
			glTranslatef(0,-2.0f,0);
		}
		light1.render();
		//light2.render();
		//light3.render();
		table.render();
		
		for(int i = 0; i < balls.length; i++) {
			balls[i].render();
		}
		if (!running && !hideCue && multiplayerTurn()) cue.render();


		window.enterOrtho();
		//Display FPS
		glColor3f(1.0f,1.0f,1.0f);
		String fps = "FPS:" + countFPS();
		font.drawString(0,fps,window.getWidth()-(14*fps.length()),0,14);

		if (displayStats) {
			displayStats();
		}else if (displayHelp) {
			displayHelp();
		}
		displayCurPlayer();
		displayMessages();
		Utilities.messages.render();
		
		if (chatting) {
			glColor3f(1.0f,1.0f,1.0f);
			String cursor = " ";
			if (((System.currentTimeMillis() - chatTime) / 500) % 2 == 0) {
				cursor = "_";
			}
			String chatStr = chat.toString() + cursor;
			font.drawString(0, chatStr, window.getWidth() - 14 * chatStr.length(),window.getHeight()-20,14);
		}
		
		menu.render();

		window.leaveOrtho();

	}
	
	public void startMultiplayerGame(int playernum) {
		if (Utilities.multiplayer == null) {
			System.err.println("Mutliplayer game not initialised.");
			return;
		}
		
		lastPlayerNum = playernum;
		
		if (Utilities.multiplayer.hostMode) {
			//We are hosting the game
			//Send the settings to the client
			String[] settings = {"carry", "twoshotblack", "backfromd", "clothspeed", 
								"tablecoeffrest", "ballcoeffrest", "slidefriction", 
								"pocketradius"};
			System.out.println("Sending settings");
			Utilities.multiplayer.sendSettings(settings);
		}else{
			//we are joining the game
			//Get the settings from the host
			long startTime = System.currentTimeMillis();
			long timeout = 30000;	//30 second timeout
			boolean settingsReceived;
			System.out.println("Receiving settings");
			do {
				settingsReceived = Utilities.multiplayer.getSettings();
			}while (!settingsReceived && System.currentTimeMillis() - startTime < timeout);
			if (!settingsReceived) {
				if (System.currentTimeMillis() - startTime >= timeout) {
					System.err.println("Waiting for settings timeout");
				}else{
					System.err.println("Error receiving settings!");
				}
			}
			simulationMode = false;
			loadSettings(true);
		}
		
		//Start the game
		setupScene();
		
		if (Utilities.multiplayer.hostMode) {
			System.out.println("Sending ball state");
			Utilities.multiplayer.sendBallState(balls);
		}else{
			//Receive the position of the balls from the host
			Ball[] ballState;
			System.out.println("Receiving ball state");
			long startTime = System.currentTimeMillis();
			long timeout = 30000;	//30 second timeout
			do {
				ballState = Utilities.multiplayer.getBallState();
			}while (ballState == null && System.currentTimeMillis() - startTime < timeout);
			if (ballState == null) {
				if (System.currentTimeMillis() - startTime >= timeout) {
					System.err.println("Waiting for ball state timeout");
				}else{
					System.err.println("Error receiving ball state!");
				}
			}else{
				balls = ballState;
				for (Ball b: balls) {
					b.setup();
				}
			}
		}
		
		multiplayer = true;
		
		//set the current player
		curPlayer = players[playernum];	
		otherPlayer = players[1-playernum];	
		
		//Set which player we are
		if (Utilities.multiplayer.hostMode) {
			thisPlayer = players[0];
		}else{
			thisPlayer = players[1];
		}
	}

	//Process keyboard and mouse input
	public void processInput() {

		//If the menu is being displayed then let the menu process the input and return
		//otherwise continue to handle game input
		if (menu.getVisible()) {
			menu.processInput();
			return;
		}
		
		if (gameOver) return;
		
		if (chatting) {
			while(Keyboard.next()) {
				char inputChar = Keyboard.getEventCharacter();
				if (inputChar == '\n' || inputChar == '\r') {
					chatting = false;
					if (chat.length() > 0) {
						Utilities.displayMessage(chat.toString(), Utilities.MSG.CHAT);
						if (multiplayer) Utilities.multiplayer.sendChat(chat.toString());
					}
				}else if (keyboard.isKeyPressed(Keyboard.KEY_BACK)) {
					if (chat.length() > 0) chat.deleteCharAt(chat.length()-1);
				}else if (Pattern.matches("\\p{Print}", String.valueOf(inputChar))) {
					chat.append(inputChar);
				}
			}
			
		}

		//Get change in mouse movement
		float dX = Mouse.getDX();
		float dY = Mouse.getDY();
		
		if (Mouse.isButtonDown(0)) {
			//Zoom camera
			cameraQ.zoom(dY * ZOOM_SENSITIVITY);
		}else if (keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			//Move cue backwards and forwards
			//Don't want to let the player move the cue unless it's their turn
			if (!running && multiplayerTurn()) {
				//running = true;
				//if (multiplayer && !ourTurn)
				//Add the speed of the mouse to the queue
				velQueue.add(new Float(dY));
				//If the size of the queue is big enough then remove the first item
				if (velQueue.size() > 5) {
					velQueue.remove();
				}
				if (cue.setDist(-dY * MOUSE_SENSITIVITY * 0.01f)) {
					//Cue ball has been hit
					
					//Get the average of all the speeds in the queue
					float cueSpeed = 0;
					for (Float v: velQueue) {
						cueSpeed += v.floatValue();
					}
					cueSpeed /= (float)velQueue.size();
					strikeCue(cueSpeed * MOUSE_SENSITIVITY * 0.7f);
				}
			}
		}else if (keyboard.isKeyDown(Keyboard.KEY_M)) {
			//Allow the ball to be moved within the D
			if (!running && (ballmoveable && multiplayerTurn()) || simulationMode) {
				Vector3D newPos = cameraQ.getView().multiply(dY * MOUSE_SENSITIVITY * 0.01f);
				newPos.addthis(cameraQ.getView().cross(new Vector3D(0,1,0)).multiply(dX * MOUSE_SENSITIVITY * 0.01f));
				newPos = newPos.add(balls[curTarget].getPos());
				if ((newPos.subtract(table.getHeadSpot()).length() < Table.D_RADIUS &&
					newPos.z > table.getHeadSpot().z) || simulationMode) {
					boolean ballCollide = false;
					for (int i = 0; i < numBalls; i++) {
						if (i != curTarget) {
							if (balls[i].getPos().subtract(newPos).length2() <= 4 * Ball.BALL_RADIUS * Ball.BALL_RADIUS) {
								ballCollide = true;
								System.out.println("Collison");
							}
						}
					}
					if (!ballCollide) {
						balls[curTarget].setPos(newPos);
						cameraQ.setTarget(new Vector3D(balls[curTarget].getPos()));
						if (curTarget == 0) {
							cue.setTarget(cameraQ.getTarget());
							if (multiplayer) {
								Utilities.multiplayer.sendNewCueBallPos(newPos);
							}
						}
					}
				}
			}
		}else if (keyboard.isKeyDown(Keyboard.KEY_C)) {
			//Move the tip of the cue
			//System.out.println(!running && multiplayerTurn());
			if (!running && multiplayerTurn()) {
				cue.setOffset(new Vector3D(dX * MOUSE_SENSITIVITY * 0.01f, dY * MOUSE_SENSITIVITY * 0.01f, 0));
				if (multiplayer) {
					Utilities.multiplayer.sendNewCueOffset(cue.getOffset());
				}
			}
		}else if (!interpolating){
			//Camera orbits the current ball if no buttons are being pressed
			cameraQ.orbit((float)Math.toRadians(dX * MOUSE_SENSITIVITY),
						  (float)Math.toRadians(-dY * MOUSE_SENSITIVITY));
			if (curTarget == 0) cue.orbit(dX * MOUSE_SENSITIVITY);
			cue.resetDist();
		}

		if (keyboard.isKeyDown(Keyboard.KEY_TAB)) {
			overheadview = true;
		}else{
			overheadview = false;
		}

		//Process key presses
		while(Keyboard.next()) {
		
			//check for escape key
			if (keyboard.isKeyPressed(Keyboard.KEY_ESCAPE)) {
				paused = true;
				menu.setVisible(true);
			}
			
			//check for chat key
			if (keyboard.isKeyPressed(Keyboard.KEY_Y)) {
				chatting = true;
				chat = new StringBuffer();
				chatTime = System.currentTimeMillis();
			}
			
			//check for fullscreen key
			if (keyboard.isKeyPressed(Keyboard.KEY_F)) {
				System.out.println("Switching fullscreen");
				try {
					window.toggleFullScreen();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			//display help
			if(keyboard.isKeyPressed(Keyboard.KEY_F1)) {
				displayHelp = !displayHelp;
			}
	
			//replay the last shot
			if(keyboard.isKeyPressed(Keyboard.KEY_R)) {
				loadSimulationState("replay.sta");
			}
			
			//undo the last shot
			if(keyboard.isKeyPressed(Keyboard.KEY_U)) {
				loadSimulationState("undo.sta");
				cameraQ.setTarget(new Vector3D(balls[curTarget].getPos()));
				//cameraQ.centreCamera();
				cue.setTarget(cameraQ.getTarget());
			}
	
			if(keyboard.isKeyPressed(Keyboard.KEY_T)) {
				//If the game is already running then update the balls to
				//their final positions
				if (running) {
					do {
						update(0.01f);
					}while (running);
				}
				//Reset the game
				//setupScene();
				//Strike the cue
				strikeCue(8.0f);
				//Run the game until balls stop
				do {
					update(0.01f);
				}while (running);
			}

			if(keyboard.isKeyPressed(Keyboard.KEY_P)) {
				for (Ball b: balls) {
					b.toggleShowPath();
				}
			}

			if(keyboard.isKeyPressed(Keyboard.KEY_S)) {
				if (simulationMode) displayStats = !displayStats;
			}

			if(keyboard.isKeyPressed(Keyboard.KEY_B)) {
				if (simulationMode) {
					balls[curTarget].select(false);
					do {
						curTarget = ++curTarget % numBalls;
					}while (balls[curTarget].getState() == Ball.State.POCKETED);
					cameraQ.setTarget(new Vector3D(balls[curTarget].getPos()));
					balls[curTarget].select(true);
					if (curTarget == 0) {
						interpolate(new Vector3D(balls[curTarget].getPos()));
					}else{
						cameraQ.centreCamera(new Vector3D(balls[curTarget].getPos()));
						interpolating = true;
					}
				}
			}
			
			if(keyboard.isKeyPressed(Keyboard.KEY_L)) {
				balls[0].setVel(new Vector3D(0.23034976106732344, 0, -0.8906958056960526));
				running = true;
			}

			int maxUpdates = Utilities.settings.getInt("maxupdates", Defaults.MAX_UPDATES);
			if (keyboard.isKeyPressed(Keyboard.KEY_EQUALS)) {
				if (numUpdates == 1) {
					numUpdates = 2;
				}else if (numUpdates < maxUpdates) {
					numUpdates *= 2;
				}
				Utilities.displayMessage("Simulation speed: " + printFraction(numUpdates, maxUpdates));
				//Utilities.displayMessage("Simulation speed: " + numUpdates / GCD(numUpdates, maxUpdates) + "/" + maxUpdates / GCD(numUpdates, maxUpdates));
			}
			if (keyboard.isKeyPressed(Keyboard.KEY_MINUS)) {
				if (numUpdates <= 2) {
					numUpdates = 1;
				}else{
					numUpdates /= 2;
				}
				Utilities.displayMessage("Simulation speed: " + printFraction(numUpdates, maxUpdates));
				//Utilities.displayMessage("Simulation speed: " + GCD(2, 21));
			}
			
		}

	}
	
	private String printFraction(int a, int b) {
		if (a == b) return "1";
		return a / GCD(a, b) + "/" + b / GCD(a, b);
	}
	
	
	private int GCD(int a, int b) {
		if (b == 0) return a;
		return GCD(b, a % b);
	}
	
	
	private void displayCurPlayer() {
		if (curPlayer.num == 1) {
			glColourBall(curPlayer.colour, 1.0f);
			font.drawString(1,"Player 1",4,window.getHeight()-40,16);
			glColourBall(otherPlayer.colour, 0.3f);
			font.drawString(1,"Player 2",4,window.getHeight()-20,16);
		}else{
			glColourBall(otherPlayer.colour, 0.3f);
			font.drawString(1,"Player 1",4,window.getHeight()-40,16);
			glColourBall(curPlayer.colour, 1.0f);
			font.drawString(1,"Player 2",4,window.getHeight()-20,16);
		}
	}

	//This method is used to find the colour for the current player on the screen
	private void glColourBall(Ball.Colour c, float weight) {
		float r, g, b;
		if (c == Ball.Colour.RED) {
			r = 0.63f; g = 0; b = 0.09f;
		}else if(c == Ball.Colour.YELLOW) {
			r = 0.87f; g = 0.80f; b = 0.11f;
		}else{
			r = 1.0f; g = 1.0f; b = 1.0f;
		}
		glColor3f(weight * r,weight * g,weight * b);
	}

	private void displayMessages() {
		if (messageTime != 0) {
			if (System.currentTimeMillis() - messageTime < messageDuration) {
				glColor3f(1.0f,1.0f,1.0f);
				String lines[] = message.split("\n");
				for (int i = 0; i < lines.length; i++) {
					font.drawString(1,lines[i],window.getWidth()/2 - 8 * lines[i].length(),window.getHeight()/2-50+(i*20),16);
				}
			}else{
				messageTime = 0;
				//The game is over so display the main menu if in single player
				//or start a new game if in multiplayer
				if(gameOver) {
					if (!multiplayer) {
						paused = true;
						menu.setVisible(true);
					}else{
						//Start a new multiplayer game and switch players
						startMultiplayerGame(1-lastPlayerNum);
					}
				}
			}
		}
	}

	private void displayHelp() {
		if (!simulationMode) {
			font.drawString(0,"Display help: F1",0,0,14);
			font.drawString(0,"Strike cue ball: Space",0,12,14);
			font.drawString(0,"Aim: Move mouse",0,24,14);
			font.drawString(0,"Zoom in/out: Left mouse button",0,36,14);
			font.drawString(0,"Overhead view: Tab",0,48,14);
			font.drawString(0,"Chat: Y",0,60,14);
			font.drawString(0,"Move cue ball within D: M",0,72,14);
			font.drawString(0,"Show Menu: Escape",0,84,14);
		}else{
			font.drawString(0,"Display help: F1",0,0,14);
			font.drawString(0,"Strike cue ball: Space",0,12,14);
			font.drawString(0,"Aim: Move mouse",0,24,14);
			font.drawString(0,"Zoom in/out: Left mouse button",0,36,14);
			font.drawString(0,"Overhead view: Tab",0,48,14);
			font.drawString(0,"Chat: Y",0,60,14);
			font.drawString(0,"Replay last shot: R",0,72,14);
			font.drawString(0,"Undo last shot: U",0,84,14);
			font.drawString(0,"Show ball paths: P",0,96,14);
			font.drawString(0,"Show ball stats: S",0,108,14);
			font.drawString(0,"Select next ball: B",0,120,14);
			font.drawString(0,"Move ball: M",0,132,14);
			font.drawString(0,"Show Menu: Escape",0,144,14);
		}
	}
	
	//Shows the statistics for the current ball
	private void displayStats() {
		font.drawString(0,"Ball Num: " + (curTarget + 1),0,0,14);
		font.drawString(0,"Ball state: " + balls[curTarget].getState(),0,12,14);
		font.drawString(0,"Ball colour: " + balls[curTarget].getColour(),0,24,14);
		font.drawString(0,"Ball pos: " + balls[curTarget].getPos(),0,36,14);
		font.drawString(0,"Ball vel: " + balls[curTarget].getVel(),0,48,14);
	}
	

	
	//If given a float velocity then assume the direction is the direction of the camera
	private void strikeCue(float speed) {
		//Vector3D strike = cameraQ.getView().multiply(speed);
		Vector3D strike = cue.getView().multiply(speed);
		strikeCue(strike);
		if (multiplayer) Utilities.multiplayer.sendCueStrike(strike);
	}
	
	private void strikeCue(Vector3D speed) {
		//Save the undo state
		saveSimulationState("undo.sta");
		//Set the cue ball's speed
		balls[0].setVel(speed);
		
		//Vector3D force = collisionNormal.multiply((1 + Table.COEFF_RESTITUTION) * vel.dot(collisionNormal) * 0.2);
		//Vector3D colPoint = new Vector3D(0, Table.CUSHION_HEIGHT - ball.radius,0);
		
		Vector3D cueOffset = cue.getOffset();
		if (multiplayer && thisPlayer != curPlayer) {
			//Check if a new cue offset has been received
			cueOffset = Utilities.multiplayer.getNewCueOffset();
			if (cueOffset == null) cueOffset = cue.getOffset();
		}
		
		for (Ball b: balls) {
			b.clearPath();
		}
		
		balls[0].applyTorque(cueOffset.cross(speed), 0.3f);
		running = true;
		//Calculate volume of cue strike sound based on strike speed
		float vol = (float)speed.length()/150f;
		Utilities.soundman.playEffect(Utilities.cuehit, vol);
		//Save the replay state
		saveSimulationState("replay.sta");
		
	}

	private void interpolate(Vector3D target) {
		cameraQ.centreCamera(target);
		cue.setAngle((float)Math.toDegrees(cameraQ.getAngle()));
		cue.setTarget(cameraQ.getTarget());
		interpolating = true;
	}
	
	private void saveSimulationState(String path) {
		state.balls = balls;
		state.numBalls = numBalls;
		state.curTarget = curTarget;
		state.running = running;
		state.hideCue = hideCue;
		state.ballmoveable = ballmoveable;
		state.players = players;
		state.curPlayer = curPlayer;
		state.otherPlayer = otherPlayer;
		state.ballsPotted = ballsPotted;
		state.firstBallHit = firstBallHit;
		state.store(path);
	}
	
	private void loadSimulationState(String path) {
		state.restore(path);
		balls = state.balls;
		numBalls = state.numBalls;
		curTarget = state.curTarget;
		running = state.running;
		hideCue = state.hideCue;
		ballmoveable = state.ballmoveable;
		players = state.players;
		curPlayer = state.curPlayer;
		otherPlayer = state.otherPlayer;
		ballsPotted = state.ballsPotted;
		firstBallHit = state.firstBallHit;
	}
	
	//This loads the settings from the configuration file
	public void loadSettings(boolean newGame) {
		if (Utilities.settings.getBoolean("simulationmode", Defaults.SIMULATION_MODE)) {
			Ball.loadSettings(newGame);
			Table.loadSettings(newGame);
			simulationMode = true;
		}else{
			Ball.loadDefaults(newGame);
			Table.loadDefaults(newGame);
			simulationMode = false;
			displayStats = false;
		}
		window.fixFrameRate(Utilities.settings.getBoolean("fixframerate", Defaults.FIX_FRAME_RATE));
		//numUpdates = Utilities.settings.getInt("maxupdates", Defaults.MAX_UPDATES);
	}
	
	//This method returns true if we are in single player mode
	//or if we are in multiplayer mode and it's our turn
	private boolean multiplayerTurn() {
		return (!multiplayer || (multiplayer && thisPlayer == curPlayer));
	}

	public void createRack(boolean randomize) {

		Random rand = new Random();


		int ballNum = 1;
		float xpos;
		float zpos;

		final float BALL_OFFSET = 0.000001f;
		float radius = Ball.BALL_RADIUS + BALL_OFFSET;

		Vector3D pos1 = balls[1].getPos();	//get position of first ball

		//Needed to find the positions
		double Sqrt3 = Math.sqrt(3);

		for (int i = 1; i <= 5; i++) {
			for (int j = 1; j <= i; j++) {
				xpos = (j) * radius * 2 - (i + 1) * radius;
				zpos = (float)-(Sqrt3 * (radius-BALL_OFFSET) * (i-3));
				//If randomize is true then add a small offset to the position of the balls
				if (randomize) {
					xpos += (rand.nextFloat() * 2 - 1) * BALL_OFFSET;
					zpos += (rand.nextFloat() * 2 - 1) * BALL_OFFSET;
				}
				if (ballNum < balls.length) {
					balls[ballNum].setPos(new Vector3D(pos1.x + xpos, pos1.y, pos1.z + zpos));
					ballNum++;
				}else{
					break;
				}
			}
		}
	}
	
	public int getNumUpdates() {
		return numUpdates;
	}

	//Release the mouse
	public void shutdown() {
		Mouse.setGrabbed(false);
		Utilities.soundman.destroy();
		if (Utilities.multiplayer != null) Utilities.multiplayer.shutdown();
	}

	//This method counts and displays the current FPS in the window title
	private int countFPS() {
		if (System.currentTimeMillis() - lastTime >= 1000) {
			//window.setTitle("My Scene - FPS: " + frameCount);
			lastTime = System.currentTimeMillis();
			lastCount = frameCount;
			frameCount = 0;
		}
		frameCount++;
		return lastCount;
	}

}
