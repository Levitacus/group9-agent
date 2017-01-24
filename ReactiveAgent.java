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
    private static final int dashHeight=280;		// height of panel for dashboard (apx 3.5 * number of items to display)
    
    private GridClient gc;
		private SensoryPacket sp;
    /**
     * ReactiveAgent constructor takes a string and an int
     * and creates a socket and connects with a serverSocket
     * PRE: h is a string and p is an int (preferably above 1024)
     * POST: GridClient connects to Grid via network sockets
     */
  public ReactiveAgent(String h, int p) 
	{
		gc = new GridClient(h, p); //Makes the client.
	}
	public void run()
	{
		String direction;
		while(true)
		{
			sp = gc.getSensoryPacket(); //Gets a new sensory packet after every move.
			direction = sp.getSmell(); //Smell for direction.
			if(direction.equals("h")) //You're on the cheese.
			{
				gc.effectorSend("g"); //Pick up the cheese.
				gc.effectorSend("u"); //Ends the game.
			}
			else
				gc.effectorSend(direction); //Move towards the smell.
		}
	}
	public static void main(String [] args)
	{
		ReactiveAgent ra = new ReactiveAgent("localHost", MAEDENPORT);
		ra.run();
	}
}