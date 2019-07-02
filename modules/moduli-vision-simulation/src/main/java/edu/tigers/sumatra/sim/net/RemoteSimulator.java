package edu.tigers.sumatra.sim.net;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.source.DirectRefereeMsgForwarder;
import edu.tigers.sumatra.referee.source.ERefereeMessageSource;
import edu.tigers.sumatra.sim.ASumatraSimulator;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;


/**
 * The remote simulator is running in the Sumatra instance that is not running the actual simulator,
 * but rather receives the sim state from the server and response with robot actions
 */
public class RemoteSimulator extends ASumatraSimulator
{
	private static final Logger log = Logger.getLogger(RemoteSimulator.class.getName());
	private final SimNetClient publisher = new SimNetClient();
	private AReferee referee;
	
	
	@Override
	public void startModule()
	{
		referee = SumatraModel.getInstance().getModule(AReferee.class);
		publisher.setActive(true);
		
		super.startModule();
	}
	
	
	@Override
	public void stopModule()
	{
		publisher.setActive(false);
		
		super.stopModule();
		
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
			log.warn("Referee source must be set to " + ERefereeMessageSource.INTERNAL_FORWARDER +
					" in order to forward referee messages from the simulation server");
		}
		
		publishFilteredVisionFrame(latestFrame);
	}
}
