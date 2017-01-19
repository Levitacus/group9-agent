import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.List;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Vector;

/**
 * The ReactiveAgent class provides an automated Reactive Agent to interact
 * with a Grid world simulation.
 * 
 *@author:  Nathan Ruf - Group #9
 *@date:    01-17-2017
 *@version: 1.0
 */
 
public class ReactiveAgent extends Frame 
{
    //private data field
    private String myID;
    private static final int MAEDENPORT = 7237;		// uses port 1237 on localhost
    private Insets iTrans;
    private static final int cellSize = 60;         // sets the width and height of individual visual cells
    private static final int cx = 5;                //sets size for the visual array
    private static final int ry = 7;				
    private static final int dashHeight=280;		// height of panel for dashboard (apx 3.5 * number of items to display)

    private ArrayList<GridObject> visField;         // stores GOB's for painting the visual field
    private GridDisplay gd;                         //for graphical display of map
    private Dashboard db;
    private boolean termOut = false;
    
    protected GridClient gc;
	
	//-----------------------------EDITS----------------------------------------------------------
	//sensory packet contents
	private String heading;					//1.  direction of food
	private String inventory;				//2.  inventory items
	private String ground;					//4.  ground contents
	private String messages;				//5.  messages
	private String energy;					//6.  remaining energy
	private String lastActionStatus;		//7.  status of last actionPerformed
	private String worldTime;				//8.  world time
	
	//TEMP
	private boolean food = false;			//TEMP (NEED TO USE INVENTORY STRING) indicates if food has been inventoried
	private String effector;				//indicates effector command											
	//-----------------------------EDITS----------------------------------------------------------

    /**
     * ReactiveAgent constructor takes a string and an int
     * and creates a socket and connects with a serverSocket
     * PRE: h is a string and p is an int (preferably above 1024)
     * POST: GridClient connects to Grid via network sockets
     */
    public ReactiveAgent(String h, int p) 
	{
		gc = new GridClient(h, p);
		visField = new ArrayList<GridObject>(); //the visual field contents will be held in array list
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		iTrans = getInsets();
		setTitle("Manual Agent: " + gc.myID);              //window title
		//setTitle("Agent View");                          //window title for generating figure for paper
		System.out.println("left:" + iTrans.left + " right:" + iTrans.right + " top:" + iTrans.top + " bottom:" + iTrans.bottom);
		setSize(cx * cellSize + iTrans.left + iTrans.right,
			ry * cellSize + dashHeight + iTrans.top + iTrans.bottom); //resize based on window cutoff
		gd = new GridDisplay(cx, ry, cellSize);  //initialize the graphical display
		db = new Dashboard(cx * cellSize, dashHeight, gc.gridOut);
		add(gd);
		add(db);

		setVisible(true);
    }//end constructor
	
	
	
