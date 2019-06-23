/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics;

import static edu.tigers.sumatra.ai.data.MatchStats.EMatchStatistics;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.EPossibleGoal;
import edu.tigers.sumatra.ai.data.MatchStats;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class GoalStats extends AStats
{
	private static final Logger log = Logger.getLogger(GoalStats.class.getName());
	private static final int BALL_CONTACT_BUFFER_SIZE = 5;
	private final Map<BotID, Integer> possibleBotGoals = new HashMap<>();
	private EPossibleGoal lastPossibleGoalVal = EPossibleGoal.NO_ONE;
	
	private Deque<BotID> ballContactBuffer = new LinkedList<>();
	
	
	@Override
	public void saveStatsToMatchStatistics(final MatchStats matchStatistics)
	{
		int possibleGoalSum = possibleBotGoals.values().stream().mapToInt(a -> a).sum();
		StatisticData possibleGoalsTigersStats = new StatisticData(possibleBotGoals, possibleGoalSum);
		matchStatistics.putStatisticData(EMatchStatistics.GOALS_SCORED, possibleGoalsTigersStats);
	}
	
	
	@Override
	public void onStatisticUpdate(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		BotID botLastTouched = newTacticalField.getBotLastTouchedBall();
		updateBallContactBuffer(botLastTouched);
		
		EPossibleGoal ePossibleGoal = newTacticalField.getPossibleGoal();
		if (ePossibleGoal == lastPossibleGoalVal)
		{
			return;
		}
		lastPossibleGoalVal = ePossibleGoal;
		
		// we are only interested in our goals
		if (ePossibleGoal != EPossibleGoal.WE)
		{
			return;
		}
		
		BotID shooter = null;
		for (BotID botID : ballContactBuffer)
		{
			if (botID.getTeamColor() == baseAiFrame.getTeamColor())
			{
				shooter = botID;
				break;
			}
		}
		
		if (shooter != null)
		{
			possibleBotGoals.putIfAbsent(shooter, 0);
			possibleBotGoals.compute(shooter, (id, goals) -> goals + 1);
		} else
		{
			log.debug("Could not determine goal shooter. None of last touched bots is ours: " + ballContactBuffer);
		}
	}
	
	
	private void updateBallContactBuffer(final BotID botLastTouched)
	{
		if (ballContactBuffer.isEmpty() || ballContactBuffer.getLast() != botLastTouched)
		{
			ballContactBuffer.addLast(botLastTouched);
			if (ballContactBuffer.size() > BALL_CONTACT_BUFFER_SIZE)
			{
				ballContactBuffer.removeFirst();
			}
		}
	}
}
