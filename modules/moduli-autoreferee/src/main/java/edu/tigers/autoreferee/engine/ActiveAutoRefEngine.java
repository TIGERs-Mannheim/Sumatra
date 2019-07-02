/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine;

import java.net.InetAddress;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.detector.EGameEventDetectorType;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.autoreferee.remote.AutoRefToGameControllerConnector;
import edu.tigers.autoreferee.remote.GameEventResponse;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.control.GcEventFactory;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;


public class ActiveAutoRefEngine extends AutoRefEngine
{
	private final Logger log = Logger.getLogger(ActiveAutoRefEngine.class.getName());
	private static final String DEFAULT_REFEREE_HOST = "localhost";
	private static final int DEFAULT_GC_AUTO_REF_PORT = 11007;
	
	private AutoRefToGameControllerConnector remote;
	private Long lastTimeSentContinue;
	
	
	public ActiveAutoRefEngine(final Set<EGameEventDetectorType> activeDetectors)
	{
		super(activeDetectors);
	}
	
	
	@Override
	public void start()
	{
		lastTimeSentContinue = null;
		AReferee referee = SumatraModel.getInstance().getModule(AReferee.class);
		String hostname = referee.getActiveSource().getRefBoxAddress()
				.map(InetAddress::getHostAddress)
				.orElse(DEFAULT_REFEREE_HOST);
		int port = SumatraModel.getInstance().getModule(AutoRefModule.class)
				.getSubnodeConfiguration().getInt("gameControllerPort", DEFAULT_GC_AUTO_REF_PORT);
		remote = new AutoRefToGameControllerConnector(hostname, port);
		remote.addGameEventResponseObserver(this::onGameControllerResponse);
		remote.start();
	}
	
	
	@Override
	public void stop()
	{
		remote.stop();
	}
	
	
	@Override
	public void process(final IAutoRefFrame frame)
	{
		processEngine(frame).forEach(this::processGameEvent);
		
		if (SumatraModel.getInstance().isSimulation())
		{
			handleContinue(frame);
		}
	}
	
	
	private void handleContinue(final IAutoRefFrame frame)
	{
		if (frame.getGameState().getState() == EGameState.HALT
				&& canContinueGame(frame))
		{
			if (lastTimeSentContinue == null)
			{
				lastTimeSentContinue = frame.getTimestamp();
			}
			double timeSinceLastSentContinue = (frame.getTimestamp() - lastTimeSentContinue) / 1e9;
			if (timeSinceLastSentContinue > 1)
			{
				log.info("Resuming from HALT");
				SumatraModel.getInstance().getModule(AReferee.class)
						.sendGameControllerEvent(GcEventFactory.triggerContinue());
				lastTimeSentContinue = frame.getTimestamp();
			}
		} else
		{
			lastTimeSentContinue = null;
		}
	}
	
	
	private boolean canContinueGame(final IAutoRefFrame frame)
	{
		return ballPlaced(frame)
				&& notTooManyBots(frame, ETeamColor.BLUE)
				&& notTooManyBots(frame, ETeamColor.YELLOW);
	}
	
	
	private boolean ballPlaced(final IAutoRefFrame frame)
	{
		return frame.getGameState().getBallPlacementPositionNeutral() == null
				|| frame.getWorldFrame().getBall().getPos()
						.distanceTo(frame.getGameState().getBallPlacementPositionNeutral()) < RuleConstraints
								.getBallPlacementTolerance();
	}
	
	
	private boolean notTooManyBots(final IAutoRefFrame frame, ETeamColor teamColor)
	{
		return frame.getWorldFrame().getBots().keySet().stream()
				.filter(id -> id.getTeamColor() == teamColor)
				.count() <= frame.getRefereeMsg().getTeamInfo(teamColor).getMaxAllowedBots();
	}
	
	
	@Override
	protected void processGameEvent(final IGameEvent gameEvent)
	{
		super.processGameEvent(gameEvent);
		remote.sendEvent(gameEvent);
	}
	
	
	private void onGameControllerResponse(GameEventResponse response)
	{
		if (response.getResponse() != GameEventResponse.Response.OK)
		{
			log.warn("Game-controller response was not OK: " + response);
		}
	}
}