	//-----------------------------EDITS----------------------------------------------------------
	//the agent function determines the effector command for the Reactive Agent
	//@return the command to be sent to the Grid
	public String agentFunction()
	{
		//consumes food if in inventory
		if(food)
		{
			return "u";
		}
		//agent moves based on sensory heading
		else
		{
			//moves forward
			if(this.heading.equals("f"))
			{
				//move forward
				return "f";
			}///end if
			//moves back
			else if(this.heading.equals("b"))
			{
				//send command to move back
				return "b";
			}//end else if
			//moves left
			else if(this.heading.equals("l"))
			{
				//send command to move left
				return "l";
			}//end else if
			//moves right
			else if(this.heading.equals("r"))
			{
				//send command to move right
				return "r";
			}//end else if
			//grabs food
			else if(this.heading.equals("h"))
			{
				//sets food to true
				this.food = true;
				
				//send command to grab food
				return "g";
			}//end else if
			 
			return "";
		}//end else
		
	}//end agentFunction
	//-----------------------------EDITS----------------------------------------------------------

 
    /**
     * sendEffectorCommand sends the specified command to the grid
     * *NOTE: GOBAgent only looks at first letter of command string unless talk or shout is sent*
     * pre: command is either f, b, l, r, g, u, d, "talk" + message, or "shout" + message
     * post: command is sent via the printwriter
     */
    public void sendEffectorCommand(String command) 
	{
		gc.gridOut.println(command);
    }//end sendEffectorCommand
 
 
    /**
     * getSensoryInfo via the GridClient component
     */
    public void getSensoryInfo() 
	{
		SensoryPacket sp = gc.getSensoryPacket();
		//sp.printVisualArray();
		String[] rawSenses = sp.getRawSenseData();
		
		//-----------------------------EDITS----------------------------------------------------------
		//sets sensory packet
		//this.rawSenses = sp.getRawSenseData();
		
		// 1: set the smell info
		this.heading = rawSenses[0];
		// 2: set the inventory
		this.inventory = rawSenses[1];
		// 3: set the currently visible objects info the visField list for display
		processRetinalField(sp.getVisualArray());
		// 4: set ground contents
		this.ground = rawSenses[3];
		// 5: set messages
		this.messages = rawSenses[4]; //CHECKS MESSAGES ****CHANGE****
		// 6: set energy
		this.energy = rawSenses[5];
		// 7: set lastActionStatus
		this.lastActionStatus = rawSenses[6];
		// 8: set world time
		this.worldTime = rawSenses[7];
		
		// store or update according to the data just read. . . .
		gd.updateGDObjects(visField);
		db.updateLabels(this.heading, this.inventory, this.ground, this.messages, this.energy, this.lastActionStatus, this.worldTime);
		//-----------------------------EDITS----------------------------------------------------------
    }//end getSensoryInfo

	
    /* processRetinalField: populate the display grid from the pre-processed visual field of a SensoryPacket
     * @param visualArray preprocessed array of lists of character representing maeden objects
     */
    void processRetinalField(ArrayList<ArrayList<Vector<Character>>> visualArray)
	{
		visField.clear();
		for (int r = 6; r >= 0; r--)
			for (int c = 0; c < 5; c++)
				if (visualArray.get(r).get(c) != null)
					for (char item : visualArray.get(r).get(c))
					{
						switch(item) 
						{    //add the GridObjects for the graphical display
							case ' ': break;
							case '@': visField.add(new GOBRock(c, r, cellSize)); break;         //Rock
							case '+': visField.add(new GOBFood(c, r, cellSize)); break;         //Food
							case '#': visField.add(new GOBDoor(c, r, cellSize)); break;         //Door
							case '*': visField.add(new GOBWall(c, r, cellSize)); break;         //Wall
							case '=': visField.add(new GOBNarrows(c, r, cellSize)); break;      //Narrows
							case 'K': visField.add(new GOBKey(c, r, cellSize)); break;          //Key
							case 'T': visField.add(new GOBHammer(c, r, cellSize)); break;       //Hammer
							case 'Q': visField.add(new GOBQuicksand(c, r, cellSize)); break;    //Quicksand
							case 'O': visField.add(new GOBFoodCollect(c, r, cellSize)); break;  //Food Collection
							case '$': visField.add(new GOBGold(c, r, cellSize, gd)); break;     //Gold
							case 'R': visField.add(new GOBRobot(c, r, cellSize, gd)); break;    // Robot Monster
							case 'G': visField.add(new GOBRayGun(c, r, cellSize, gd)); break;   // Robot-Monster-Killing Ray-Gun
							default:
								if(item >= '0' && item <= '9' && r == 5 && c == 2)
								visField.add(new GOBAgent(c, r, cellSize, 'N'));
								else if((item >= '0' && item <= '9') || item == 'H')
								visField.add(new GOBAgent(c, r, cellSize, '?'));
						}//end switch
					}//end for
    }//end processRetinalField

 
    /**
     * run iterates through program commands
     * pre: sockets are connected to each other
     * post: program is run and exited when the agent reaches the food
     */
    public void run() 
	{
		//-----------------------------EDITS----------------------------------------------------------
		getSensoryInfo();
		effector = agentFunction();
		
		//testing
		System.out.println("The current effector is " + effector);
		//testing
		
		sendEffectorCommand(effector);
		while(true) 
		{
			gd.repaint();
			getSensoryInfo();
			effector = agentFunction();
			sendEffectorCommand(effector);
		}//end while
		
		//-----------------------------EDITS----------------------------------------------------------
    }//end run


