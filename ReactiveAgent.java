import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.List;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Random;

public class ReactiveAgent extends Frame 
{
	//private data field
	private String myID;
	private static final int MAEDENPORT = 7237;		// uses port 1237 on localhost
	//The agent's location on the visual array.
	private static final int PLAYERY = 5;
	private static final int PLAYERX = 2;
	
	//The size of the visual field;
	public static final int HEIGHT = 7;
	public static final int WIDTH = 5;
	
	private GridClient gc;
	private SensoryPacket sp;
	/*
	 * ReactiveAgent constructor takes a string and an int
	 * and creates a socket and connects with a serverSocket
	 * PRE: h is a string and p is an int (preferably above 1024)
	 * POST: GridClient connects to Grid via network sockets
	 */
	 
	//Constructor.
  private ReactiveAgent(String h, int p) 
	{
		gc = new GridClient(h, p); //Makes the client and connects to the socket.
		String line = "";
	}
	
	/**
	*Where the main chunk of logic for the program is located.
	*Run requires no arguments and returns no value
	*Used in the main method.
	**/
	/*
	Actions the Agent can take:
	f: move forward
	b: move backward
	r: turn right
	l: turn left"
	g: grab an object in the current spot
	d: drop an object currently being carried
  u: apply a carried object (tool or food)
	a: attack an agent ahead
	w: wait
	k: remove yourself from world
	?: print this help information
	*/
	private void run()
	{
		//Declaring variables.
		String direction;
		String[] rawSenseData;
		String badSight;
		char[][] sight;
		List<Character> groundContents;
		List<Character> inventory;
		
		while(true)
		{
			sp = gc.getSensoryPacket(); //Gets a new sensory packet after every move.
			rawSenseData = sp.getRawSenseData(); //List of rawsensedata; only using it for the sight field.
			badSight = rawSenseData[2]; //The unprocessed string of sense data.
			direction = sp.getSmell(); //Smell for direction.
			sight = processRetinalField(badSight); //Processes badSight to produce a 2d array of chars.
			printRetinalField(sight); //Prints the retinal field on the command prompt.
			groundContents = sp.getGroundContents(); //Items on the ground under Agent.
			inventory = sp.getInventory(); //Agent's inventory
			
			if(groundContents.contains('T') || groundContents.contains('K')) //If there is a key or a hammer underneath agent.
				gc.effectorSend("g"); //Grab it.
			
			else if(!sp.getLastActionStatus())
			{
				//Any permutation where you'd get stuck in a corner with an obstacle in front and on the right.
				//Looks like bad code to me, but it works so *shrug*
				if(((checkWallForwards(sight) == 1) && (checkWallRight(sight) == 1)) || ((checkWallForwards(sight) == 1) && (checkBoulderRight(sight) == 1) && (!inventory.contains('T'))) || ((checkWallForwards(sight)) == 1 && (checkDoorRight(sight) == 1) && (!inventory.contains('K'))) //Wall in front.
					|| ((checkBoulderForwards(sight) == 1) && (checkWallRight(sight) == 1) && (!inventory.contains('T'))) || ((checkBoulderForwards(sight) == 1) && (checkBoulderRight(sight) == 1) && (!inventory.contains('T'))) || ((checkBoulderForwards(sight)) == 1 && (checkDoorRight(sight) == 1) && (!inventory.contains('K') && (!inventory.contains('T'))) //Boulder in front.
					|| ((checkDoorForwards(sight) == 1) && (checkWallRight(sight) == 1) && (!inventory.contains('K'))) || ((checkDoorForwards(sight) == 1) && (checkBoulderRight(sight) == 1) && (!inventory.contains('T')) && (!inventory.contains('K'))) || ((checkDoorForwards(sight)) == 1 && (checkDoorRight(sight) == 1)) && (!inventory.contains('K')))) //Door in front.
					gc.effectorSend("l"); //Move left to get out.
				else
					gc.effectorSend("r"); //Move right to get out.
			}
			
			else
			{
				if(checkPickups(sight)) //Check for pickups in the field.
					moveTowardsPickups(sight); //If there are pickups, try to reach them.
				else
					moveTowardsDirection(direction, sight); //Else, try to follow your nose.
			}
		}
	}
	
