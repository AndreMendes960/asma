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

public class gameManager extends Agent{
	
	private ArrayList<AID> team;
	
	Position target;
	String targetUpper;
	String targetLower;
	String targetLeft;
	String targetRight;
	
	String textColor;
	
	int minDistance = 9999;
	
	int roundIncr = 0;
	
	private String teamName = null;
	
	protected void setup() {
		super.setup();
		
		team = new ArrayList<AID>();
		target = new Position(0,0);
		
		targetUpper = null;
		targetLower = null;
		targetLeft = null;
		targetRight = null;
		textColor = null;		
		
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
						try {
							ArrayList<Position> movingPieceList =(ArrayList<Position>) msg.getContentObject();
							
							if(teamName == null)
							{
								if(movingPieceList.get(movingPieceList.size() - 1).getY() == 0)
								{
									teamName = "Team A";
									textColor = ANSIConstants.ANSI_BLUE;
								}
								else	
								{
									teamName = "Team B";
									textColor = ANSIConstants.ANSI_RED;
								}
							}
							
							Thread.sleep(3000);

							ArrayList<ArrayList<Position>> listAllPieces = new ArrayList<ArrayList<Position>>();
							
							listAllPieces.add(movingPieceList);
						
							for(int i = 0; i<4; i++) 
							{
								ACLMessage replies = receive();
								
								if(replies != null && replies.getPerformative() == ACLMessage.INFORM)
								{
									//System.out.println("receiving replies from other pieces");
									listAllPieces.add((ArrayList<Position>) replies.getContentObject());
								}
								
							}
							
							int inc = 0;
							for(int i = 0; i < listAllPieces.size(); i++)
							{
								//check size of all arrays. If all of them contain only 1 Piece, there are no enemy pieces on the surroundings
								if(listAllPieces.get(i).size() > 1)
								{

									inc++;
								}
								
							}
							
							Position moveToPos = new Position(movingPieceList.get(movingPieceList.size()-1).getX(),movingPieceList.get(movingPieceList.size()-1).getY());
							
							if(inc == 0)
							{
								System.out.println( textColor + "No enemies close by. Exploring" + ANSIConstants.ANSI_RESET);
								
								if(teamName == "Team A")
								{
									moveToPos.setY(movingPieceList.get(movingPieceList.size() - 1).getY() + 1);
								}
								else
								{
									moveToPos.setY(movingPieceList.get(movingPieceList.size() - 1).getY() - 1);
								}
								
							}
							else
							{
								System.out.println(textColor + "Enemies Found" + ANSIConstants.ANSI_RESET);
								//first, find the closest enemies to all the pieces
								
								//reset minDistance in case target moved
								for(int i = 0; i < listAllPieces.size(); i++)
								{
									//check if the size is bigger than 1. If it isnt, means the list only contains the position of the piece and not enemy pieces
									if(listAllPieces.get(i).size() > 1)
									{
										for(int j = 0; j < listAllPieces.get(i).size() - 1; j++)
										{
											Position posToCalculate = listAllPieces.get(i).get(j);											
											//calculate the sum of all distances
											int newDist = 0;
											for (int k = 0; k < team.size(); k++)
											{
												newDist = newDist + Math.abs(posToCalculate.getX() - listAllPieces.get(k).get(listAllPieces.get(k).size() - 1).getX()) +  Math.abs(posToCalculate.getY() - listAllPieces.get(k).get(listAllPieces.get(k).size() - 1).getY());
											}
											
											//System.out.println("For piece : " + posToCalculate.getX() + ","+ posToCalculate.getY() + "distance is : " + newDist);
											
											if(newDist < minDistance && target.getX() != posToCalculate.getX() && target.getY() != posToCalculate.getY())
											{
												//if the target is the same, we dont need to change 
												//if it is different, reset all the positions
												minDistance = newDist;
												target = posToCalculate;
												targetUpper = null;
												targetLower = null;
												targetRight = null;
												targetLeft = null;
												
											}
										}
									}	
								}

								System.out.println(textColor + "Target is : " + target.getX() + "," + target.getY()  + ANSIConstants.ANSI_RESET);
								
								//We check if the piece is alone. If it is alone in comparison to all the other members, we then move this piece towards the group
								
								ArrayList<Integer> TeamPosX = new ArrayList<Integer>();
								ArrayList<Integer> TeamPosY = new ArrayList<Integer>();
								
								for( int z = 0; z< 5 ; z++)
								{
									//check if piece is eliminated
									if(listAllPieces.get(z).get(listAllPieces.get(z).size() - 1).getX() != 100)
									{
										TeamPosX.add(listAllPieces.get(z).get(listAllPieces.get(z).size() - 1).getX());
										TeamPosY.add(listAllPieces.get(z).get(listAllPieces.get(z).size() - 1).getY());
									}
								}
								
								//check the median value
								double medianX, medianY;
								
								if (TeamPosX.size() % 2 == 0)
								    medianX = (TeamPosX.get(TeamPosX.size()/2) + TeamPosX.get(TeamPosX.size()/2 - 1))/2;
								else
								    medianX = (TeamPosX.get(TeamPosX.size()/2));
								if (TeamPosY.size() % 2 == 0)
								    medianY = (TeamPosY.get(TeamPosY.size()/2) + TeamPosY.get(TeamPosY.size()/2 - 1))/2;
								else
								    medianY = (TeamPosY.get(TeamPosY.size()/2));
								
								if(Math.abs(movingPieceList.get(movingPieceList.size() - 1).getX() - medianX) > 5)
								{
									System.out.println(textColor + "Too far away, approaching Team" + ANSIConstants.ANSI_RESET);
								}
								else if(Math.abs(movingPieceList.get(movingPieceList.size() - 1).getY() - medianY) > 5)
								{
									System.out.println(textColor + "Too far away, approaching Team" + ANSIConstants.ANSI_RESET);
								}
								else
								{
									//the piece takes the targets left side
									if(targetLeft == null || targetLeft.equals(msg.getSender().getLocalName()))
										targetLeft = msg.getSender().getLocalName();
										//already in the correct X position, move in Y
										if(movingPieceList.get(movingPieceList.size()-1).getX() == target.getX() - 1)
										{
											if(movingPieceList.get(movingPieceList.size()-1).getY() < target.getY())
											{
												moveToPos = new Position(movingPieceList.get(movingPieceList.size()-1).getX(), movingPieceList.get(movingPieceList.size()-1).getY() + 1);
											}
											else if(movingPieceList.get(movingPieceList.size()-1).getY() > target.getY())
											{
												moveToPos = new Position(movingPieceList.get(movingPieceList.size()-1).getX(), movingPieceList.get(movingPieceList.size()-1).getY() - 1);
											}
										}
										else
										{
											if(movingPieceList.get(movingPieceList.size()-1).getX() < target.getX()-1)
											{
												moveToPos = new Position(movingPieceList.get(movingPieceList.size()-1).getX() +1, movingPieceList.get(movingPieceList.size()-1).getY());
											}
											else if(movingPieceList.get(movingPieceList.size()-1).getX() > target.getX()-1)
											{
												moveToPos = new Position(movingPieceList.get(movingPieceList.size()-1).getX() - 1, movingPieceList.get(movingPieceList.size()-1).getY());
											}
										}
									}
									else if( targetUpper == null || targetUpper.equals(msg.getSender().getLocalName()) )
									{
										targetUpper = msg.getSender().getLocalName();
										//already in the correct Y position, move in X
										if(movingPieceList.get(movingPieceList.size()-1).getY() == target.getY() - 1)
										{
											if(movingPieceList.get(movingPieceList.size()-1).getX() < target.getX())
											{
												moveToPos = new Position(movingPieceList.get(movingPieceList.size()-1).getX() + 1, movingPieceList.get(movingPieceList.size()-1).getY());
											}
											else if(movingPieceList.get(movingPieceList.size()-1).getX() > target.getX())
											{
												moveToPos = new Position(movingPieceList.get(movingPieceList.size()-1).getX() - 1, movingPieceList.get(movingPieceList.size()-1).getY());
											}
										}
										else
										{
											if(movingPieceList.get(movingPieceList.size()-1).getY() < target.getY() - 1)
											{
												moveToPos = new Position(movingPieceList.get(movingPieceList.size()-1).getX(), movingPieceList.get(movingPieceList.size()-1).getY() + 1);
											}
											else if(movingPieceList.get(movingPieceList.size()-1).getY() > target.getY() - 1)
											{
												moveToPos = new Position(movingPieceList.get(movingPieceList.size()-1).getX(), movingPieceList.get(movingPieceList.size()-1).getY() - 1);
											}
										}
									}
									else if( targetLower == null || targetLower.equals(msg.getSender().getLocalName()))
									{
										targetLower = msg.getSender().getLocalName();
										//already in the correct Y position, move in X
										if(movingPieceList.get(movingPieceList.size()-1).getY() == target.getY() + 1)
										{
											if(movingPieceList.get(movingPieceList.size()-1).getX() < target.getX())
											{
												moveToPos = new Position(movingPieceList.get(movingPieceList.size()-1).getX() + 1, movingPieceList.get(movingPieceList.size()-1).getY());
											}
											else if (movingPieceList.get(movingPieceList.size()-1).getX() > target.getX())
											{
												moveToPos = new Position(movingPieceList.get(movingPieceList.size()-1).getX() - 1, movingPieceList.get(movingPieceList.size()-1).getY());
											}
										}
										else
										{
											if(movingPieceList.get(movingPieceList.size()-1).getY() < target.getY()+1)
											{
												
												moveToPos = new Position(movingPieceList.get(movingPieceList.size()-1).getX(), movingPieceList.get(movingPieceList.size()-1).getY() + 1);
											}
											else if (movingPieceList.get(movingPieceList.size()-1).getY() > target.getY()+1)
											{
												
												moveToPos = new Position(movingPieceList.get(movingPieceList.size()-1).getX(), movingPieceList.get(movingPieceList.size()-1).getY() - 1);
											}
										}
									}
									else if( targetRight == null || targetRight.equals(msg.getSender().getLocalName()))
									{
										targetRight = msg.getSender().getLocalName();
										//already in the correct X position, move in Y
										if(movingPieceList.get(movingPieceList.size()-1).getX() == target.getX() + 1)
										{
											if(movingPieceList.get(movingPieceList.size()-1).getY() < target.getY())
											{
												moveToPos = new Position(movingPieceList.get(movingPieceList.size()-1).getX(), movingPieceList.get(movingPieceList.size()-1).getY() + 1);
											}
											else if (movingPieceList.get(movingPieceList.size()-1).getY() > target.getY())
											{
												moveToPos = new Position(movingPieceList.get(movingPieceList.size()-1).getX(), movingPieceList.get(movingPieceList.size()-1).getY() - 1);
											}
										}
										else
										{
											if(movingPieceList.get(movingPieceList.size()-1).getX() < target.getX() + 1)
											{
												moveToPos = new Position(movingPieceList.get(movingPieceList.size()-1).getX() +1, movingPieceList.get(movingPieceList.size()-1).getY());
											}
											else if (movingPieceList.get(movingPieceList.size()-1).getX() > target.getX() + 1)
											{
												moveToPos = new Position(movingPieceList.get(movingPieceList.size()-1).getX() - 1, movingPieceList.get(movingPieceList.size()-1).getY());
											}
										}
									}
								}
									
							}

							//at the end of everything, we just have to send a position  to the piece
							
							ACLMessage posReply = new ACLMessage(ACLMessage.CONFIRM);
							
							
							//posReply.setContentObject(moveToPos);
							posReply.setContentObject(moveToPos);
							
							posReply.addReceiver(msg.getSender());
							
							myAgent.send(posReply);
							
							//we use this variable for at the end of the round, reset the target
							roundIncr++;
							
							if(roundIncr == 5)
							{
								roundIncr =0;
								minDistance = 9999;
								target = new Position(0,0);
								targetLeft = null;
								targetRight = null;
								targetUpper = null;
								targetLower = null;
							}
							
							
						} catch (IOException | InterruptedException | UnreadableException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
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
