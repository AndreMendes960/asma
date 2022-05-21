package agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import Classes.InformPosition;
import Classes.Position;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class piece extends Agent {
	
	private Position current_location;
	private AID team_manager;

	protected void setup() {
		super.setup();
		current_location = new Position(1, 1);
		team_manager = new AID();
		
		this.addBehaviour(new Register());
		this.addBehaviour(new Receiver());
	}

	protected void takeDown() {
		super.takeDown();
	}
	
	private class Register extends OneShotBehaviour {
		public void action() {

			DFAgentDescription template = new DFAgentDescription();
			//DFAgentDescription template2 = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			//ServiceDescription sd2 = new ServiceDescription();
			sd.setType("central");
			template.addServices(sd);
			//sd2.setType("gameManager");
			//template2.addServices(sd2);

			try {
				DFAgentDescription[] result = DFService.search(myAgent, template);
				//DFAgentDescription[] result2 = DFService.search(myAgent, template2);
				
				// If Manager is available!
				if (result.length > 0) {
					//System.out.println(myAgent.getAID());
					
					ACLMessage msg = new ACLMessage(ACLMessage.SUBSCRIBE);
					msg.setContentObject(myAgent.getAID());

					for (int i = 0; i < result.length; ++i) {
						msg.addReceiver(result[i].getName());
					}
					myAgent.send(msg);		
				}
				
				// No Manager is available - kill the agent!
				else {
					System.out.println(myAgent.getAID().getLocalName() + ": No Manager available. Agent offline");
				}
				
			} catch (IOException | FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
						
		}
	}

	private class Receiver extends CyclicBehaviour {

		public void action() {
			
			ACLMessage msg = receive();
			if (msg != null) {

				//subscribe is used to add the team manager to the piece and is followed by a request to set its initial Position
				if (msg.getPerformative() == ACLMessage.SUBSCRIBE) {

					try {
							//System.out.println(msg.getSender().getLocalName());
							AID content = (AID) msg.getContentObject();
							//System.out.println("Manager assigned");
							team_manager = content;
							
							ACLMessage initialPos = receive();
							
							if(initialPos != null && initialPos.getPerformative()== ACLMessage.INFORM)
							{
								Position initialPosContent = (Position) initialPos.getContentObject();
								current_location = initialPosContent;
							}
							
							System.out.println(current_location.getX() +":"+current_location.getY());
							
						}
					catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//if it receives a request, the piece nows that it needs to move (or not, it can move to the same spot)
				//when this happens, the piece asks the manager about his surroundings, to make a decision about where to move
				//after asking, piece waits some time to receive reply and to make decision
				else if (msg.getPerformative() == ACLMessage.REQUEST) {
					//String content = msg.getContent();
					if(true)
					{
						System.out.println(myAgent.getAID().getLocalName() + " Asked to move : " + current_location.getX());
						try {
							ArrayList<Position> surr = (ArrayList<Position>) msg.getContentObject();
							surr.add(current_location);
							
							ACLMessage askGM = new ACLMessage(ACLMessage.REQUEST);
							askGM.setContentObject(surr);
							askGM.addReceiver(team_manager);
							myAgent.send(askGM);
							//askSurr.addReceiver(msg.getSender());
							//myAgent.send(askSurr);
							
							Thread.sleep(5000);
							
							ACLMessage nextMove = receive();
							
							
							if(nextMove != null && nextMove.getPerformative() == ACLMessage.CONFIRM)
							{
								Position nextPos = (Position) nextMove.getContentObject();
								System.out.println("Sending Accept Proposal");
								
								ACLMessage moveReply = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
								moveReply.setContentObject(nextPos);
								moveReply.addReceiver(msg.getSender());
								myAgent.send(moveReply);
									
							}
							
							
							
						} catch (UnreadableException | IOException | InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}						
					}
					else
					{
						try {
							//System.out.println("Sending my Position");
							ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
							reply.setContentObject(current_location);
							reply.addReceiver(msg.getSender());
							myAgent.send(reply);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				else if (msg.getPerformative() == ACLMessage.CONFIRM)
				{
					System.out.println("I have been authorized to move");
					
					try {
						Position newPos = (Position) msg.getContentObject();
						current_location = newPos;
						
						
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				}
				
				else if (msg.getPerformative() == ACLMessage.INFORM)
				{
					try {
						//System.out.println(msg.getSender().getLocalName());
						ArrayList<Position> surr = (ArrayList<Position>) msg.getContentObject();
						//System.out.print(surr.get(0).getX());
						//System.out.println(myAgent.getLocalName() + " Received Information about Surroundings");
						//team_manager = content;
						
						//MOVE
						System.out.println("Received Inform");	
						ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
						//add pieces position to the list so manager has access to that information
						surr.add(current_location);
						reply.setContentObject(surr);
						reply.addReceiver(team_manager);
						myAgent.send(reply);
						
						
						
						
					}
					catch (UnreadableException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
		}	
	}
}


}


	