    /**
     * main: creates new gridclient w/ the name of the server machine and the port number
     * pre: none
     * post: the program is run
     */
    public static void main(String [] args) 
	{
		ReactiveAgent client = new ReactiveAgent("localhost", MAEDENPORT);
		client.run();
    }//end main
    
    //-------------------------------------------------------------------------
 
    public class Dashboard extends Panel 
	{
	
		private Label foodHeading;
		private Label foodIs;
		private Label invIs;
		private Label invObject;
		private Label groundIs;
		private Label groundList;
		private Label energyIs;
		private Label energyNum; 
		private Label msgIs;
		private Label msgInfo;
		private Label textIs;
		private TextField text;
		private Label lastActStatIs;
		private Label lastActStat;
		private Label worldTimeIs;
		private Label worldTime;

		//private Panel prompts;
		//private Panel vals;

		public Dashboard(int width, int height, PrintWriter pw)
		{
			/*
			//setFont(new Font("GENEVA", Font.BOLD, 14));
			setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			prompts = new Panel(new GridLayout(0, 1));
			vals =  new Panel(new GridLayout(0, 1));
			//setLayout(new GridLayout(0, 2));//note: rows expand as components added
			setSize(width, height);
			foodIs = new Label("The food is:");
			foodHeading = new Label(" ", Label.LEFT);
			invIs = new Label("Inventory:");
			invObject= new Label(" ", Label.LEFT);
			groundIs= new Label("Ground:");
			groundList= new Label(" ", Label.LEFT);
			energyIs= new Label("Energy:");
			energyNum= new Label(" ", Label.LEFT);
			msgIs= new Label ("Message:");
			msgInfo= new Label (" ", Label.LEFT);
			textIs= new Label("Command:");
			text = new TextField(30);
			lastActStatIs = new Label("Prev result:");
			lastActStat = new Label(" ", Label.LEFT);
			text.addActionListener(new GridDisplayListener(pw, text));
			*/

			//setFont(new Font("GENEVA", Font.BOLD, 14));
			setLayout(new GridLayout(0, 2, 0, 0));//note: rows expand as components added
			setSize(width, height);
			foodIs = new Label("The food is:  ", Label.RIGHT);
			foodHeading = new Label(" ", Label.LEFT);
			invIs = new Label("Inventory:  ", Label.RIGHT);
			invObject= new Label(" ", Label.LEFT);
			groundIs= new Label("Ground:  ", Label.RIGHT);
			groundList= new Label(" ", Label.LEFT);
			energyIs= new Label("Energy:  ", Label.RIGHT);
			energyNum= new Label(" ", Label.LEFT);
			msgIs= new Label ("Message:  ", Label.RIGHT);
			msgInfo= new Label (" ", Label.LEFT);
			textIs= new Label("Command:  ", Label.RIGHT);
			text = new TextField(17);
			lastActStatIs = new Label("Prev result:  ", Label.RIGHT);
			lastActStat = new Label(" ", Label.LEFT);
			worldTimeIs = new Label("World time:  ", Label.RIGHT);
			worldTime = new Label(" ", Label.LEFT);
			text.addActionListener(new GridDisplayListener(pw, text));

			// msgInfo.setFont(new Font("GENEVA", Font.BOLD, 10));
			foodIs.setSize(width/2, 50);
			foodHeading.setSize(width/2, 50);
			invIs.setSize(150, 50);
			invObject.setSize(150, 60);
			groundIs.setSize(150, 60);
			groundList.setSize(150, 60);
			energyIs.setSize(150, 60);
			msgIs.setSize(150, 60);
			msgInfo.setSize(150, 60);
			textIs.setSize(150, 60);
			text.setSize(150, 60);
			lastActStat.setSize(150, 60);
			worldTime.setSize(150, 60);
			add(foodIs);
			add(foodHeading);
			add(invIs);
			add(invObject);
			add(groundIs);
			add(groundList);
			add(energyIs);
			add(energyNum);
			add(msgIs);
			add(msgInfo);
			add(textIs);
			add(text);
			add(lastActStatIs);
			add(lastActStat);
			add(worldTimeIs);
			add(worldTime);

			/*
			  prompts.add(foodIs);
			  vals.add(foodHeading);
			  prompts.add(invIs);
			  vals.add(invObject);
			  prompts.add(groundIs);
			  vals.add(groundList);
			  prompts.add(energyIs);
			  vals.add(energyNum);
			  prompts.add(msgIs);
			  vals.add(msgInfo);
			  prompts.add(textIs);
			  vals.add(text);
			  prompts.add(lastActStatIs);
			  vals.add(lastActStat);

			  add(prompts);
			  add(vals);
			*/

			//	    validate();
			
			//	    System.out.println(foodIs.getWidth() + " " + foodIs.getHeight());
		}//end constructor

