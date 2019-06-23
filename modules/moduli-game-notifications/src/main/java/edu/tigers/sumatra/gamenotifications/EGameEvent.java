/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gamenotifications;

import edu.tigers.sumatra.gamenotifications.events.GameContinuesEvent;
import edu.tigers.sumatra.gamenotifications.events.GameEndEvent;
import edu.tigers.sumatra.gamenotifications.events.GameStartEvent;
import edu.tigers.sumatra.gamenotifications.events.GoalScoredEvent;
import edu.tigers.sumatra.gamenotifications.events.HalfTimeEvent;
import edu.tigers.sumatra.gamenotifications.events.PenaltyShootoutEvent;
import edu.tigers.sumatra.gamenotifications.events.StartCommandEvent;
import edu.tigers.sumatra.gamenotifications.events.StopCommandEvent;
import edu.tigers.sumatra.gamenotifications.events.TimeoutEvent;
import edu.tigers.sumatra.gamenotifications.events.YellowCardEvent;
import edu.tigers.sumatra.gamenotifications.events.YellowCardOverEvent;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public enum EGameEvent
{
	
	GAME_STARTS(GameStartEvent.class),
	GAME_ENDS(GameEndEvent.class),
	GAME_CONTINUES(GameContinuesEvent.class),
	PENALTY_SHOOTOUT(PenaltyShootoutEvent.class),
	START_COMMAND(StartCommandEvent.class),
	STOP_COMMAND(StopCommandEvent.class),
	TIMEOUT(TimeoutEvent.class),
	HALF_TIME(HalfTimeEvent.class),
	GOAL_SCORED(GoalScoredEvent.class),
	YELLOW_CARD(YellowCardEvent.class),
	YELLOW_CARD_OVER(YellowCardOverEvent.class);
	
	private Class impl;
	
	
	EGameEvent(Class impl)
	{
	}
	
	
	public Class getImpl()
	{
		
		return impl;
	}
}
