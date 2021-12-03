package uk.ac.cf.cs.aspurling.pool.multi;

public class ChatEvent extends GameEvent {

	private String chat;
	
	public ChatEvent(String chat) {
		this.chat = chat;
	}

	public String getString() {
		return chat;
	}
}
