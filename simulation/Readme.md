# Run simulation with different AIs and export statistics

Sumatra can be run with multiple instances (using different versions of the AI) in docker compose.

## Preparation

You need Docker and Docker Compose installed on your machine.
Then, pull the required docker images, using one of the following methods:

1. Pull from TIGERs GitLab registry (requires login):

```shell
# Create an access token in GitLab and use it to login (only for TIGERs members)
docker login registry.gitlab.tigers-mannheim.de
# Pull the images
docker compose --profile watch pull
```

2. Build sumatra image locally:

```shell
# Build image with Jib
./gradlew jibDockerBuild
# Pull remaining images (ignore errors about not being able to pull sumatra)
docker compose --profile watch pull
```

## Activate statistics collection

You can start a local InfluxDB and Grafana to collect statistics from the simulation.
This is optional and can be skipped if you don't need statistics.

```shell
# Start InfluxDB and Grafana in the background
docker compose -f simulation/compose.yml up -d
```

Then open http://localhost:3000.
Username is `admin`, password `fCQW904U3mtzui8MAAxXXE7i1DN2gphY`.

You can stop the services later with:

```shell
docker compose -f simulation/compose.yml down
```

## Run simulation

```shell
./simulation/runDockerSimulation.sh
```

If you want to run multiple simulations in parallel, pass an identifier to the script:

```shell
./simulation/runDockerSimulation.sh sumatra1
```

### Run simulation with different AIs

You can change the docker image for each AI by setting the following environment variables:

- `SUMATRA_IMAGE_TAG_SIMULATOR`: Docker image for the simulator
- `SUMATRA_IMAGE_TAG_YELLOW`: Docker image for the yellow AI
- `SUMATRA_IMAGE_TAG_BLUE`: Docker image for the blue AI

You can either use a tag available in GitLab (as a TIGERS member) or build the image locally from the desired commit.

### Additional Configuration

More configuration options can be found in [compose.yml](../compose.yml).
Either change this file or set the environment variables directly.

## Watch a running simulation

You can run the ssl-vision-client and ssl-status-board to get a live view of the simulation:

```shell
./simulation/watchLocalSimulation.sh
# If you passed an identifier above, pass it here as well:
./simulation/watchLocalSimulation.sh sumatra1
```

They are available at:

- ssl-vision-client: http://localhost:8082
- ssl-game-controller: http://localhost:8083
- ssl-status-board: http://localhost:8084

Ignore the addresses of the log output, that's only the docker-internal port.

## Run simulation from GitLab on a GitLab runner

The same setup can be run in a GitLab Pipeline.
Open the latest pipeline in GitLab and you will find a job "run-simulation".
It will execute the `./simulation/runDockerSimulation.sh` script.
You can overwrite parameters by adding variables to the job before starting it.

You may want to build a new Sumatra image before.
This is not done automatically. Go to the latest pipeline of the master branch and run the job "build-image".
A list of all images can be found [here](https://gitlab.tigers-mannheim.de/main/Sumatra/container_registry/6).
Currently, you need sufficient permission to run the pipeline, so you may ask someone to trigger it for you.
