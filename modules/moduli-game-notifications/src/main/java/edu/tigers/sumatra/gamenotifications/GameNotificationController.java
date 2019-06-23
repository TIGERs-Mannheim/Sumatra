/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gamenotifications;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.gamenotifications.events.GameContinuesEvent;
import edu.tigers.sumatra.gamenotifications.events.GameEndEvent;
import edu.tigers.sumatra.gamenotifications.events.GameStartEvent;
import edu.tigers.sumatra.gamenotifications.events.GoalScoredEvent;
import edu.tigers.sumatra.gamenotifications.events.HalfTimeEvent;
import edu.tigers.sumatra.gamenotifications.events.PenaltyShootoutEvent;
import edu.tigers.sumatra.gamenotifications.events.StartCommandEvent;
import edu.tigers.sumatra.gamenotifications.events.StopCommandEvent;
import edu.tigers.sumatra.gamenotifications.events.TimeoutEvent;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.IRefereeObserver;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class GameNotificationController extends AModule implements IRefereeObserver
{
	
	public static final String MODULE_TYPE = "GameNotificationController";
	public static final String MODULE_ID = "game_notification_controller";
	
	private static final Logger log = Logger
			.getLogger(GameNotificationController.class.getName());
	
	private List<IGameNotificationObserver> gameNotificationObservers;
	private int goalSum = 0;
	
	private Referee.SSL_Referee.Command lastRefCommand = null;
	private Referee.SSL_Referee.Stage lastStage = null;
	
	
	/**
	 * Creates a new Controller
	 *
	 * @param subconfig
	 */
	public GameNotificationController(SubnodeConfiguration subconfig)
	{
		// Nothing to do.
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
		
		gameNotificationObservers = new ArrayList<>();
		goalSum = 0;
	}
	
	
	@Override
	public void deinitModule()
	{
		
		for (int i = gameNotificationObservers.size() - 1; i >= 0; i--)
		{
			
			gameNotificationObservers.remove(i);
		}
		
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		try
		{
			AReferee aRef = (AReferee) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
			aRef.addObserver(this);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find agent", e);
		}
	}
	
	
	@Override
	public void stopModule()
	{
		try
		{
			AReferee aRef = (AReferee) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
			aRef.removeObserver(this);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find agent", e);
		}
	}
	
	
	@Override
	public void onNewRefereeMsg(final Referee.SSL_Referee refMsg)
	{
		Referee.SSL_Referee.Stage stage = refMsg.getStage();
		Referee.SSL_Referee.Command command = refMsg.getCommand();
		if (lastStage != null && !stage.equals(lastStage))
		{
			calculateStageEvent(refMsg, stage);
		}
		
		// Determine event to distribute
		calculateCommandEvent(refMsg, command);
		
		lastRefCommand = command;
		lastStage = stage;
	}
	
	
	private void calculateStageEvent(final Referee.SSL_Referee refMsg, final Referee.SSL_Referee.Stage stage)
	{
		
		log.trace("Processing new stage: " + stage.toString());
		
		switch (stage)
		{
			case NORMAL_FIRST_HALF:
			case NORMAL_SECOND_HALF:
			case EXTRA_FIRST_HALF:
			case EXTRA_SECOND_HALF:
				processNormalHalf(refMsg);
				break;
			case NORMAL_HALF_TIME:
			case EXTRA_HALF_TIME:
				processNormalHalfTime(refMsg);
				break;
			case PENALTY_SHOOTOUT:
				processPenaltyShootout(refMsg);
				break;
			case POST_GAME:
				processPostGame(refMsg);
				break;
			default:
				break;
		}
	}
	
	
	private void calculateCommandEvent(final Referee.SSL_Referee refMsg, final Referee.SSL_Referee.Command command)
	{
		switch (command)
		{
			case GOAL_BLUE:
			case GOAL_YELLOW:
				processGoalCommand(refMsg);
				break;
			case NORMAL_START:
			case FORCE_START:
				processStartCommand(refMsg);
				break;
			case STOP:
			case HALT:
				processStopCommand(refMsg);
				break;
			case TIMEOUT_BLUE:
			case TIMEOUT_YELLOW:
				processTimeoutCommand(refMsg);
				break;
			default:
				break;
		}
	}
	
	
	private void processNormalHalfTime(final Referee.SSL_Referee refMsg)
	{
		
		distributeEvent(new HalfTimeEvent(refMsg));
	}
	
	
	private void processPenaltyShootout(final Referee.SSL_Referee refMsg)
	{
		
		if (Referee.SSL_Referee.Stage.PENALTY_SHOOTOUT_BREAK.equals(lastStage))
		{
			return;
		}
		
		distributeEvent(new PenaltyShootoutEvent(refMsg));
	}
	
	
	private void processPostGame(final Referee.SSL_Referee refMsg)
	{
		
		distributeEvent(new GameEndEvent(refMsg));
	}
	
	
	private void processNormalHalf(final Referee.SSL_Referee refMsg)
	{
		
		if (lastStage == null || Referee.SSL_Referee.Stage.NORMAL_FIRST_HALF_PRE.equals(lastStage))
		{
			distributeEvent(new GameStartEvent(refMsg));
			return;
		}
		
		distributeEvent(new GameContinuesEvent(refMsg));
	}
	
	
	private void processGoalCommand(final Referee.SSL_Referee refMsg)
	{
		
		int goalCounter = refMsg.getBlue().getScore() + refMsg.getYellow().getScore();
		if (goalSum != goalCounter)
		{
			
			Referee.SSL_Referee.TeamInfo teamScoring;
			Referee.SSL_Referee.TeamInfo teamOther;
			
			switch (refMsg.getCommand())
			{
				case GOAL_BLUE:
					teamScoring = refMsg.getBlue();
					teamOther = refMsg.getYellow();
					break;
				case GOAL_YELLOW:
					teamScoring = refMsg.getYellow();
					teamOther = refMsg.getBlue();
					break;
				default:
					return;
			}
			
			GoalScoredEvent e = new GoalScoredEvent(teamScoring, teamOther, refMsg);
			
			if (goalSum < goalCounter)
			{
				e.setValidChange(false);
			}
			
			goalSum = goalCounter;
			
			distributeEvent(e);
		}
		
	}
	
	
	private void processStartCommand(final Referee.SSL_Referee refMsg)
	{
		
		Referee.SSL_Referee.Command refCommand = refMsg.getCommand();
		if (refCommand.equals(lastRefCommand))
		{
			return;
		}
		
		switch (refCommand)
		{
			case NORMAL_START:
				distributeEvent(new StartCommandEvent(StartCommandEvent.StartType.NORMAL, refMsg));
				break;
			case FORCE_START:
				distributeEvent(new StartCommandEvent(StartCommandEvent.StartType.FORCE, refMsg));
				break;
			default:
				log.error("Processing start command without start command being sent. This should not happen!");
				break;
		}
		
	}
	
	
	private void processStopCommand(final Referee.SSL_Referee refMsg)
	{
		
		if (refMsg.getCommand().equals(lastRefCommand))
		{
			return;
		}
		
		if (Referee.SSL_Referee.Command.TIMEOUT_BLUE.equals(lastRefCommand)
				|| Referee.SSL_Referee.Command.TIMEOUT_YELLOW.equals(lastRefCommand))
		{
			distributeEvent(new GameContinuesEvent(refMsg));
			return;
		}
		
		distributeEvent(new StopCommandEvent(refMsg));
	}
	
	
	private void processTimeoutCommand(final Referee.SSL_Referee refMsg)
	{
		
		if (refMsg.getCommand().equals(lastRefCommand))
		{
			return;
		}
		
		distributeEvent(new TimeoutEvent(refMsg));
	}
	
	
	private synchronized void distributeEvent(final AGameEvent e)
	{
		for (IGameNotificationObserver o : gameNotificationObservers)
		{
			o.onGameEvent(e);
		}
	}
	
	
	/**
	 * Adds the given observer
	 * 
	 * @param o
	 */
	public synchronized void addObserver(IGameNotificationObserver o)
	{
		gameNotificationObservers.add(o);
	}
	
	
	/**
	 * Removes the given observer
	 * 
	 * @param o
	 */
	public synchronized void removeObserver(final IGameNotificationObserver o)
	{
		gameNotificationObservers.remove(o);
	}
}
