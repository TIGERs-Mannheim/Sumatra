/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics.stats;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.goal.EPossibleGoal;
import edu.tigers.sumatra.ids.BotID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Log4j2
@RequiredArgsConstructor
public class GoalStatsCalc extends AStatsCalc
{
	private static final int BALL_CONTACT_BUFFER_SIZE = 5;

	private final Supplier<Set<BotID>> botsLastTouchedBall;
	private final Supplier<EPossibleGoal> possibleGoal;

	private final Map<Integer, Integer> possibleBotGoals = new HashMap<>();
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
	public void onStatisticUpdate(final BaseAiFrame baseAiFrame)
	{
		Set<BotID> botLastTouched = botsLastTouchedBall.get();
		if (botLastTouched.size() == 1)
		{
			updateBallContactBuffer(botLastTouched.iterator().next());
		}

		EPossibleGoal ePossibleGoal = possibleGoal.get();
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
			possibleBotGoals.compute(shooter.getNumber(), (id, goals) -> goals == null ? 1 : goals + 1);
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
