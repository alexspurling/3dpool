package uk.ac.cf.cs.aspurling.pool;

import static org.lwjgl.opengl.GL11.glColor4f;
import uk.ac.cf.cs.aspurling.pool.util.BitmapFont;
import uk.ac.cf.cs.aspurling.pool.util.GLColour;
import uk.ac.cf.cs.aspurling.pool.util.TextureLoader;

import java.util.Queue;
import java.util.LinkedList;

public class MessageSystem {
	
	//total time to display message
	private static final long MESSAGE_LIFE = 5000;
	//time to fade message
	private static final long MESSAGE_FADE = 1000;
	private Queue<Message> messages;
	private BitmapFont font;
	private GLWindow window;
	
	public MessageSystem(GLWindow window) {
		messages = new LinkedList<Message>();
		font = new BitmapFont(TextureLoader.get().getTexture("images/font5.png"),16,16);
		this.window = window;
	}

	//Default colour white
	public void addMessage(String msg) {
		addMessage(msg, new GLColour(1.0f,1.0f,1.0f));
	}
	
	public void addMessage(String msg, GLColour colour) {
		messages.add(new Message(msg, colour));
	}
	
	public void render() {
		int i = 0;
		if (messages.isEmpty()) return;
		for (Message m: messages) {
			String msg = m.getMessage();
			GLColour colour = m.getColour();
			float time = System.currentTimeMillis() - m.getTime();
			if (time <= MESSAGE_LIFE - MESSAGE_FADE) {
				glColor4f(colour.red, colour.green, colour.blue, 1.0f);
			}else if (time <= MESSAGE_LIFE) {
				float fade = (float)(MESSAGE_LIFE - time) / (float)MESSAGE_FADE;
				glColor4f(colour.red, colour.green, colour.blue, fade);
			}else{
				glColor4f(0, 0, 0, 0);
				m.setAlive(false);
			}
			font.drawString(0, msg, window.getWidth() - 14 * msg.length(),window.getHeight()-(20*(messages.size()-i+1)),14);
			i++;
		}
		while (!messages.isEmpty() && !messages.element().isAlive()) {
			messages.remove();
		}
	}

}
