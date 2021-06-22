/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim.net;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.source.DirectRefereeMsgForwarder;
import edu.tigers.sumatra.referee.source.ERefereeMessageSource;
import edu.tigers.sumatra.sim.ASumatraSimulator;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * The remote simulator is running in the Sumatra instance that is not running the actual simulator,
 * but rather receives the sim state from the server and responds with robot actions
 */
public class RemoteSimulator extends ASumatraSimulator
{
	private static final Logger log = LogManager.getLogger(RemoteSimulator.class.getName());
	private final SimNetClient publisher = new SimNetClient();
	private AReferee referee;
	private ExecutorService service = null;


	@Override
	public void startModule()
	{
		referee = SumatraModel.getInstance().getModule(AReferee.class);
		publisher.setActive(true);

		service = Executors.newSingleThreadExecutor(new NamedThreadFactory("SumatraSimulator"));
		service.execute(this);
	}


	@Override
	public void stopModule()
	{
		publisher.setActive(false);

		service.shutdownNow();
		try
		{
			boolean terminated = service.awaitTermination(1, TimeUnit.SECONDS);
			if (!terminated)
			{
				log.error("Could not terminate simulator");
			}
		} catch (InterruptedException e)
		{
			log.error("Interrupted while awaiting termination", e);
			Thread.currentThread().interrupt();
		}

		publisher.stop();

		referee = null;
	}


	@Override
	public void run()
	{
		while (publisher.isActive())
		{
			publisher.start();
			handleConnection();
			log.info("Disconnected from simulation server");
		}
	}


	private void handleConnection()
	{
		while (publisher.isConnected())
		{
			SimNetClient.SimServerResponse response = publisher.accept(simulatorActionCallbacks);
			if (response != null)
			{
				processResponse(response);
			}
		}
	}


	private void processResponse(final SimNetClient.SimServerResponse response)
	{
		FilteredVisionFrame latestFrame = response.getFrame();

		if (referee.getActiveSource().getType() == ERefereeMessageSource.INTERNAL_FORWARDER)
		{
			((DirectRefereeMsgForwarder) referee.getActiveSource()).send(response.getRefereeMessage());
		} else
		{
			log.warn("Referee source must be set to {} in order to forward referee messages from the simulation server",
					ERefereeMessageSource.INTERNAL_FORWARDER);
		}

		publishFilteredVisionFrame(latestFrame);
	}
}
