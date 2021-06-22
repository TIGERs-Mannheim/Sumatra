/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.goal.EPossibleGoal;
import edu.tigers.sumatra.ai.metis.statistics.stats.AStatsCalc;
import edu.tigers.sumatra.ai.metis.statistics.stats.BallPossessionStatsCalc;
import edu.tigers.sumatra.ai.metis.statistics.stats.DefenseCoverageStatsCalc;
import edu.tigers.sumatra.ai.metis.statistics.stats.DirectShotsStatsCalc;
import edu.tigers.sumatra.ai.metis.statistics.stats.DuelStatsCalc;
import edu.tigers.sumatra.ai.metis.statistics.stats.GoalStatsCalc;
import edu.tigers.sumatra.ai.metis.statistics.stats.MatchStats;
import edu.tigers.sumatra.ai.metis.statistics.stats.RoleTimeStatsCalc;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.vision.data.IKickEvent;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;


/**
 * This calculator generates statistics for a game.
 */
public class MatchStatisticsCalc extends ACalculator
{
	@Getter
	private MatchStats matchStatistics;


	private final Collection<AStatsCalc> statisticsSubscriber = new ArrayList<>();


	public MatchStatisticsCalc(
			Supplier<BallPossession> ballPossession,
			Supplier<Set<BotID>> botsLastTouchedBall,
			Supplier<EPossibleGoal> possibleGoal,
			Supplier<IKickEvent> detectedGoalKickTigers,
			Supplier<BotDistance> opponentClosestToBall)
	{
		statisticsSubscriber.add(new BallPossessionStatsCalc(
				ballPossession
		));
		statisticsSubscriber.add(new GoalStatsCalc(
				botsLastTouchedBall,
				possibleGoal
		));
		statisticsSubscriber.add(new RoleTimeStatsCalc());
		statisticsSubscriber.add(new DuelStatsCalc(
				ballPossession
		));
		statisticsSubscriber.add(new DirectShotsStatsCalc(
				detectedGoalKickTigers,
				possibleGoal
		));
		statisticsSubscriber.add(new DefenseCoverageStatsCalc(
				opponentClosestToBall
		));
	}


	@Override
	public void doCalc()
	{
		if (getWFrame().getBots().isEmpty() || !getAiFrame().getGameState().isGameRunning())
		{
			createMatchStatistics();
			return;
		}

		updateStatistics();
		createMatchStatistics();
	}


	private void updateStatistics()
	{
		for (AStatsCalc statisticToUpdate : statisticsSubscriber)
		{
			statisticToUpdate.onStatisticUpdate(getAiFrame());
		}
	}


	private void createMatchStatistics()
	{
		matchStatistics = new MatchStats();
		for (AStatsCalc statisticsToReceiveDataFrom : statisticsSubscriber)
		{
			statisticsToReceiveDataFrom.saveStatsToMatchStatistics(matchStatistics);
		}
	}
}
