/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreatDefData;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.goal.EPossibleGoal;
import edu.tigers.sumatra.ai.metis.statistics.stats.AStatsCalc;
import edu.tigers.sumatra.ai.metis.statistics.stats.BallDefenderInTimeStatsCalc;
import edu.tigers.sumatra.ai.metis.statistics.stats.BallPossessionStatsCalc;
import edu.tigers.sumatra.ai.metis.statistics.stats.DefenseCoverageStatsCalc;
import edu.tigers.sumatra.ai.metis.statistics.stats.DefenseThreatRatingStatsCalc;
import edu.tigers.sumatra.ai.metis.statistics.stats.DirectShotsStatsCalc;
import edu.tigers.sumatra.ai.metis.statistics.stats.DuelStatsCalc;
import edu.tigers.sumatra.ai.metis.statistics.stats.GoalStatsCalc;
import edu.tigers.sumatra.ai.metis.statistics.stats.MatchStats;
import edu.tigers.sumatra.ai.metis.statistics.stats.RoleTimeStatsCalc;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.vision.data.IKickEvent;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


/**
 * This calculator generates statistics for a game.
 */
public class MatchStatisticsCalc extends ACalculator
{
	private final Collection<AStatsCalc> statisticsSubscriber = new ArrayList<>();
	@Getter
	private MatchStats matchStatistics;


	public MatchStatisticsCalc(
			Supplier<Map<EPlay, Set<BotID>>> desiredBots,
			Supplier<BallPossession> ballPossession,
			Supplier<Set<BotID>> botsLastTouchedBall,
			Supplier<EPossibleGoal> possibleGoal,
			Supplier<IKickEvent> detectedGoalKickTigers,
			Supplier<BotDistance> opponentClosestToBall,
			Supplier<ITrackedBot> opponentPassReceiver,
			Supplier<List<DefenseBotThreatDefData>> defenseBotThreats,
			Supplier<Map<Integer, Double>> defenseThreatRatingForNumDefender,
			Supplier<List<DefenseThreatAssignment>> defenseThreatAssignments,
			Supplier<Set<BotID>> currentlyTouchingBots)
	{
		register(new BallPossessionStatsCalc(
				ballPossession
		));
		register(new GoalStatsCalc(
				botsLastTouchedBall,
				possibleGoal
		));
		register(new RoleTimeStatsCalc());
		register(new DuelStatsCalc(
				ballPossession
		));
		register(new DirectShotsStatsCalc(
				detectedGoalKickTigers,
				possibleGoal,
				currentlyTouchingBots
		));
		var defenseCoverageStatsCalc = register(new DefenseCoverageStatsCalc(
				opponentClosestToBall
		));

		register(new BallDefenderInTimeStatsCalc(
				opponentPassReceiver,
				defenseThreatAssignments,
				currentlyTouchingBots,
				defenseCoverageStatsCalc::getUncoveredRangeDeg
		));

		register(new DefenseThreatRatingStatsCalc(
				desiredBots,
				defenseBotThreats,
				defenseThreatRatingForNumDefender,
				defenseThreatAssignments
		));
	}


	private <T extends AStatsCalc> T register(T calc)
	{
		statisticsSubscriber.add(calc);
		return calc;
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
