/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 3, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.engine.log.GameLog;
import edu.tigers.autoreferee.engine.states.IAutoRefState;
import edu.tigers.autoreferee.engine.states.IAutoRefStateContext;
import edu.tigers.autoreferee.engine.states.impl.DummyAutoRefState;
import edu.tigers.autoreferee.engine.states.impl.KickState;
import edu.tigers.autoreferee.engine.states.impl.PlaceBallState;
import edu.tigers.autoreferee.engine.states.impl.PrepareKickoffState;
import edu.tigers.autoreferee.engine.states.impl.PreparePenaltyState;
import edu.tigers.autoreferee.engine.states.impl.RunningState;
import edu.tigers.autoreferee.engine.states.impl.StopState;
import edu.tigers.autoreferee.remote.ICommandResult;
import edu.tigers.autoreferee.remote.IRefboxRemote;
import edu.tigers.sumatra.Referee.SSL_Referee.Stage;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;


/**
 * @author "Lukas Magel"
 */
public class ActiveAutoRefEngine extends AbstractAutoRefEngine
{
	private List<IAutoRefEngineObserver>				engineObserver	= new CopyOnWriteArrayList<>();
	private IAutoRefState									dummyState		= null;
	private Map<EGameStateNeutral, IAutoRefState>	refStates		= new HashMap<>();
	
	private final IRefboxRemote							remote;
	private FollowUpAction									followUp			= null;
	private boolean											doProceed		= false;
	
	
	private class RefStateContext implements IAutoRefStateContext
	{
		
		private RefStateContext()
		{
			
		}
		
		
		@Override
		public ICommandResult sendCommand(final RefCommand cmd)
		{
			return ActiveAutoRefEngine.this.sendCommand(cmd);
		}
		
		
		@Override
		public FollowUpAction getFollowUpAction()
		{
			return followUp;
		}
		
		
		@Override
		public void setFollowUpAction(final FollowUpAction action)
		{
			setFollowUp(action);
		}
		
		
		@Override
		public boolean doProceed()
		{
			return doProceed;
		}
		
		
		@Override
		public GameLog getGameLog()
		{
			return gameLog;
		}
		
	}
	
	/**
	 * @author Lukas Magel
	 */
	public interface IAutoRefEngineObserver
	{
		
