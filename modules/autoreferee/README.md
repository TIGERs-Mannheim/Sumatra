# TIGERs Mannheim AutoReferee implementation

# Building the Project
You can either build the application using the Maven buildtool or launch it through Eclipse. Please note that you will need an internet connection to build the project with Maven as it will download additional dependencies from the internet. No matter which way you choose you will always need Java 8. Both the Oracle JDK and the OpenJDK will suffice. The software has been developed and tested under Linux, but should also run on Windows/Mac machines.


The following guides provide detailed instructions on how to install and run the AutoReferee on Ubuntu 14.04:

## Maven

1. Installing OpenJDK 8
	
	The OpenJDK 8 has not been offically backported to Ubuntu 14.04 LTS and cannot be installed from the default repositories. The following link provides a detailed guide on how to install OpenJDK 8 from a PPA repository: [How to Install OpenJDK 8 in Ubuntu 14.04 & 12.04 LTS](http://ubuntuhandbook.org/index.php/2015/01/install-openjdk-8-ubuntu-14-04-12-04-lts/)

	Please make sure that the **java** as well as the **javac** commands point to the correct Java version by manually setting the default if you have not already done so in the installation guide above:
	
	```
	sudo update-alternatives --config java
	sudo update-alternatives --config javac
	```

2. Installing Maven

	Installing Maven from the official repository is recommended as the **mvn** command will be available without further actions.
	
	```
	sudo apt-get install maven
	```

3. You can then proceed to cloning the repository:

	```
	git clone http://gitlab.tigers-mannheim.de/open-source/AutoReferee.git
	```
4. Change directory into the repository and issue the following command to build the project:

	```
	mvn install -DskipTests -Dmaven.javadoc.skip=true
	```
	
	This step can take some time if this is the first time that Maven is used on the machine as Maven will populate its local repository with all the dependencies it requires to perform the build steps.
5. Run the `run.sh` script to start the application

### Possible issues:
- If the build fails with the following error please verify that you have correctly set up Java 8.

	```
	Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.3:compile (default-compile) on project common: Fatal error compiling: invalid target release: 1.8 -> [Help 1]
	```

## Eclipse

1. Install Java 8 as explained in the first step of the Maven guide above
2. Clone the AutoReferee repository into a directory of your choosing:

	```
	git clone http://gitlab.tigers-mannheim.de/open-source/AutoReferee.git
	```

3. Download Eclipse with EGit/Maven support (The Java EE edition already contains all necessary plugins): [Eclipse Downloads](http://www.eclipse.org/downloads/)
4. Extract and launch Eclipse
5. Eclipse will only detect the Java version that you used to run it with. If you did not set up your system to use Java 8 as default then you will have to add the Java 8 JRE manually. See this link for more details: [Adding a new JRE definition](http://help.eclipse.org/mars/topic/org.eclipse.jdt.doc.user/tasks/task-add_new_jre.htm)
6. Import the projects:
	- Open the Import dialogue under `File->Import...`
	- Choose the entry `General->Existing Projects into Workspace`
	- Select the repository directory as `root directory` and make sure that `Search for nested projects` is ticked.
	- Select all listed projects and hit Finish
7. Eclipse will now import all projects and instruct Maven to download all necessary dependencies. This can take some time.
8. You can now launch the application by opening the drop down menu next to the green play button and selecting the `AutoReferee` entry.

	If Eclipse did not automatically pick up the launch configuration and the drop down menu is empty you can also launch the project by expanding the **autoreferee-main** project, right-clicking on the `AutoReferee.launch` file and selecting `Run as -> AutoReferee`

### Possible issues:
- Eclipse will automatically compile the **.proto** files in the **moduli-cam** and **moduli-referee** projects. If you encounter errors in these projects try to refresh the configuration by selecting all projects in the Package Explorer, right clicking and choosing `Maven->Update Project..` Uncheck the checkbox labeled `Update Project configuration from pom.xml` in the dialogue that pops up (as that would override the Eclipse project configuration) and then select `OK`.

	The Java files which the protobuf compiler generates do not comply with the strict compiler settings for the projects. This is why the classpath folders that contain the generated protobuf files are marked to ignore optional compiler errors. These settings are overriden by Eclipse when updating the projects from the Maven configuration. If Eclipse complains about compile errors in these classpath folders check if the **.classpath** file has been modified and **checkout** possible changes. Refresh the projects afterwards.


# Usage

## GUI

### AutoReferee view
The **Violations** group can be used to activate/deactivate individual rule violations even after the autoreferee has been started. The **Engine** group provides multiple controls to alter the behavior of the active autoreferee. The labels in the top part of the group display the next action that the autoref will execute after the game has come to a stop. The **Reset** button can be used to reset the internal state and delete the stored action. The **Proceed** button will instruct the autoref to initiate the next action even if it is still waiting for a condition to become true like a ball to be placed or a bot to come to a stop. 

### Game Log View
The **Game Log** view displays all events that occurred and have an impact on the behavior of the autoreferee. The checkboxes below the table can be used to toggle different event types on or off.

### Ball Speed View
The ball speed view displays the velocity of the ball on the field. The slider component on the right can be used to alter the time window of the chart. If the **Pause when not RUNNING** checkbox is ticked the chart will automatically pause if the game is not in the RUNNING gamestate. This pause can be overridden by using the Pause/Resume buttons. Please note that the chart will not automatically resume after it has been manually paused by the user if the gamestate transitions back to RUNNING.

## Configuration
All configuration options are available via the **Cfg** tab. The **Cfg** tab itself contains multiple tabs to modify parameters of different parts of the application. Before you can modify any parameter you need to hit the **Load** button. You can then make the necessary modifications and **Apply** them and/or **Save** them to disk.

### Vision port
The application will try to receive vision frames from **224.5.23.2:10006** by default. If you want to change this behavior navigate to the **user** section and select `edu.tigers.sumatra->cam->SSLVisionCam`. You can change the address or the port(ROBOCUP).

### Refbox & autoref parameter
The behavior of the AutoReferee can be altered through the **autoreferee** config section. All import configuration parameters are grouped under the `edu.tigers.autoreferee->AutoRefConfig` section. Before you hit the **Start** button make sure that the refbox hostname and port are configured correctly. They point to **localhost:10007** by default.

## AutoReferee
The autoref software has been designed to work as an extension of the refbox. It will automatically try to keep the game alive once you initiate an action(kickoff/direct/indirect). Rule infringements are currently logged directly to the game log in the bottom right corner of the main window.


# Mode of operation
The autoref software has been designed to work as an extension of the refbox. It can be used in two modes:
- **Active**: A connection to the refbox is required. The autoref will actively send commands and run the game.
- **Passive**: No connection to the refbox is required. The autoref will not send any commands and log all its decisions to the main window.

No matter which mode is chosen the AutoReferee always employs multiple detector classes which are responsible for detecting rule violations that occur on the field. These classes do not actively manipulate the state of the game and are meant to be passive. They can be found in the [edu.tigers.autoreferee.engine.violations.impl](modules/moduli-autoreferee/src/main/java/edu/tigers/autoreferee/engine/violations/impl/) package of the [moduli-autoreferee](modules/moduli-autoreferee/) project. The passive autoref merely displays the detected violations in the game log whereas the active autoref will act upon the violations and alter the gamestate accordingly. This behavior of the active autoref is implemented through multiple state classes which are responsible for driving gamestate transitions as well as handling rule violations by sending commands to the refbox. In every gamestate there can be only one active autoref state.

## Initiating Plays
Whenever the game is stopped after a rule violation or a goal the autoref will send a ball placement command (see the Ball Placement section for more details) to have the ball placed at the desired position. If the placement fails or no teams are capable of placing the ball it will visualize the desired position on the field visualizer and wait for the ball to be placed. After the ball has been placed the autoref will wait until all robots have come to a stop and then send the next command. If for some reason the bots do not come to a stop it is possible to manually start the transition by clicking the **Proceed** button in the **AutoReferee** view. Another alternative is to simply issue the command manually using the refbox.

## Detecting Goals
At the moment it is not possible to deduct the vertical position of the ball from the vision data. This can produce false positives if the ball is chip kicked over the goal. To avoid these situations the autoref tries to detect goals by monitoring the direction in which the ball is headed while being located inside the goal perimeter. It checks the following sufficient conditions for a goal:
- The ball comes to a stop while being inside the goal
- The heading of the ball changes by more than 45 degrees while the ball is inside the goal

## Ball Placement
The autoref will signal ball placements after it has stopped the game to place the ball at the desired position. It is possible to specify which team is capable of performing a ball placement in the **Cfg** tab. Please note that at the moment the ball placement is deactivated by default so you will need to activate it before or during the game. If both teams are capable of placing the ball you can manually set a preference for which team will be given the chance to place the ball first. If you do not set a preference the autoreferee will first send the placement command to the team which will afterwards perform the actual kick.

The autoref will give each team 15s time to place the ball at the desired position. After the ball has been placed or all teams that are capable of placing the ball have failed it will return to the stopped state and wait for the ball to be placed manually or initiate the next action. This will give the teams more time to reposition themselves after the placement states.

## Rule infringements
The referee is currently capable of detecting the following rule infringements:

- **Defender to Ball distance during a freekick**: The autoref will interrupt the freekick and return to the stopped state to reschedule.
- **Indirect Goals**: If the ball is kicked directly into the goal after an indirect freekick the autoref will schedule a throw-in or goal kick. 
- **10s Timeout during a freekick/kick-off situation**: If the team performing the kick takes longer than 10 seconds to move the ball at least 50mm from the kick position the autoref will stop the game and schedule a Force Start.
- **Attacker to Defense Area distance during a freekick**
- **Attacker touching the Keeper**
- **Attacker/Defender touching the ball inside the defense area of the opponent**
- **Throw-ins/Goal kicks/Corner kicks/ Icing**
- **Ball velocity**: Monitors the ball velocity and schedules a freekick for the opposing team if the ball was kicked too fast. Please note that you can set the maximum allowed ball velocity in the **AutoReferee** section of the **Cfg** tab.
- **Number of bots on the field**: The autoref consults referee messages and vision data to determine the currently allowed number of bots on the field for each team. It monitors the number of bots whenever the ball is in play. At the moment the autoref will only log violations of this rule but not take any further actions. 
- **Bot speed during STOP**: The rule monitors the maximum allowed speed for bots during the stopped state. At the moment it does not book any yellow cards but logs the violation. It is possible to change the velocity threshold in the **AutoReferee** section of the **Cfg** tab.
- **Double Touch**: If the bot performing a freekick/kick-off touches the ball a second time after he has performed the kick the autoref will stop the game and schedule a freekick for the opponents.
- **Dribbling**: The autoref will schedule a freekick for the opposing team if a robot stays closer than 50mm to the ball for a total distance of 1m from where it first touched the ball
- **Bot collisions**: The autoref tries to detect bots which make close contact and evaluates the velocity with which they make contact. If the velocity exceeds a certain threshold a violation is reported and a freekick awarded to the team of the bot with the lower absolute velocity.

If the freekick is not to be taken from a well defined position (like a Thrown-in/Goal kick/Corner kick) the autoref will try to place the ball at the position that is the most suited. It will perform the following checks:
- If the ball is located closer than 700m from the defense area of the attacking team -> Place it 600mm from the goal line and 100mm from the touch line
- If the ball is located closer than 700m from the defense area of the defending team -> Place it at a point that is closest to the original position and located 700mm away from the defense area
- Move the ball away from the goal line/ touch line if the kick position is located closer than 100m to these lines
