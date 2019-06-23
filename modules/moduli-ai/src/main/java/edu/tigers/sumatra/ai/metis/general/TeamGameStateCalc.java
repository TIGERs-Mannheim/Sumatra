/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.referee.data.RefereeMsg;


/**
 * Check in which game state we are by consulting new referee messages
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TeamGameStateCalc extends ACalculator
{
	private boolean		goalScoredState	= false;
	private RefereeMsg	lastRefereeMsg		= null;
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		if (baseAiFrame.getPrevFrame().getGamestate() != baseAiFrame.getGamestate())
		{
			// reset
			goalScoredState = false;
		}
		
		RefereeMsg refereeMsg = baseAiFrame.getRefereeMsg();
		
		if (baseAiFrame.isNewRefereeMsg() &&
				((refereeMsg.getCommand() == Referee.SSL_Referee.Command.GOAL_BLUE)
						|| (refereeMsg.getCommand() == Referee.SSL_Referee.Command.GOAL_YELLOW)))
		{
			goalScoredCheck(lastRefereeMsg, baseAiFrame.getRefereeMsg());
		}
		
		newTacticalField.setGoalScored(goalScoredState);
		lastRefereeMsg = refereeMsg;
		newTacticalField.setGameState(baseAiFrame.getGamestate());
		
	}
	
	
	private void goalScoredCheck(final RefereeMsg latestRef, final RefereeMsg currentRef)
	{
		if ((latestRef != null) && (currentRef != null))
		{
			int scoreYellowCurrent = currentRef.getTeamInfoYellow().getScore();
			int scoreYellowLast = latestRef.getTeamInfoYellow().getScore();
			int scoreBlueCurrent = currentRef.getTeamInfoBlue().getScore();
			int scoreBlueLast = latestRef.getTeamInfoBlue().getScore();
			if ((scoreYellowCurrent > scoreYellowLast) || (scoreBlueCurrent > scoreBlueLast))
			{
				goalScoredState = true;
			}
		}
		
	}
}