	/**
	*Intended to move the agent towards the goal of the cheese.
	*Tries to avoid walls
	*also checks inventory and avoids/destroys boulders and doors
	**/
	//Looking back at it, I should have made a lot more variables for the repeated code.
	private void moveTowardsDirection(String direction, char[][] sight)
	{
		List<Character> inventory = sp.getInventory(); //Gets the agent's inventory so it can tell if it has a usable item.
		switch(direction)
			{
				case "f":
					if(checkWallForwards(sight) == 1) //Wall in front.
						randomTurn(); //Turn a direction.
					else if(checkBoulderForwards(sight) == 1) //Boulder in front.
					{
						if(inventory.contains('T')) //If you have a hammer
							gc.effectorSend("u"); //Use it
						else
							randomTurn(); //Else, turn
					}
					else if(checkDoorForwards(sight) == 1) //Door in front.
					{
						if(inventory.contains('K')) //If you have a key
							gc.effectorSend("u"); //Use it
						else
							randomTurn(); //Else, turn
					}
					else
						gc.effectorSend(direction); //No obstacles? Go in the direction then!
					break;
				case "b": //In all instances just turn.
					if(checkWallBackwards(sight) == 1)
						randomTurn();
					else if(checkBoulderBackwards(sight) == 1)
						randomTurn();
					else if(checkDoorBackwards(sight) == 1)
						randomTurn();
					else
						gc.effectorSend(direction);//No obstacles? Go in the direction then!
					break;
				case "l":
					if(checkWallLeft(sight) == 1)
						gc.effectorSend("f");
					else if(checkBoulderLeft(sight) == 1)
					{
						if(inventory.contains('T')) //If you have a hammer.
							gc.effectorSend("l"); //Turn towards the boulder.
						else
							gc.effectorSend("f"); //Else go forwards.
					}
					else if(checkDoorLeft(sight) == 1)
					{
						if(inventory.contains('K')) //If you have a Key.
							gc.effectorSend("l"); //Turn towards door.
						else
							gc.effectorSend("f"); //Else go forwards.
					}
					else
						gc.effectorSend(direction);//No obstacles? Go in the direction then!
					break;
				case "r":
					if(checkWallRight(sight) == 1)
						gc.effectorSend("f");
					else if(checkBoulderRight(sight) == 1)
					{
						if(inventory.contains('T')) //If you have a hammer.
							gc.effectorSend("r"); //Turn towards the boulder.
						else
							gc.effectorSend("f"); //Else go forwards.
					}
					else if(checkDoorRight(sight) == 1)
					{
						if(inventory.contains('K')) //If you have a Key.
							gc.effectorSend("r"); //Turn towards the Door.
						else
							gc.effectorSend("f"); //Else go forwards.
					}
					else
						gc.effectorSend(direction);//No obstacles? Go in the direction then!
					break;
				case "h": //You're on the cheese!
					gc.effectorSend("g"); //Grab cheese.
					if(inventory.contains('+'))
						gc.effectorSend("u"); //Eat cheese.
				default:
					gc.effectorSend(direction);//No obstacles? Go in the direction then!
			}
	}
	
	/**
	*randomTurn takes no arguments and returns nothing.
	*Turns the agent in a random direction.
	*Supposed to help get off of walls.
	**/
	private void randomTurn()
	{
		int random = (int)(Math.random() * 100);
		if(random <= 50)
		{
			gc.effectorSend("l");
		}
		else
		{
			gc.effectorSend("r");
		}
	}
	/**
	*The next few methods check each direction for walls.
	*The player is at (5, 2) row 5, column 2.
	*All methods take the 2d sight array
	*All methods return an int distance
	*All methods return 0 if there are no walls in that direction.
	**/
	private int checkWallForwards(char[][] sight) //Player is at (5, 2)
	{
		for(int i=PLAYERY; i >= 0; i--)
		{
			if(sight[i][PLAYERX]== '*')
				return (PLAYERY - i); //returns distance to wall
		}
		return 0;
	}
	private int checkWallBackwards(char[][] sight)
	{
		for(int i=PLAYERY; i <= 6; i++)
		{
			if(sight[i][PLAYERX] == '*')
				return (i - PLAYERY); //returns distance to wall
		}
		return 0; //If no walls
	}
	private int checkWallRight(char[][] sight)
	{
		for(int i=PLAYERX; i <= 4; i++)
		{
			if(sight[PLAYERY][i]== '*')
				return (4 - i); //returns distance
		}
		return 0;
	}
	private int checkWallLeft(char[][] sight)
	{
		for(int i=0; i < PLAYERX; i++)
		{
			if(sight[PLAYERY][i]== '*')
				return (PLAYERX - i); //returns distance
		}
		return 0;
	}
	/**
	*The next few methods check each direction for boulders.
	*Like the wall checkers:
	*All methods take the 2d sight array
	*All methods return an int distance
	*All methods return 0 if there are no walls in that direction.
	**/
	private int checkBoulderForwards(char[][] sight) //Player is at (5, 2)
	{
		for(int i=PLAYERY; i >= 0; i--)
		{
			if(sight[i][PLAYERX]== '@')
				return (PLAYERY - i); //returns distance to wall
		}
		return 0;
	}
	private int checkBoulderBackwards(char[][] sight)
	{
		for(int i=PLAYERY; i <= 6; i++)
		{
			if(sight[i][PLAYERX] == '@')
				return (i - PLAYERY); //returns distance to wall
		}
		return 0; //If no walls
	}
	private int checkBoulderRight(char[][] sight)
	{
		for(int i=PLAYERX; i <= 4; i++)
		{
			if(sight[PLAYERY][i]== '@')
				return (4 - i); //returns distance
		}
		return 0;
	}
	private int checkBoulderLeft(char[][] sight)
	{
		for(int i=0; i < PLAYERX; i++)
		{
			if(sight[PLAYERY][i]== '@')
				return (PLAYERX - i); //returns distance
		}
		return 0;
	}
	/**
	*The next few methods check each direction for Doors.
	*Like the wall and boulder checkers:
	*All methods take the 2d sight array
	*All methods return an int distance
	*All methods return 0 if there are no walls in that direction.
	**/
	private int checkDoorForwards(char[][] sight) //Player is at (5, 2)
	{
		for(int i=PLAYERY; i >= 0; i--)
		{
			if(sight[i][PLAYERX]== '#')
				return (PLAYERY - i); //returns distance to Door
		}
		return 0;
	}
	private int checkDoorBackwards(char[][] sight)
	{
		for(int i=PLAYERY; i <= 6; i++)
		{
			if(sight[i][PLAYERX] == '#')
				return (i - PLAYERY); //returns distance to Door
		}
		return 0; //If no walls
	}
	private int checkDoorRight(char[][] sight)
	{
		for(int i=PLAYERX; i <= 4; i++)
		{
			if(sight[PLAYERY][i]== '#')
				return (4 - i); //returns distance
		}
		return 0;
	}
	private int checkDoorLeft(char[][] sight)
	{
		for(int i=0; i < PLAYERX; i++)
		{
			if(sight[PLAYERY][i]== '#')
				return (PLAYERX - i); //returns distance
		}
		return 0;
	}
	
