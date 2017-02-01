Author
----------------
Levi Sinclair
sinclairl@sou.edu

Description of classes.
----------------
SensoryPacket
Contains the sense information that is pivotal to the agent.


GridClient
Uses a socket to connect to maeden's Grid.
Information is sent throught this client.

ReactiveAgent
Main method of the program.
Contains the methods that control the agent's movement in response to sense data.
Utilizes both the SensoryPacket and GridClient classes.

Instructions on running agent.
----------------
This program is only dependent on Java's standard libraries. However, it is to be used with maeden.
Maeden's Grid program must be running in order for the agent to work.

First, Make sure you have Java up to date. I'm using version 1.8.0_111.

Second, if you haven't already, set up the Java Environment. You may not have to do this step.
On windows:
set JAVA_HOME to C:\ProgramFiles\java\jdk1.8.0_111
Append the String "C:\Program Files\Java\jdk1.8.0_111\bin" at the end of the system variable PATH.
Create the system variable Classpath and append "C:\Program Files\Java\jdk1.8.0_111\rt.jar;." 

On Linux:
export JAVA_HOME=/usr/local/java
export PATH=$PATH:$JAVA_HOME/bin/

On both Linux and Windows, you can compile and run the program like this:
% javac ReactiveAgent.java
% java ReactiveAgent

Description of "Other Ideas", how they work, and to what benefit.
----------------

The avoidance of getting stuck in corners idea:
Found in the run method as a single if/else
It goes through the permutaions of obstacles and turns left or right

moveTowardsPickups method
Searches for pickup items in the visual array.
If it finds any it will tell if it's ahead, behind, or on the same row as the agent.
The agent will move until it's on the same row as the pickup item.
The agent will then turn and walk forward to the item.

Acknoledgements
----------------
I'd like to thank my mother and the group members who dropped the class, making me do this by myself.