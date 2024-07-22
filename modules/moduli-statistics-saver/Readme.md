# Run simulation with different AIs and export statistics

## Run simulation

Sumatra can be run with multiple instances (using different versions of the AI) in docker compose.

Simply run: `./scripts/runDockerSimulationLocal.sh`

By default, one game half (5min) will be simulated with both AIs having the same local development version. You can
switch to pre-build docker images for individual AIs in the script. Make sure to login into the TIGERs GitLab docker
registry: `docker login registry.gitlab.tigers-mannheim.de` before trying to pull pre-build images.

If you want to run multiple simulations in parallel, run `runDockerSimulationLocal.sh` with an additional identifier,
like:

`./scripts/runDockerSimulationLocal.sh sumatra1`

Note that Grafana and InfluxDB are started in the background by default (only once) and need to be stopped manually
afterwards:

`docker compose -p "sumatra_stats" -f docker-compose.statistics.yml down`

## See time series statistics

The docker compose setup also runs a local InfluxDB which stores time-series match statistics. They can be viewed with
Grafana.

If Grafana is not yet running, start it with:

`docker compose -p "sumatra_stats" -f docker-compose.statistics.yml up -d grafana`

Then open http://localhost:3000. Username is `admin`, password `fCQW904U3mtzui8MAAxXXE7i1DN2gphY`.

## Watch a running simulation

You can run the ssl-vision-client and ssl-status-board to get a live view of the simulation:

```shell
./scripts/watchLocalSimulation.sh
# If you passed an identifier above, pass it here as well:
./scripts/watchLocalSimulation.sh sumatra1
```

They are available at:

- ssl-vision-client: http://localhost:8082
- ssl-game-controller: http://localhost:8083
- ssl-status-board: http://localhost:8084

Ignore the addresses of the log output, that's only the docker-internal port.

## Run simulation from GitLab on a GitLab runner

The same setup can be run in a GitLab Pipeline. Open the latest pipeline in GitLab and you will find a job "
run-simulation". It will execute the `./scripts/runDockerSimulation.sh` script. You can overwrite parameters by adding
variables to the job before starting it.

You may want to build a new Sumatra image before. This is not done automatically. Go to the latest pipeline of the
master branch and run the job "build-image". A list of all images can be
found [here](https://gitlab.tigers-mannheim.de/main/Sumatra/container_registry/6). Currently, you need sufficient
permission to run the pipeline, so you may ask someone to trigger it for you.
