/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gamenotifications.events;

import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.gamenotifications.AGameEvent;
import edu.tigers.sumatra.gamenotifications.EGameEvent;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class GoalScoredEvent extends AGameEvent
{
	
	private Referee.SSL_Referee.TeamInfo teamScoring;
	private Referee.SSL_Referee.TeamInfo teamOther;
	
	private boolean validChange = true;
	
	
	/**
	 * Creates a new GoalScoredEvent
	 *
	 * @param teamScoring The teamScoring which scored a goal
	 * @param teamOther The opposite team
	 * @param refMsg
	 */
	public GoalScoredEvent(Referee.SSL_Referee.TeamInfo teamScoring, Referee.SSL_Referee.TeamInfo teamOther,
			Referee.SSL_Referee refMsg)
	{
		super(EGameEvent.GOAL_SCORED, refMsg);
		
		this.teamScoring = teamScoring;
		this.teamOther = teamOther;
	}
	
	
	public Referee.SSL_Referee.TeamInfo getTeamScoring()
	{
		return teamScoring;
	}
	
	
	public Referee.SSL_Referee.TeamInfo getTeamOther()
	{
		return teamOther;
	}
	
	
	public boolean isValidChange()
	{
		return validChange;
	}
	
	
	public void setValidChange(final boolean validChange)
	{
		this.validChange = validChange;
	}
}