		/**
		 * @param proceedPossible
		 */
		public void onStateChanged(final boolean proceedPossible);
		
		
		/**
		 * @param action
		 */
		public void onFollowUpChanged(final FollowUpAction action);
		
	}
	
	
	/**
	 * @param remote
	 */
	public ActiveAutoRefEngine(final IRefboxRemote remote)
	{
		this.remote = remote;
		setupStates();
	}
	
	
	private void setupStates()
	{
		RunningState runningState = new RunningState();
		refStates.put(EGameStateNeutral.RUNNING, runningState);
		
		PrepareKickoffState prepKickOffState = new PrepareKickoffState();
		putForAll(prepKickOffState, Arrays.asList(EGameStateNeutral.PREPARE_KICKOFF_BLUE,
				EGameStateNeutral.PREPARE_KICKOFF_YELLOW));
		
		PreparePenaltyState prepPenaltyState = new PreparePenaltyState();
		putForAll(prepPenaltyState, Arrays.asList(EGameStateNeutral.PREPARE_PENALTY_BLUE,
				EGameStateNeutral.PREPARE_PENALTY_YELLOW));
		
		PlaceBallState placeBallState = new PlaceBallState();
		putForAll(placeBallState, Arrays.asList(EGameStateNeutral.BALL_PLACEMENT_BLUE,
				EGameStateNeutral.BALL_PLACEMENT_YELLOW));
		
		KickState kickState = new KickState();
		putForAll(kickState, Arrays.asList(
				EGameStateNeutral.DIRECT_KICK_BLUE, EGameStateNeutral.DIRECT_KICK_YELLOW,
				EGameStateNeutral.INDIRECT_KICK_BLUE, EGameStateNeutral.INDIRECT_KICK_YELLOW,
				EGameStateNeutral.KICKOFF_BLUE, EGameStateNeutral.KICKOFF_YELLOW,
				EGameStateNeutral.PENALTY_BLUE, EGameStateNeutral.PENALTY_YELLOW));
		
		StopState stopState = new StopState();
		refStates.put(EGameStateNeutral.STOPPED, stopState);
		
		dummyState = new DummyAutoRefState();
	}
	
	
	private void putForAll(final IAutoRefState refState, final List<EGameStateNeutral> states)
	{
		states.forEach(state -> refStates.put(state, refState));
	}
	
	
	@Override
	public synchronized void stop()
	{
		remote.stop();
	}
	
	
	@Override
	public synchronized void reset()
	{
		super.reset();
		setFollowUp(null);
		doProceed = false;
		resetRefStates();
	}
	
	
	@Override
	public synchronized void resume()
	{
		super.resume();
		doProceed = false;
		resetRefStates();
	}
	
	
	/**
	 * 
	 */
	public synchronized void proceed()
	{
		doProceed = true;
	}
	
	
	@Override
	public AutoRefMode getMode()
	{
		return AutoRefMode.ACTIVE;
	}
	
	
	private void resetRefStates()
	{
		refStates.values().forEach(state -> state.reset());
	}
	
	
	private IAutoRefState getActiveState(final EGameStateNeutral gameState)
	{
		IAutoRefState state = refStates.get(gameState);
		if (state == null)
		{
			return dummyState;
		}
		return state;
	}
	
	
	private ICommandResult sendCommand(final RefCommand cmd)
	{
		gameLog.addEntry(cmd);
		return remote.sendCommand(cmd);
	}
	
	
	@Override
	public synchronized void process(final IAutoRefFrame frame)
	{
		if (engineState == EEngineState.PAUSED)
		{
			return;
		}
		
		super.process(frame);
		
		IAutoRefState state = getActiveState(frame.getGameState());
		RefStateContext ctx = new RefStateContext();
		
		List<IGameEvent> gameEvents = getGameEvents(frame);
		
		boolean canProceed = state.canProceed();
		if (gameEvents.size() > 0)
		{
			IGameEvent gameEvent = gameEvents.remove(0);
			boolean accepted = state.handleGameEvent(gameEvent, ctx);
			
			gameLog.addEntry(gameEvent, accepted);
			logGameEvents(gameEvents);
		}
		
		state.update(frame, ctx);
		
		if (state.canProceed() != canProceed)
		{
			notifyStateChange(state.canProceed());
		}
		
		doProceed = false;
	}
	
	
	@Override
	protected void onGameStateChange(final EGameStateNeutral oldGameState, final EGameStateNeutral newGameState)
	{
		super.onGameStateChange(oldGameState, newGameState);
		
		IAutoRefState oldRefState = getActiveState(oldGameState);
		IAutoRefState newRefState = getActiveState(newGameState);
		if ((oldRefState != newRefState) && (newRefState != null))
		{
			newRefState.reset();
		}
		
		notifyStateChange(false);
		
		if (newGameState == EGameStateNeutral.RUNNING)
		{
			setFollowUp(null);
		}
	}
	
	
	@Override
	protected void onStageChange(final Stage oldStage, final Stage newStage)
	{
		super.onStageChange(oldStage, newStage);
		
		if ((oldStage == Stage.NORMAL_FIRST_HALF) || (oldStage == Stage.NORMAL_SECOND_HALF)
				|| (oldStage == Stage.EXTRA_FIRST_HALF) || (oldStage == Stage.EXTRA_SECOND_HALF))
		{
			setFollowUp(null);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IAutoRefEngineObserver observer)
	{
		engineObserver.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IAutoRefEngineObserver observer)
	{
		engineObserver.remove(observer);
	}
	
	
	private void setFollowUp(final FollowUpAction action)
	{
		followUp = action;
		engineObserver.forEach(observer -> observer.onFollowUpChanged(followUp));
		gameLog.addEntry(followUp);
	}
	
	
	private void notifyStateChange(final boolean canProceed)
	{
		engineObserver.forEach(obs -> obs.onStateChanged(canProceed));
	}
}
