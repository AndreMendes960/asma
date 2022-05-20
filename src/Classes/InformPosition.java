package Classes;

import jade.core.AID;

public class InformPosition implements java.io.Serializable {

	private AID agent;
	private Position position;
	private boolean available;
	
	public InformPosition(AID agent, Position position, boolean available) {
		super();
		this.agent = agent;
		this.position = position;
		this.available = available;
	}
	
	public InformPosition(AID agent, int x, int y, boolean available) {
		super();
		this.agent = agent;
		this.position = new Position(x,y);
		this.available = available;
	}

	public AID getAgent() {
		return agent;
	}

	public void setAgent(AID agent) {
		this.agent = agent;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	@Override
	public String toString() {
		return "InformPosition [agent=" + agent + ", position=" + position + ", available=" + available + "]";
	}
	
	

}
