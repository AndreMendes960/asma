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

public class manager extends Agent {
	
	//typically, in the array, we know that the first 5 indexes belong to team A and last 5 indexes belong to Team B.
	//Might need adjusting
	private ArrayList<InformPosition> pieces;
	private ArrayList<AID> managers;
	
	private String GameState;
	
	protected void setup() {
		super.setup();

		pieces = new ArrayList<InformPosition>();
		managers = new ArrayList<AID>();
		GameState = new String();
		
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
		this.addBehaviour(new GameCycle());
	}

	protected void takeDown() {

		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		super.takeDown();

	}
	
	private Position AskPosition(Agent myAgent, AID agent)
	{
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setContent("Asking position");

		msg.addReceiver(agent);
		myAgent.send(msg);	
		
		ACLMessage reply = receive();
		if (reply != null) {

			if (reply.getPerformative() == ACLMessage.INFORM) {

				try {
					//System.out.println(myAgent.getAID().getLocalName() + ": " + msg.getSender().getLocalName() + "Sent a team member!");

					Position content = (Position) reply.getContentObject();
					
					System.out.println(myAgent.getAID().getLocalName() + " : " + reply.getSender().getLocalName() + " : " + content.getX());
					return content;

				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}
	
	private class Receiver extends CyclicBehaviour {

		public void action() {
			
			ACLMessage msg = receive();
			if (msg != null) {

				if (msg.getPerformative() == ACLMessage.SUBSCRIBE) {

					try {
						if(msg.getSender().getLocalName().contains("piece"))
						{
							//System.out.println(msg.getSender().getLocalName());
							AID content = (AID) msg.getContentObject();
							
							InformPosition newPiece = new InformPosition(content, 0, 0, true);
							
							pieces.add(newPiece);
						}
						else
						{
							//System.out.println(msg.getSender().getLocalName());
							AID content = (AID) msg.getContentObject();
							managers.add(content);
						}
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//Este bloco de código apenas ocorre quando existe o último subscribe
				//Sends the pieces to each manager and the manager to each piece
				if(managers.size() == 2 && pieces.size() == 10)
				{
					for (int n = 0; n < 2; n++)
					{
						try {		
							for (int  i = 0; i < 5 ; i++)
							{
								ACLMessage msg1 = new ACLMessage(ACLMessage.SUBSCRIBE);
								msg1.setContentObject(pieces.get(i + (n*5)).getAgent());
								msg1.addReceiver(managers.get(n));
								myAgent.send(msg1);
							}
							
							
							//System.out.print(managers.get(n));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					
					for (int n = 0; n < 10; n++)
					{
						ACLMessage msg1 = new ACLMessage(ACLMessage.SUBSCRIBE);
						ACLMessage initialPosition = new ACLMessage(ACLMessage.INFORM);
						if(n < 5)
						{
							try {
								msg1.setContentObject(managers.get(0));
								initialPosition.setContentObject(new Position(0, n*7));
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else
						{
							try {
								msg1.setContentObject(managers.get(1));
								initialPosition.setContentObject(new Position(34, (n*7) - ((n-1)*7)));
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						msg1.addReceiver(pieces.get(n).getAgent());
						initialPosition.addReceiver(pieces.get(n).getAgent());
						myAgent.send(msg1);
						
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						myAgent.send(initialPosition);
					}
					
					//Game is ready to start
					GameState = "Start";
					
				}
			}
		
		}
	}

	//handles the cycle of a natural game. It needs to check if the conditions for the game to start are met
	//If they are, iterates the pieces array asking each piece to move. On moving, the piece
	private class GameCycle extends CyclicBehaviour{

		public void action() {
			
			if(GameState == "Start")
			{
				for (int i = 0; i < 10;)
				{
					//System.out.println("Asking to move");
					ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
					try {
						request.setContentObject(checkSurroundings(i));
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					request.addReceiver(pieces.get(i).getAgent());
					myAgent.send(request);
					
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					
					if(i < 5)
					{
						for (int aux = 0; aux < 5; aux++)
						{
							if(aux != i) 
							{
								ACLMessage surrInform = new ACLMessage(ACLMessage.INFORM);
								try {
									surrInform.setContentObject(checkSurroundings(aux));
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								surrInform.addReceiver(pieces.get(aux).getAgent());
								myAgent.send(surrInform);
							}
						}
					}
					else
					{
						for (int aux = 5; aux <= 9; aux++)
						{
							if(aux != i) 
							{
								ACLMessage surrInform = new ACLMessage(ACLMessage.INFORM);
								try {
									surrInform.setContentObject(checkSurroundings(aux));
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								surrInform.addReceiver(pieces.get(aux).getAgent());
								myAgent.send(surrInform);
								
								System.out.println("Sent Inform");	
							}
						}
					}
					//we need this sleep to allow for each piece to reply, because receive does not wait for a reply
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					ACLMessage reply = receive();
					
					if(reply != null && reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL)
					{
						
						//Only then do we receive a reply regarding the desired movement of the piece
						try {
							Position movePos = (Position) reply.getContentObject();
							
							System.out.print("Received Position piece wants to move to");
							if(checkMovement(movePos, myAgent))
							{
								System.out.println("Allowed");
								i++;
							}
							else
							{
								System.out.println("Not allowed");
							}
						} catch (UnreadableException | IOException  e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}			
				}
				
			}
			else
			{
				//System.out.println("Loading");
			}
			
		}
	}

	private boolean checkMovement(Position replyContent, Agent myAgent) throws IOException {
			
			System.out.println("Checking Movement");
			
			for(int i = 0; i < 10; i ++)
			{
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.setContentObject("Checking movement");

				
				msg.addReceiver(pieces.get(i).getAgent());
				myAgent.send(msg);	
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				ACLMessage reply = receive();
				if (reply != null) {

					if (reply.getPerformative() == ACLMessage.INFORM) {

						try {
							//System.out.println(myAgent.getAID().getLocalName() + ": " + msg.getSender().getLocalName() + "Sent a team member!");

							Position content = (Position) reply.getContentObject();
						
							
							if(content.getX() == replyContent.getX() && content.getY() == replyContent.getY())
							{
								return false;
							}
							//team.add(content);

						} catch (UnreadableException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			
			}
		return true;
		}
	
	private ArrayList<Position> checkSurroundings(int i)
	{
		ArrayList<Position> returnList = new ArrayList<Position>();
		//we know it is in the first team
		if(i < 5)
		{
			for (int aux = 0; aux < 5; aux ++)
			{
				//check if its neighbour in X
				if(pieces.get(i).getPosition().getX() - 7 < pieces.get(aux).getPosition().getX() || pieces.get(i).getPosition().getX() + 7 > pieces.get(aux).getPosition().getX())
				{
					//check if its neighbour in Y
					if(pieces.get(i).getPosition().getY() - 7 < pieces.get(aux).getPosition().getY() || pieces.get(i).getPosition().getY() + 7 > pieces.get(aux).getPosition().getY())
					{
						returnList.add(pieces.get(aux).getPosition());
					}
				}
			}
		}
		else
		{
			for (int aux = 5; aux < 10; aux ++)
			{
				//check if its neighbour in X
				if(pieces.get(i).getPosition().getX() - 7 < pieces.get(aux).getPosition().getX() || pieces.get(i).getPosition().getX() + 7 > pieces.get(aux).getPosition().getX())
				{
					//check if its neighbour in Y
					if(pieces.get(i).getPosition().getY() - 7 < pieces.get(aux).getPosition().getY() || pieces.get(i).getPosition().getY() + 7 > pieces.get(aux).getPosition().getY())
					{
						returnList.add(pieces.get(aux).getPosition());
					}
				}
			}
		}
		
		return returnList;
		
	}
	
}
	
	
