/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine;

import java.util.Arrays;
import java.util.EnumMap;
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
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;


/**
 * @author "Lukas Magel"
 */
public class ActiveAutoRefEngine extends AbstractAutoRefEngine
{
	private final IRefboxRemote remote;
	private List<IAutoRefEngineObserver> engineObserver = new CopyOnWriteArrayList<>();
	private IAutoRefState dummyState = null;
	private Map<EGameState, IAutoRefState> refStates = new EnumMap<>(EGameState.class);
	private FollowUpAction followUp = null;
	private boolean doProceed = false;
	
	
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
		refStates.put(EGameState.RUNNING, new RunningState());
		refStates.put(EGameState.PREPARE_KICKOFF, new PrepareKickoffState());
		refStates.put(EGameState.PREPARE_PENALTY, new PreparePenaltyState());
		refStates.put(EGameState.BALL_PLACEMENT, new PlaceBallState());
		
		KickState kickState = new KickState();
		putForAll(kickState, Arrays.asList(
				EGameState.DIRECT_FREE, EGameState.INDIRECT_FREE,
				EGameState.KICKOFF, EGameState.PENALTY));
		
		refStates.put(EGameState.STOP, new StopState());
		
		dummyState = new DummyAutoRefState();
	}
	
	
	private void putForAll(final IAutoRefState refState, final List<EGameState> states)
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
	 * proceed
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
		refStates.values().forEach(IAutoRefState::reset);
	}
	
	
	private IAutoRefState getActiveState(final GameState gameState)
	{
		IAutoRefState state = refStates.get(gameState.getState());
		if (state == null)
		{
			return dummyState;
		}
		return state;
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
		if (!gameEvents.isEmpty())
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
	protected void onGameStateChange(final GameState oldGameState, final GameState newGameState)
	{
		super.onGameStateChange(oldGameState, newGameState);
		
		IAutoRefState oldRefState = getActiveState(oldGameState);
		IAutoRefState newRefState = getActiveState(newGameState);
		if ((oldRefState != newRefState) && (newRefState != null))
		{
			newRefState.reset();
		}
		
		notifyStateChange(false);
		
		if (newGameState.getState() == EGameState.RUNNING)
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
	
	
	/**
	 * @author Lukas Magel
	 */
	public interface IAutoRefEngineObserver
	{
		
		/**
		 * @param proceedPossible
		 */
		void onStateChanged(final boolean proceedPossible);
		
		
		/**
		 * @param action
		 */
		void onFollowUpChanged(final FollowUpAction action);
		
	}
	
	private class RefStateContext implements IAutoRefStateContext
	{
		
		private RefStateContext()
		{
		}
		
		
		@Override
		public ICommandResult sendCommand(final RefboxRemoteCommand cmd)
		{
			gameLog.addEntry(cmd);
			return remote.sendCommand(cmd);
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
}
