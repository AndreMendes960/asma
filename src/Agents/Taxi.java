package Agents;

import java.io.IOException;
import java.util.Random;

import Classes.InformPosition;
import Classes.MakeRequest;
import Classes.Position;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class Taxi extends Agent {
	
	private InformPosition current_location;

	protected void setup() {
		super.setup();

		this.addBehaviour(new Register_taxi());
		this.addBehaviour(new Receiver());
	}

	protected void takeDown() {
		super.takeDown();
	}

	private class Register_taxi extends OneShotBehaviour {
		public void action() {

			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("central");
			template.addServices(sd);

			try {
				DFAgentDescription[] result = DFService.search(myAgent, template);

				// If Manager is available!
				if (result.length > 0) {
					
					Random rand = new Random();
					current_location = new InformPosition(myAgent.getAID(),
							new Position(rand.nextInt(100), rand.nextInt(100)), true);
					
					ACLMessage msg = new ACLMessage(ACLMessage.SUBSCRIBE);
					msg.setContentObject(current_location);

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

				if (msg.getPerformative() == ACLMessage.REQUEST) {

					try {

						current_location.setAvailable(false);

						// Taxi takes 1 second per transportation
						Thread.sleep(1000);

						// Update current position and make Taxi available
						MakeRequest content = (MakeRequest) msg.getContentObject();
						current_location = new InformPosition(myAgent.getAID(), content.getDest(), true);

						// Send Confirm Message to Customer and Manager
						ACLMessage mensagem = new ACLMessage(ACLMessage.CONFIRM);
						mensagem.setContentObject(current_location);

						// Add Customer AID to the list of receivers of the
						// ACLMessage
						mensagem.addReceiver(content.getAgent());
						mensagem.addReceiver(msg.getSender());
						
						myAgent.send(mensagem);

					} catch (UnreadableException | InterruptedException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			} else {
				block();
			}
		}
	}
}
