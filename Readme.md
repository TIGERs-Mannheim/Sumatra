# Sumatra - Central AI of TIGERs Mannheim

Homepage: https://tigers-mannheim.de  
Mail: info@tigers-mannheim.de

This is the central AI software of TIGERs Mannheim. It is called Sumatra.
We use Gradle for building the application and IntelliJ as the primary IDE.
All dependencies will be downloaded automatically, so you need an internet connection for the build.

## System Requirements

* Java JDK (recent LTS version, exact required version can be
  found [here](buildSrc/src/main/groovy/sumatra.java.gradle))
* Internet connection
* Some optional features require native libraries (like screencasting or gamepad support), so some more exotic
  architectures may not fully be supported

## Build

Run `./gradlew build` to build the full application.

You can skip the tests with `./gradlew build -x test`.

## Run

Start Sumatra with `./gradlew :run` or use the provided IntelliJ run configuration.

Custom Sumatra arguments can be passed like this: `./gradlew :run --args="--help"`.

## Install

Sumatra can be installed into a single directory containing all dependencies: `./gradlew installDist`.
The directory is located in `build/install/sumatra`.
Additional JVM arguments can be passed in using the environment variable `SUMATRA_OPTS`.
Note: Install may not work with Windows due to an issue in the generated start script (set CLASSPATH line too long).

## IntelliJ

Sumatra makes use of Lombok. In order to detect generated code correctly you need to install the *Lombok Plugin*.

## Getting Started in the UI

The following steps should get you started:

- Start Sumatra and make sure that in the menu Moduli -> sim.xml is selected
- Go to the "Ref" view and press "Force Start" (or press F4)
- Go to the "AutoReferee" view and select "Active" to enable the automatic referee

## Simulation

### Internal Simulator

There is an internal simulator that can be used without any external dependencies.
Simply choose the "sim.xml" config from the moduli-menu.
It allows to run AI against AI within one Sumatra instance.

### External Simulator

The SSL-Simulation-Protocol is supported and was tested with [grSim](https://github.com/RoboCup-SSL/grSim)
and [ER-Force simulator](https://github.com/robotics-erlangen/framework#simulator-cli).

To connect to an external simulator, choose one of the following moduli configs:

- [simulation_protocol.xml](config/moduli/simulation_protocol.xml)
    - for testing/development
    - consumes ssl-vision messages from 224.5.23.2:10020
    - runs the ssl-game-controller internally and publishes messages on 224.5.23.1:11003
    - publishes vision tracking data on 224.5.23.2:11010
- [simulation_match.xml](config/moduli/simulation_match.xml)
    - for a virtual match
    - consumes ssl-vision messages from 224.5.23.2:10006
    - listens for referee messages on 224.5.23.1:10003

You can change some of the configuration parameters in the respective XML files.
Further parameters can be changed either via CLI (`--help`) or in the Cfg view under "user".

## Multiple Sumatra instances

If you want to run different AIs against each other, you can start a second instance of Sumatra and connect it to the
first one.
On the second instance, you choose the "sim_remote" config.
The configuration of this mode can be found under user -> sim. There, you can change the communication port and choose
the team color that is controlled by the client. It is YELLOW by default.

Make sure you disable the AIs that are not used by switching them to OFF in the AI view.

The simulation can only be controlled from the server. The game-controller is also running on the server. You can use
the autoRef on the server as well. The client only provides the AI including the skill system.

To simplify the setup, there is a docker compose setup. It is
described [here](./modules/moduli-statistics-saver/Readme.md).

## Further Documentation

The documentation can be found in [doc](./doc).

## Known issues

### UI scaling

If the font size is too small (e.g. on high resolution screens), you can change the font size.
An example is given in [build.gradle](build.gradle).
