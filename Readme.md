# Sumatra - Central AI of TIGERs Mannheim

Homepage: https://tigers-mannheim.de  
Mail: info@tigers-mannheim.de

This is the central AI software of TIGERs Mannheim. It is called Sumatra.
We use Gradle for building the application and IntelliJ as the primary IDE.
All dependencies will be downloaded automatically, so you need an internet connection for the build.

## System Requirements
 * Java JDK 11
 * Internet connection
 * no limitations on OS known

## Build
Run `./gradlew build` to build the full application. 

You can skip the tests with `./gradlew build -x test`. 

## Run
Start Sumatra with `./gradlew run` or use the provided IntelliJ run configuration.

Custom Sumatra arguments can be passed with `--args="--my-arg"`.
For development, use `./gradlew run -Pdevelopment` to enable some more runtime checks (assertions).
For a real match, use `./gradlew run -Pproductive` to disable some checks, start recordings automatically and increase the heap size.

## Install
Sumatra can be installed into a single directory containing all dependencies: `./gradlew installDist`.
The directory is located in `build/install/sumatra`.
The `run.sh` and `build.sh` scripts make use of this task.
Additional JVM arguments can be passed in using the environment variable `SUMATRA_OPTS`.
Note: Install does not work with Windows due to an issue in the generated start script (set CLASSPATH line too long).

## IntelliJ
IntelliJ reads the Gradle configuration and uses Gradle to perform the build and to start the application.
Make sure to configure Gradle for build and tests under Build, Execution, Deployment -> Build Tools -> Gradle.

Sumatra makes use of Lombok. In order to detect generated code correctly you need to install the *Lombok Plugin*.

When you start Sumatra, "Run" one of the Gradle run configurations. 
If you want to debug code, "Debug" Sumatra Debug afterwards.
If you "Debug" one of the other run configurations, the startup time will increase significantly, because
a new Gradle daemon is started and configuration takes longer.

## Documentation
The documentation can be found in [doc](./doc).

## Simulation
There is an internal simulator that can be used without any external dependencies. 
Simply choose the "sim" config from the menu.

You can run AI against AI within one Sumatra instance. 
After pressing the run button, you can use the "Ref" view in Sumatra to control the game. 
You can also start the included autoRef from the "AutoRef" view. 
Most configs can be found in the "config" view.

## Multiple Sumatra instances 
If you want to run different AIs against each other, you can start a second instance of Sumatra and connect it to the first one. 
On the second instance, you choose the "sim_remote" config. 
The configuration of this mode can be found under user -> sim. 
There, you can change the communication port and choose the team color that is controlled by the client. 
It is YELLOW by default.

Make sure you disable the AIs that are not used by switching them to OFF in the AI view.

The simulation can only be controlled from the server. 
The game-controller is also running on the server. 
You can use the autoRef on the server as well. 
The client only provides the AI including the skill system.

## Known issues
### UI scaling
If the font size is too small (e.g. on high resolution screens), you can add following arguments to the command below.
16 is the font size and can be adapted. You can also choose other font types.

```shell script
# Linux
export SUMATRA_OPTS="${SUMATRA_OPTS} -Dswing.plaf.metal.controlFont='sans-serif-16' -Dswing.plaf.metal.userFont='sans-serif-16'"
./run.sh
# Windows
set SUMATRA_OPTS="%SUMATRA_OPTS% -Dswing.plaf.metal.controlFont='sans-serif-16' -Dswing.plaf.metal.userFont='sans-serif-16'"
run.bat
```
