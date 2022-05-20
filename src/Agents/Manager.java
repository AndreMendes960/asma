package Agents;

import java.io.IOException;
import java.util.ArrayList;

import Classes.InformPosition;
import Classes.MakeRequest;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class Manager extends Agent {

	protected void setup() {
		super.setup();

		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("central");
		sd.setName(getLocalName());
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}

		this.addBehaviour(new Receiver());
	}

	protected void takeDown() {

		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		super.takeDown();

	}

	private class Receiver extends CyclicBehaviour {

		private ArrayList<InformPosition> taxis_position = new ArrayList<InformPosition>();

		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {

				if (msg.getPerformative() == ACLMessage.SUBSCRIBE) {

					try {
						System.out.println(myAgent.getAID().getLocalName() + ": " + msg.getSender().getLocalName()
								+ " registered!");

						InformPosition content = (InformPosition) msg.getContentObject();
						taxis_position.add(content);

					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else if (msg.getPerformative() == ACLMessage.REQUEST) {

					try {
						System.out.println(myAgent.getAID().getLocalName() + ": " + msg.getSender().getLocalName()
								+ " requested a transport!");

						MakeRequest content = (MakeRequest) msg.getContentObject();

						// Search for closest available taxi
						float dist_min = 1000.0f;
						AID closestTaxi = null;
						int list_pos = -1;

						// Calculate distance of each taxi with customer init
						// position
						for (int i = 0; i < taxis_position.size(); i++) {
							if (taxis_position.get(i).isAvailable() == true) {

								float distance = (int) Math.sqrt(((Math.pow(
										(taxis_position.get(i).getPosition().getX() - content.getInit().getX()), 2))
										+ (Math.pow(
												(taxis_position.get(i).getPosition().getY() - content.getInit().getY()),
												2))));

								if (dist_min > distance) {
									closestTaxi = taxis_position.get(i).getAgent();
									list_pos = i;
									dist_min = distance;
								}
							}
						}

						// Taxi Available = send request
						if (closestTaxi != null) {
							System.out.println(myAgent.getAID().getLocalName() + ": Taxi selected:" + closestTaxi);

							ACLMessage mensagem = new ACLMessage(ACLMessage.REQUEST);
							mensagem.addReceiver(closestTaxi);
							mensagem.setContentObject(content);
							myAgent.send(mensagem);

							// Change selected Taxi information
							taxis_position.set(list_pos, new InformPosition(closestTaxi, content.getDest(), false));

						} else {
							// No Taxi Available = send refuse
							System.out.println(myAgent.getAID().getLocalName() + ": No Taxis available!");

							ACLMessage mensagem = msg.createReply();
							mensagem.setPerformative(ACLMessage.REFUSE);
							myAgent.send(mensagem);
						}
					} catch (UnreadableException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// Taxi inform that transportation is complete
				} else if (msg.getPerformative() == ACLMessage.CONFIRM) {

					try {
						InformPosition content = (InformPosition) msg.getContentObject();
						for (int i = 0; i < taxis_position.size(); i++) {

							if (taxis_position.get(i).getAgent().equals(content.getAgent())) {
								System.out.println(myAgent.getAID().getLocalName() + ": Agent "
										+ msg.getSender().getLocalName() + " arrived!");
								taxis_position.set(i, content);
							}
						}

					} catch (UnreadableException e) {
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
