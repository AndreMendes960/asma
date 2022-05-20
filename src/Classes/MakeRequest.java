package Classes;

import jade.core.AID;

public class MakeRequest implements java.io.Serializable {
	
	private AID agent;
	private Position init, dest;
	
	public MakeRequest(AID agent, Position init, Position dest) {
		super();
		this.agent = agent;
		this.init = init;
		this.dest = dest;
	}

	public AID getAgent() {
		return agent;
	}

	public void setAgent(AID agent) {
		this.agent = agent;
	}

	public Position getInit() {
		return init;
	}

	public void setInit(Position init) {
		this.init = init;
	}

	public Position getDest() {
		return dest;
	}

	public void setDest(Position dest) {
		this.dest = dest;
	}

	@Override
	public String toString() {
		return "MakeRequest [agent=" + agent + ", init=" + init + ", dest=" + dest + "]";
	}

}
