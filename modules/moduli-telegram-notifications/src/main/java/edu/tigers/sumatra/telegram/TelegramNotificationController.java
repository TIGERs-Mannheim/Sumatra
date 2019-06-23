/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.telegram;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.gamenotifications.AGameEvent;
import edu.tigers.sumatra.gamenotifications.GameNotificationController;
import edu.tigers.sumatra.gamenotifications.IGameNotificationObserver;
import edu.tigers.sumatra.gamenotifications.events.GameEndEvent;
import edu.tigers.sumatra.gamenotifications.events.GameStartEvent;
import edu.tigers.sumatra.gamenotifications.events.GoalScoredEvent;
import edu.tigers.sumatra.gamenotifications.events.TimeoutEvent;
import edu.tigers.sumatra.gamenotifications.events.YellowCardEvent;
import edu.tigers.sumatra.gamenotifications.events.YellowCardOverEvent;
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
	public void initModule()
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
	public void startModule()
	{
		notificationWorker = NotificationWorker.getInstance();
		notificationWorker.start();
		
		try
		{
			GameNotificationController gameNotificationController = SumatraModel.getInstance()
					.getModule(GameNotificationController.class);
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
			GameNotificationController gameNotificationController = SumatraModel.getInstance()
					.getModule(GameNotificationController.class);
			gameNotificationController.removeObserver(this);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find agent", e);
		}
	}
	
	
	@Override
	// Handles some enums.
	@SuppressWarnings("squid:MethodCyclomaticComplexity")
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
			case YELLOW_CARD:
				processYellowCardEvent((YellowCardEvent) e);
				break;
			case YELLOW_CARD_OVER:
				processYellowCardOverEvent((YellowCardOverEvent) e);
				break;
			default:
				break;
		}
	}
	
	
	private void processYellowCardOverEvent(final YellowCardOverEvent e)
	{
		notifySubscribingChats("Yellow card over! (" + e.getTeam().getName() + ")\n"
				+ e.getTeam().getYellowCardTimesList().size() + " cards left active");
	}
	
	
	private void processYellowCardEvent(final YellowCardEvent e)
	{
		notifySubscribingChats("Yellow card for " + e.getTeam().getName() + " (Currently "
				+ e.getTeam().getYellowCardTimesList().size() + " cards active)");
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