		//udpates labels in dashboard based on current sensory values
		public void updateLabels(String h, String inv, String g, String m, String e, String res, String wt)
		{
			foodHeading.setText(h);
			invObject.setText(inv);
			groundList.setText(g);
			msgInfo.setText(m);
			energyNum.setText(e);
			lastActStat.setText(res);
			worldTime.setText(wt);
			repaint();
		}//end updateLabels


		class GridDisplayListener implements ActionListener
		{
			PrintWriter gridOut;
			TextField text;
			
			public GridDisplayListener(PrintWriter pw, TextField tf)
			{
				gridOut=pw;
				text=tf;
				//System.out.println("initialized");
			}//end GridDisplayListener

			public void actionPerformed(ActionEvent e)
			{
				CommandCheck check = new CommandCheck();
				String command = text.getText();
				command = check.validateCommand(command);
				gridOut.println(command);
				text.setText("");
				//System.out.println("SENT: " + text.getText().toLowerCase());
			}//end actionPerformed
		}//end class GridDisplayListener
    }//end class Dashboard

    public class CommandCheck 
	{
	    
		private String commandString;   //stores the string that is sent in by the user
				   
		private void printHelp() 
		{
			System.out.println("Maeden help information");
			System.out.println("Allowable commands are:");
			System.out.println("f: move forward");
			System.out.println("b: move backward");
			System.out.println("r: turn right");
			System.out.println("l: turn left");
			System.out.println("g: grab an object in the current spot");
			System.out.println("d: drop an object currently being carried");
			System.out.println("u: apply a carried object (tool or food)");
			System.out.println("a: attack an agent ahead");
			System.out.println("w: wait");
			System.out.println("k: remove yourself from world");
			System.out.println("?: print this help information");
		}//end printHelp
	
		/**
		 * invalidCommand: string -> boolean
		 * In general, a command is invalid if its length is 0 (the user just pressed enter),
		 * or the command starts with an invalid letter (one that's not fbrldguwst).
		 * If the first letter of the command is g, if additional requirments are not met the command
		 * is invalid. A valid "g command" is of the form "g [item]" where [item] begins with +, k or t.   
		 */
		public boolean invalidCommand(String commandString)
		{
			return (commandString.length() == 0
				|| "fbrldguwstka?".indexOf(commandString.substring(0,1)) < 0);
		}//end invalidCommand

		public String validateCommand(String commandString) 
		{
			try 
			{
				commandString=commandString.toLowerCase();
				while ( invalidCommand(commandString) )
				{
					printHelp();
					return null;
				}//end while
			}//end try
			catch(Exception e) {System.out.println("validateCommand: " + e);}
			return commandString;
		}//end validateCommand
    }//end class CommandCheck
}//end class ReactiveAgent
