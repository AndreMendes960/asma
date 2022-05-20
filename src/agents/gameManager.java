package agents;

import java.io.IOException;
import java.util.ArrayList;

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

public class gameManager extends Agent{
	
	private ArrayList<AID> team;
	
	protected void setup() {
		super.setup();
		
		team = new ArrayList<AID>();
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("gameManager");
		sd.setName(getLocalName());
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}

		this.addBehaviour(new Receiver());
		this.addBehaviour(new Register());
	}
	
	//usado para que as peças possam ser atribuidas a cada gameManager
	private class Receiver extends CyclicBehaviour {


		public void action() {
			
			if(team.size() < 5)
			{
				ACLMessage msg = receive();
				if (msg != null) {

					if (msg.getPerformative() == ACLMessage.SUBSCRIBE) {

						try {
							//System.out.println(myAgent.getAID().getLocalName() + "Message recevied : " + msg.getSender().getLocalName() + "Sent a team member!")
							AID content = (AID) msg.getContentObject();
							//System.out.println(myAgent.getAID().getLocalName() + " added piece " + content.getLocalName() + " to the team!");
							team.add(content);

						} catch (UnreadableException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			else
			{
				ACLMessage msg = receive();
				if(msg != null)
				{
					if(msg.getPerformative() == ACLMessage.REQUEST)
					{
						System.out.println("Someone wants to move");
						
						try {
							Thread.sleep(4000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						for(int i = 0; i<4; i++) 
						{
							ACLMessage replies = receive();
							
							if(replies != null && replies.getPerformative() == ACLMessage.INFORM)
							{
								System.out.println("receiving replies from other pieces");
							}
							
						}
						
						//at the end of everything, we just have to send a position  to the piece
						
						ACLMessage posReply = new ACLMessage(ACLMessage.CONFIRM);
						
						try {
							posReply.setContentObject(new Position(110,0));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						posReply.addReceiver(msg.getSender());
						
						myAgent.send(posReply);
						
					}
				}
				
				
			}
		}
	}
	
	//usado para se registar ao manager
	private class Register extends OneShotBehaviour {
		public void action() {

			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("central");
			template.addServices(sd);

			try {
				DFAgentDescription[] result = DFService.search(myAgent, template);
				
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
}
