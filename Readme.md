# Sumatra - Central AI of TIGERs Mannheim

Homepage: https://www.tigers-mannheim.de  
Mail: management@tigers-mannheim.de

This is the central AI software of TIGERs Mannheim. It is called Sumatra.
We use Maven for building the application. You can also use IntelliJ or Eclipse to build and run it. The required configuration files are included.
Dependencies will be downloaded from Maven repositories, so you need an internet connection for the build.

## System Requirements
 * Java JDK 8
 * Maven
 * Internet connection
 * no limitations on OS known

## Build
Configuration for IntelliJ and Eclipse is provided and should work out-of-the-box. Without an IDE, run `./build.sh` or `./build.bat`, depending on your system platform.

### Run from command line
Run `run.sh` or `run.bat`.

### More details
For more detailed information about compiling and running Sumatra, have a look at our AutoReferee implementation which is based on Sumatra:
http://gitlab.tigers-mannheim.de/open-source/AutoReferee

## Simulation
There is an internal simulator that can be used without any external dependencies. Simply choose the "sim" config from the menu.

You can run AI against AI within one Sumatra instance. After pressing the run button, you can use the original refbox or the ref view in Sumatra to start a game. You can also start the included autoRef from the "AutoRef" view. Most configs can be found in the config view.

### Multiple Sumatra instances 
If you want to run different AIs against each other, you can start a second instance of Sumatra and connect it to the first one. On the second instance, you choose the "sim_remote" config. The configuration of this mode can be found under user -> sim. There, you can change the communication port and choose the team color that is controlled by the client. It is YELLOW by default.

Make sure you disable the AIs that are not used by switching them to OFF in the AI view.

The simulation can only be controlled from the server. The referee box is also running on the server. You can use the autoRef on the server as well. The client only provides the AI including the skill system.

