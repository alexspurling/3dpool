package uk.ac.cf.cs.aspurling.pool.multi;

import java.net.*;
import java.io.*;

import uk.ac.cf.cs.aspurling.pool.util.Vector3D;
import uk.ac.cf.cs.aspurling.pool.Ball;
import uk.ac.cf.cs.aspurling.pool.util.Utilities;

public class Multiplayer extends Thread {
	
	public static final int gamePort = 8172;
	public boolean hostMode;
	private InetAddress addr;
	private Socket sock = new Socket();
	private ServerSocket srv; 

	//Store the last variables that were received so that the game can pick them up
	private StrikeEvent lastStrike;
	private BallStateEvent lastBallState;
	private MoveCueBallEvent lastCueBallPos;
	private MoveCueEvent lastCueOffset;
	private SettingsEvent lastSettings;
	private ChatEvent lastChat;
	
	public Multiplayer() {
		//We are in listen mode
		hostMode = true;
		this.start();
	}
	
	public Multiplayer(String address) {
		//We are in connect mode
		hostMode = false;
		Utilities.displayMessage("Connecting to " + address);
		
		try {
			addr = InetAddress.getByName(address);
		}catch (UnknownHostException e) {
			Utilities.displayMessage("Address could not be resolved.", Utilities.MSG.ERROR);
		}
		this.start();
	}
	
	//Process connections in a new thread
	public void run() {
		if (hostMode) {
			//We are hosting the game so listen for a connection
			try {
		        srv = new ServerSocket(gamePort);
		        srv.setSoTimeout(60000); //60 seconds
		        // Wait for connection from client.
		        Utilities.displayMessage("Listening for connection...");
		        sock = srv.accept();
	        }catch (SocketTimeoutException e) {
	        	Utilities.displayMessage("Listening for connection timed out.", Utilities.MSG.ERROR);
	        }catch (BindException e) {
	        	Utilities.displayMessage("Cannot listen to port. Port already in use.", Utilities.MSG.ERROR);
	        }catch (SocketException e) {
	        	Utilities.displayMessage("Connection closing...");
		    }catch (IOException e) {
		    	Utilities.displayMessage("Error listening to socket.", Utilities.MSG.ERROR);
	        	System.err.println(e);
		    }
		}else{
			//We are joining a game so attempt to make a connection
			SocketAddress sockaddr = new InetSocketAddress(addr, gamePort);
		    
	        // This method will block no more than timeoutMs.
	        // If the timeout occurs, SocketTimeoutException is thrown.
	        int timeoutMs = 20000;   // 20 seconds
	        try {
	        	sock.connect(sockaddr, timeoutMs);
	        }catch (SocketTimeoutException e) {
	        	Utilities.displayMessage("Connection to socket timed out", Utilities.MSG.ERROR);
	        }catch (IOException e) {
	        	Utilities.displayMessage("Error connecting to socket", Utilities.MSG.ERROR);
	        	System.err.println(e);
	        }
		}
		//Listen for messages from the socket
		if (sock != null) {
			if (hostMode) {
				Utilities.displayMessage("Client connected");
			}else{
				Utilities.displayMessage("Connected to host");
			}
			try {
				ObjectInputStream ois = null;
				while(sock.isConnected() && !sock.isClosed()) {
					//Get the input stream for the socket
					ois = new ObjectInputStream(sock.getInputStream());
					processEvent((GameEvent)ois.readObject());
				}
				if (ois != null) ois.close();
			}catch (ClassNotFoundException e) {
				System.err.println("Error reading socket stream");
				System.err.println(e);
			}catch (IOException e) {
	        	Utilities.displayMessage("Connection closed.", Utilities.MSG.ERROR);
			}
			
		}
	}
	
	private void processEvent(GameEvent event) {
		if (event instanceof StrikeEvent) {
			lastStrike = (StrikeEvent)event;
		}else if (event instanceof BallStateEvent) {
			lastBallState = (BallStateEvent)event;
		}else if (event instanceof MoveCueBallEvent) {
			lastCueBallPos = (MoveCueBallEvent)event;
		}else if (event instanceof MoveCueEvent) {
			lastCueOffset = (MoveCueEvent)event;
		}else if (event instanceof SettingsEvent) {
			lastSettings = (SettingsEvent)event;
		}else if (event instanceof ChatEvent) {
			lastChat = (ChatEvent)event;
		}
	}
	
	public Socket getSocket() {
		return sock;
	}
	
	private void sendGameEvent(GameEvent event) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
			oos.writeObject(event);
	        oos.flush();
		}catch (IOException e) {
			System.err.println("Error sending game event");
			System.err.println(e);
		}
	}
	
	//we have struck the cue, send the velocity over to opponent
	public void sendCueStrike(Vector3D strike) {
		sendGameEvent(new StrikeEvent(strike));
	}
	
	public Vector3D getCueStrike() {
		if (lastStrike != null) {
			Vector3D strike = lastStrike.getStrike();
			lastStrike = null;
			return strike;
		}
		return null;
	}
	
	public void sendBallState(Ball[] balls) {
		sendGameEvent(new BallStateEvent(balls));
	}
	
	//if there is a ball state to retreive then this function returns false if 
	//the states do not match. Otherwise it returns true.
	public boolean checkBallState(Ball[] balls) {
		if (lastBallState != null) {
			boolean stateMatches = lastBallState.compareBallState(balls);
			lastBallState = null;
			return stateMatches;
		}
		return true;
	}
	
	public Ball[] getBallState() {
		if (lastBallState != null) {
			Ball[] tempState = lastBallState.getBalls();
			lastBallState = null;
			return tempState;
		}
		return null;
	}
	
	//Player has moved the cueball so send the new position
	public void sendNewCueBallPos(Vector3D newPos) {
		sendGameEvent(new MoveCueBallEvent(newPos));
	}
	
	public Vector3D getNewCueBallPos() {
		if (lastCueBallPos != null) {
			Vector3D ballPos = lastCueBallPos.getNewPos();
			lastCueBallPos = null;
			return ballPos;
		}
		return null;
	}
	
	//Player has moved the cue tip so send the new position
	public void sendNewCueOffset(Vector3D offset) {
		sendGameEvent(new MoveCueEvent(offset));
	}
	
	public Vector3D getNewCueOffset() {
		if (lastCueOffset != null) {
			Vector3D offest = lastCueOffset.getCueOffset();
			lastCueOffset = null;
			return offest;
		}
		return null;
	}
	
	public void sendSettings(String[] keys) {
		sendGameEvent(new SettingsEvent(keys));
	}

	

	//If there are no settings to receive then this function returns false
	public boolean getSettings() {
		if (lastSettings != null) {
			lastSettings.storeSettings();
			return true;
		}
		return false;
	}
	
	public void sendChat(String chat) {
		sendGameEvent(new ChatEvent(chat));
	}
	
	public void getChat() {
		if (lastChat != null) {
			Utilities.displayMessage(lastChat.getString(), Utilities.MSG.CHAT);
			lastChat = null;
		}
	}
	
	public void shutdown() {
		try {
			if (sock != null) sock.close();
			if (srv != null) srv.close();
		}catch (IOException e) {
			System.err.println("Warning: error while closing socket");
			System.err.println(e);
		}
	}

}
