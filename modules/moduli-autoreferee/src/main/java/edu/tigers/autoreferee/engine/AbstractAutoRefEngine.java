/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.EGameEventDetectorType;
import edu.tigers.autoreferee.engine.events.GameEventEngine;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.engine.log.GameLog;
import edu.tigers.autoreferee.engine.log.GameTime;
import edu.tigers.autoreferee.engine.log.IGameLog;
import edu.tigers.sumatra.Referee.SSL_Referee.Stage;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;


/**
 * @author "Lukas Magel"
 */
public abstract class AbstractAutoRefEngine implements IAutoRefEngine
{
	private static final Logger log = Logger.getLogger(AbstractAutoRefEngine.class);
	
	private GameEventEngine gameEventEngine;
	protected EEngineState engineState;
	protected GameLog gameLog = new GameLog();
	protected AutoRefGlobalState autoRefGlobalState = new AutoRefGlobalState();
	protected Set<EGameEvent> activeGameEvents = new HashSet<>();
	
	
	protected enum EEngineState
	{
		RUNNING,
		PAUSED
	}
	
	
	/**
	 * Create new instance
	 */
	protected AbstractAutoRefEngine()
	{
		gameEventEngine = new GameEventEngine();
		engineState = EEngineState.RUNNING;
		activeGameEvents.addAll(Arrays.asList(EGameEvent.values()));
	}
	
	
	@Override
	public synchronized void setActiveGameEventDetectors(final Set<EGameEventDetectorType> types)
	{
		gameEventEngine.setActiveDetectors(types);
	}
	
	
	@Override
	public void setActiveGameEvents(final Set<EGameEvent> activeGameEvents)
	{
		this.activeGameEvents = activeGameEvents;
	}
	
	
	protected List<IGameEvent> getGameEvents(final IAutoRefFrame frame)
	{
		return gameEventEngine.update(frame);
	}
	
	
	@Override
	public synchronized void process(final IAutoRefFrame frame)
	{
		gameLog.setCurrentTimestamp(frame.getTimestamp());
		gameLog.setCurrentGameTime(calcCurrentGameTime(frame));
		
		RefereeMsg curRefMsg = frame.getRefereeMsg();
		RefereeMsg lastRefMsg = frame.getPreviousFrame().getRefereeMsg();
		if (curRefMsg.getCommandCounter() != lastRefMsg.getCommandCounter())
		{
			gameLog.addEntry(curRefMsg);
		}
		
		GameState curGameState = frame.getGameState();
		GameState lastGameState = frame.getPreviousFrame().getGameState();
		if (!curGameState.equals(lastGameState))
		{
			onGameStateChange(lastGameState, curGameState);
		}
		
		Stage curStage = frame.getRefereeMsg().getStage();
		Stage previousStage = frame.getPreviousFrame().getRefereeMsg().getStage();
		if (curStage != previousStage)
		{
			onStageChange(previousStage, curStage);
		}
	}
	
	
	// oldGameState needed for child method
	@SuppressWarnings("squid:S1172")
	protected void onGameStateChange(final GameState oldGameState, final GameState newGameState)
	{
		gameLog.addEntry(newGameState);
	}
	
	
	protected void onStageChange(final Stage oldStage, final Stage newStage)
	{
	}
	
	
	protected void logGameEvents(final List<IGameEvent> gameEvents)
	{
		gameEvents.forEach(gameLog::addEntry);
	}
	
	
	protected GameTime calcCurrentGameTime(final IAutoRefFrame frame)
	{
		RefereeMsg refMsg = frame.getRefereeMsg();
		return GameTime.of(refMsg);
	}
	
	
	@Override
	public IGameLog getGameLog()
	{
		return gameLog;
	}
	
	
	@Override
	public synchronized void reset()
	{
		log.debug("Autoref Engine reset");
		gameEventEngine.reset();
	}
	
	
	@Override
	public synchronized void resume()
	{
		log.debug("Autoref Engine resumed");
		gameEventEngine.reset();
		engineState = EEngineState.RUNNING;
	}
	
	
	@Override
	public synchronized void pause()
	{
		log.debug("Autoref Engine paused");
		engineState = EEngineState.PAUSED;
	}
}
