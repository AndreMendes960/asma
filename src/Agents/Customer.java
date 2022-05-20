package Agents;

import java.io.IOException;
import java.util.Random;

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

public class Customer extends Agent {

	protected void setup() {
		super.setup();

		this.addBehaviour(new Request());
		this.addBehaviour(new Reply());
	}

	protected void takeDown() {
		super.takeDown();
	}

	private class Request extends OneShotBehaviour {

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
					Position init = new Position(rand.nextInt(100), rand.nextInt(100));
					Position dest = new Position(rand.nextInt(100), rand.nextInt(100));

					MakeRequest mr = new MakeRequest(myAgent.getAID(), init, dest);
					ACLMessage mensagem = new ACLMessage(ACLMessage.REQUEST);

					for (int i = 0; i < result.length; ++i) {
						mensagem.addReceiver(result[i].getName());
					}

					mensagem.setContentObject(mr);
					myAgent.send(mensagem);
				}
			} catch (IOException | FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private class Reply extends CyclicBehaviour {

		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {
				if (msg.getPerformative() == ACLMessage.CONFIRM) {
					// If where transportation was completed!
					myAgent.doDelete();
				} else {
					// If where no taxis were available!
					myAgent.doDelete();
				}
			} else {
				block();
			}
		}
	}

}
