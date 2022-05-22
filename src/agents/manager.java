package agents;

import java.io.IOException;
import java.util.ArrayList;

import Classes.ANSIConstants;
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
								initialPosition.setContentObject(new Position( n*7, 0));
								pieces.get(n).setPosition(new Position( n*7, 0));
//								if(n == 0)
//								{
//									initialPosition.setContentObject(new Position( 3, 0));
//									pieces.get(n).setPosition(new Position( 3, 0));
//								}
//								else if(n == 1)
//								{
//									initialPosition.setContentObject(new Position( 14, 0));
//									pieces.get(n).setPosition(new Position( 14, 0));
//								}
//								else if(n == 2)
//								{
//									initialPosition.setContentObject(new Position( 16, 0));
//									pieces.get(n).setPosition(new Position( 16, 0));
//								}
//								else if(n == 3)
//								{
//									initialPosition.setContentObject(new Position( 17, 0));
//									pieces.get(n).setPosition(new Position( 17, 0));
//								}
//								else if(n == 4)
//								{
//									initialPosition.setContentObject(new Position( 17, 0));
//									pieces.get(n).setPosition(new Position( 17, 0));
//								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else
						{
							try {
								msg1.setContentObject(managers.get(1));
								initialPosition.setContentObject(new Position(((n-5)*7),34));
								pieces.get(n).setPosition(new Position(((n-5)*7),34));
//								if(n == 5)
//								{
//									initialPosition.setContentObject(new Position( 3, 2));
//									pieces.get(n).setPosition(new Position( 3, 2));
//								}
//								else if(n == 6)
//								{
//									initialPosition.setContentObject(new Position( 3, 4));
//									pieces.get(n).setPosition(new Position( 3, 4));
//								}
//								else if(n == 7)
//								{
//									initialPosition.setContentObject(new Position( 2, 3));
//									pieces.get(n).setPosition(new Position( 2, 3));
//								}
//								else if(n == 8)
//								{
//									initialPosition.setContentObject(new Position( 2, 4));
//									pieces.get(n).setPosition(new Position( 2, 4));
//								}
//								else if(n == 9)
//								{
//									initialPosition.setContentObject(new Position( 33, 8));
//									pieces.get(n).setPosition(new Position( 33, 8));
//								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						msg1.addReceiver(pieces.get(n).getAgent());
						initialPosition.addReceiver(pieces.get(n).getAgent());
						myAgent.send(msg1);
						
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
					if (pieces.get(i).isAvailable()) {
						//System.out.println("Asking to move");
						ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
						try {
							request.setContentObject(checkSurroundings(i));
						} catch (IOException e2) {
							e2.printStackTrace();
						}
						request.addReceiver(pieces.get(i).getAgent());
						myAgent.send(request);
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e2) {
							e2.printStackTrace();
						}
						if (i < 5) {
							System.out.println(ANSIConstants.ANSI_BLUE + "Piece number "+ i + " moving" + ANSIConstants.ANSI_RESET);
							
							for (int aux = 0; aux < 5; aux++) {
								if (aux != i) {
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
						} else {
							System.out.println(ANSIConstants.ANSI_RED + "Piece number "+ i + " moving" + ANSIConstants.ANSI_RESET);
							for (int aux = 5; aux <= 9; aux++) {
								if (aux != i) {
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
						//we need this sleep to allow for each piece to reply, because receive does not wait for a reply
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						ACLMessage reply = receive();
						if (reply != null && reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {

							//Only then do we receive a reply regarding the desired movement of the piece
							try {
								Position movePos = (Position) reply.getContentObject();

								//System.out.print("Received Position piece wants to move to");
								//Movement is allowed
								if (checkMovement(movePos, myAgent, pieces.get(i).getPosition())) {
									System.out.println("Allowed");
									ACLMessage confirmMovement = new ACLMessage(ACLMessage.CONFIRM);
									confirmMovement.addReceiver(pieces.get(i).getAgent());
									confirmMovement.setContentObject(movePos);

									myAgent.send(confirmMovement);

									pieces.get(i).setPosition(movePos);

									//Sleep for piece to change movement

									Thread.sleep(500);

									i++;
								} else {
									System.out.println("Not allowed");
								}

							} catch (UnreadableException | IOException | InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						} 
					}
					else
					{
						System.out.println("Piece no : " + i + "Is eliminated");
						i++;
					}
				}
				
				//Round Final. Check all positions
				
				int eliminatedA =0 , eliminatedB = 0;
				
				for (int row = 0; row < 35; row++)
			      {
			          System.out.println("");
			          //System.out.println("------------------------------------------------------------------------------------------------------------------------------------");

			          for (int column = 0; column < 35; column++)
			          {
			              System.out.print("| ");
			              
			              boolean printed = false;
			              for (int index = 0; index < pieces.size(); index++)
			              {
			            	  if(pieces.get(index).getPosition().getX() == column && pieces.get(index).getPosition().getY() == row)
			            	  {
			            		  printed = true;
			            		  if(index < 5)
			            		  {
			            			  System.out.print(ANSIConstants.ANSI_BLUE + "X "  + ANSIConstants.ANSI_RESET);
			            		  }
			            		  else
			            		  {
			            			  System.out.print(ANSIConstants.ANSI_RED + "X "  + ANSIConstants.ANSI_RESET); 
			            		  }
			            	  }  
			              }
			              if(!printed)
		            	  {
		            		  System.out.print("  ");
		            	  }
			          }           
			    }
			    System.out.println("");
			    //System.out.println("------------------------------------------------------------------------------------------------------------------------------------");
				
				for(int i = 0; i < pieces.size(); i++)
				{
					
					//print positions
					if(i < 5)
					{
						System.out.println(ANSIConstants.ANSI_BLUE + "Piece number " + i + " : " + pieces.get(i).getPosition().getX() + "," + pieces.get(i).getPosition().getY()  + ANSIConstants.ANSI_RESET);
					}
					else
					{
						System.out.println(ANSIConstants.ANSI_RED + "Piece number " + i + " : " + pieces.get(i).getPosition().getX() + "," + pieces.get(i).getPosition().getY()  + ANSIConstants.ANSI_RESET);
					}
					if(checkEliminated(i))
					{
						System.out.println("Eliminate piece number : " + i);
						pieces.get(i).setAvailable(false);
						pieces.get(i).setPosition(new Position(100, 100));
						if(i<5)
						{
							eliminatedA ++;
						}
						else
						{
							eliminatedB ++;
						}
					}
				}
				
				if(eliminatedA >= 2)
				{
					System.out.println("Team B is the Winner");
					GameState = "Finished";
				}
				else if(eliminatedB >= 2)
				{
					System.out.println("Team B is the Winner");
					GameState = "Finished";
				}
				
			}
			else
			{
				//System.out.println("Loading");
			}
			
		}
	}

	private boolean checkMovement(Position replyContent, Agent myAgent, Position previousPosition) throws IOException {
		
		System.out.println(previousPosition.getX() + "," + previousPosition.getY()+ " to " + replyContent.getX() + "," + replyContent.getY());
		
		if(replyContent.getX() > 34 || replyContent.getX() < 0 || replyContent.getY() > 34 || replyContent.getY() < 0)
			return false;
		
		//verificar se apenas se move numa direcao - nao se pode mover na diagonal
		if(replyContent.getX()!= previousPosition.getX() && replyContent.getY()!= previousPosition.getY())
		{
			return false;
		}
		
		//verificar se apenas se move uma unidade
		if( Math.abs(replyContent.getX() - previousPosition.getX()) > 1 || Math.abs(replyContent.getY() - previousPosition.getY()) > 1 )
		{
			return false;
		}
		
		//verificar se existe alguma peça nessa posiçao
		for(int i = 0; i < 10; i ++)
		{							
			if(pieces.get(i).getPosition().getX() == replyContent.getX() && pieces.get(i).getPosition().getY() == replyContent.getY())
			{
				return true;
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
			for (int aux = 5; aux < 10; aux ++)
			{
				//check if its neighbour in X
				if( Math.abs(pieces.get(i).getPosition().getX() - pieces.get(aux).getPosition().getX()) <= 3)
				{
					//check if its neighbour in Y
					if( Math.abs(pieces.get(i).getPosition().getY() - pieces.get(aux).getPosition().getY()) <= 3)
					{
						returnList.add(pieces.get(aux).getPosition());
					}
				}
			}
		}
		else
		{
			for (int aux = 0; aux < 5; aux ++)
			{
				//check if its neighbour in X
				if( Math.abs(pieces.get(i).getPosition().getX() - pieces.get(aux).getPosition().getX()) <= 3)
				{
					//check if its neighbour in Y
					if( Math.abs(pieces.get(i).getPosition().getY() - pieces.get(aux).getPosition().getY()) <= 3)
					{
						returnList.add(pieces.get(aux).getPosition());
					}
				}
			}
		}
		
		return returnList;
		
	}
	
	private boolean checkEliminated(int index)
	{
		int sides = 0;
		if(index < 5)
		{
			for(int i = 5; i< 10; i++)
			{
				if(Math.abs(pieces.get(index).getPosition().getX() - pieces.get(i).getPosition().getX()) == 1)
				{
					if(Math.abs(pieces.get(index).getPosition().getY() - pieces.get(i).getPosition().getY()) == 0)
					{
						sides++;
					}
				}
				else if(Math.abs(pieces.get(index).getPosition().getX() - pieces.get(i).getPosition().getX()) == 0)
				{
					if(Math.abs(pieces.get(index).getPosition().getY() - pieces.get(i).getPosition().getY()) == 1)
					{
						sides++;
					}
				}
		}
		}
		else
		{
			for(int i = 0; i< 5; i++)
			{
				if(Math.abs(pieces.get(index).getPosition().getX() - pieces.get(i).getPosition().getX()) == 1)
				{
					if(Math.abs(pieces.get(index).getPosition().getY() - pieces.get(i).getPosition().getY()) == 0)
					{
						sides++;
					}
				}
				else if(Math.abs(pieces.get(index).getPosition().getX() - pieces.get(i).getPosition().getX()) == 0)
				{
					if(Math.abs(pieces.get(index).getPosition().getY() - pieces.get(i).getPosition().getY()) == 1)
					{
						sides++;
					}
				}
			}
		}
		
		if(sides == 4)
		{
			return true;
		}
		
		return false;
	}
}
	
	
