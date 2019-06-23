/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.10.2016
 * Author(s): Sebastian Stein <sebastian-stein@gmx.de>
 * *********************************************************
 */
package edu.tigers.sumatra.telegram;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;

import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.gamenotifications.AGameEvent;
import edu.tigers.sumatra.gamenotifications.GameNotificationController;
import edu.tigers.sumatra.gamenotifications.IGameNotificationObserver;
import edu.tigers.sumatra.gamenotifications.events.GameEndEvent;
import edu.tigers.sumatra.gamenotifications.events.GameStartEvent;
import edu.tigers.sumatra.gamenotifications.events.GoalScoredEvent;
import edu.tigers.sumatra.gamenotifications.events.TimeoutEvent;
import edu.tigers.sumatra.model.SumatraModel;


/**
 * Sends notifications about the running game to subscribing channels
 * 
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class TelegramNotificationController extends ATelegramNotificationController
		implements IGameNotificationObserver
{
	private static final Logger log = Logger
			.getLogger(TelegramNotificationController.class.getName());
	
	
	private NotificationWorker notificationWorker;
	
	
	static
	{
		ConfigRegistration.registerClass("telegram", TelegramNotificationController.class);
	}
	
	
	/**
	 * Creates a new controller instance
	 * 
	 * @param subconfig The SubnodeConfiguration to use
	 */
	public TelegramNotificationController(final SubnodeConfiguration subconfig)
	{
		// Nothing to do....
	}
	
	
	/**
	 * Returns true if broadcasting is enabled
	 * 
	 * @return the broadcastEnabled
	 */
	public static boolean isBroadcastEnabled()
	{
		return "true".equals(SumatraModel.getInstance().getUserProperty("telegram_broadcasting", "false"));
	}
	
	
	@Override
	public void deinitModule()
	{
		// Nothing to do....
	}
	
	
	private String getGoalScore(final String nameBlue, final String nameYellow, final int scoreBlue,
			final int scoreYellow)
	{
		
		String message;
		if (scoreYellow > scoreBlue)
		{
			message = nameYellow + " <b>" + scoreYellow + "</b> : <b>" + scoreBlue + "</b> " + nameBlue;
		} else
		{
			message = nameBlue + " <b>" + scoreBlue + "</b> : <b>" + scoreYellow + "</b> " + nameYellow;
		}
		
		return message;
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
		// Nothing to do....
	}
	
	
	/**
	 * Adds the message to the sending queue
	 * 
	 * @param message
	 */
	private void notifySubscribingChats(final String message)
	{
		
		notificationWorker.addMessage(message);
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		
		notificationWorker = NotificationWorker.getInstance();
		notificationWorker.start();
		
		try
		{
			GameNotificationController gameNotificationController = (GameNotificationController) SumatraModel.getInstance()
					.getModule(GameNotificationController.MODULE_ID);
			gameNotificationController.addObserver(this);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find agent", e);
		}
		
	}
	
	
	@Override
	public void stopModule()
	{
		notificationWorker.scheduleStop();
		try
		{
			notificationWorker.join();
		} catch (InterruptedException e)
		{
			log.error("Not able to wait for child threads to die", e);
			Thread.currentThread().interrupt();
		}
		
		NotificationWorker.destroyInstance();
		
		try
		{
			GameNotificationController gameNotificationController = (GameNotificationController) SumatraModel.getInstance()
					.getModule(GameNotificationController.MODULE_ID);
			gameNotificationController.removeObserver(this);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find agent", e);
		}
	}
	
	
	@Override
	public void onGameEvent(final AGameEvent e)
	{
		if (!isBroadcastEnabled())
		{
			return;
		}
		
		switch (e.getEventType())
		{
			case GOAL_SCORED:
				processGoal((GoalScoredEvent) e);
				break;
			case GAME_STARTS:
				processGameStartEvent((GameStartEvent) e);
				break;
			case GAME_ENDS:
				processGameEndEvent((GameEndEvent) e);
				break;
			case HALF_TIME:
				processHalfTimeEvent();
				break;
			case GAME_CONTINUES:
				processGameContinuesEvent();
				break;
			case TIMEOUT:
				processTimeoutEvent((TimeoutEvent) e);
				break;
			case PENALTY_SHOOTOUT:
				processPenaltyShootoutEvent();
				break;
			default:
				break;
		}
	}
	
	
	private void processPenaltyShootoutEvent()
	{
		
		notifySubscribingChats("Penalty shootout is now starting!");
	}
	
	
	private void processTimeoutEvent(final TimeoutEvent e)
	{
		
		notifySubscribingChats("Timeout! (" + e.getTeam().getName() + ")");
	}
	
	
	private void processGameContinuesEvent()
	{
		
		notifySubscribingChats("The game continues.");
	}
	
	
	private void processHalfTimeEvent()
	{
		
		notifySubscribingChats("It's half time.");
	}
	
	
	private void processGameEndEvent(final GameEndEvent e)
	{
		
		Referee.SSL_Referee ref = e.getRefMsg();
		
		notifySubscribingChats("The match is over. Final score:\n" + getGoalScore(ref.getBlue().getName(),
				ref.getYellow().getName(), ref.getBlue().getScore(), ref.getYellow().getScore()));
	}
	
	
	private void processGameStartEvent(final GameStartEvent e)
	{
		
		notifySubscribingChats("The match <b>" + e.getBlue().getName() + "</b> vs <b>" + e.getYellow().getName()
				+ "</b> is now starting.");
	}
	
	
	private void processGoal(GoalScoredEvent e)
	{
		
		int goalsScoringTeam = e.getTeamScoring().getScore();
		int goalsOtherTeam = e.getTeamOther().getScore();
		
		String nameScoringTeam = e.getTeamScoring().getName();
		String nameOtherTeam = e.getTeamOther().getName();
		
		String goals = getGoalScore(nameScoringTeam, nameOtherTeam, goalsScoringTeam, goalsOtherTeam);
		String message;
		
		if (e.isValidChange())
		{
			
			message = "<b>" + nameScoringTeam + "</b> scored a goal!";
			
		} else
		{
			
			message = "Score changed.";
		}
		
		notifySubscribingChats(message + "\n" + goals);
		
	}
}