	private boolean checkPickups(char[][] sight)
	{
		for(int row = 0; row < 7; row++)
		{
			for(int column = 0; column < 5; column++)
			{
				if(sight[row][column] == 'K' || sight[row][column] == 'T') //return true if there's a pickup in the visual field
					return true;
			}
		}
		return false; //else return false
	}
	/**
	*Checks the whole field for a pickup (In our case a hammer or a key)
	*Will then move towards them using the moveTowardsDirection
	**/
	private void moveTowardsPickups(char[][] sight) //Checks to see if there are pickups in the visual array, and if so, moves towards them.
	{
		for(int row = 0; row < 7; row++)
		{
			for(int column = 0; column < 5; column++)
			{
				if(sight[row][column] == 'K' || sight[row][column] == 'T')
				{
					if(row < PLAYERY) //The pickup is in front of the player somewhere.
						moveTowardsDirection("f", sight);
					else if(row == PLAYERY) //The pickup is on the same row as the player.
					{
						if(column < PLAYERX)
							moveTowardsDirection("l", sight);
						else if(column > PLAYERX)
							moveTowardsDirection("r", sight);
					}
					else //The pickup is behind the player.
						moveTowardsDirection("b", sight);
				}
			}
		}
	}
	
	/*
		Gets the visual field as a 2d array of Strings
		Looks like this:
		[] [] [] [] []
		[] [] [] [] []
		[] [] [] [] []
		[] [] [] [] []
		[] [] [] [] []
		[] [] [Player] [] []
		[] [] [] [] []
		
		----------------------------------------------
		
		OPTIONS FOR VISUAL FIELD ITEMS
		'@': Rock
		'+': Food
		'#': Door
		'*': Wall
		'=': Narrows
		'K': Key
		'T': Hammer
		'Q': Quicksand
		'O': Food Collection
		'$': Gold
		'R': Robot Monster
		'G': Robot-Monster-Killing Ray-Gun
		
	*/
	/**
	*Returns the raw visual field data as a 2d array of chars
	*
	**/
	protected char[][] processRetinalField(String info) 
	{
		StringTokenizer visTokens = new StringTokenizer(info, "(", true);
		visTokens.nextToken();
		char[][] sight = new char[HEIGHT][WIDTH];
		for (int i = 6; i >= 0; i--) 
		{              //iterate backwards so character printout displays correctly
			visTokens.nextToken();
			for (int j=0; j <=4; j++) 
			{             //iterate through the columns
				visTokens.nextToken();
				char[] visArray = visTokens.nextToken().replaceAll("[\\(\"\\)\\s]+","").toCharArray();
				for(char item : visArray)
					sight[i][j] = item;
			}
		}
		return sight;
	}
	/**
	* Prints out the retinal field as it would be shown from the agent's perspective.
	* Similar to the PrintVisualArray method in the sensorypacket
	*
	**/
	public void printRetinalField(char[][] sight)
	{
		for(int row = 0; row < 7; row++)
		{
			for(int column = 0; column < 5; column++)
			{
				System.out.print('[');
				System.out.print((sight[row][column]));
				System.out.print(']');
			}
			System.out.println();
		}
		System.out.println();
	}
	/**
	*main method runs the agent.
	**/
	public static void main(String [] args) //Runs the agent. Any number of them can be run.
	{
		ReactiveAgent ra = new ReactiveAgent("localHost", MAEDENPORT);
		ra.run();
	}
	
}