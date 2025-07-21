/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreatDefData;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.goal.EPossibleGoal;
import edu.tigers.sumatra.ai.metis.pass.PassStats;
import edu.tigers.sumatra.ai.metis.statistics.stats.AStatsCalc;
import edu.tigers.sumatra.ai.metis.statistics.stats.BallDefenderInTimeStatsCalc;
import edu.tigers.sumatra.ai.metis.statistics.stats.BallPossessionStatsCalc;
import edu.tigers.sumatra.ai.metis.statistics.stats.DefenseCoverageStatsCalc;
import edu.tigers.sumatra.ai.metis.statistics.stats.DefenseThreatRatingStatsCalc;
import edu.tigers.sumatra.ai.metis.statistics.stats.DirectShotsStatsCalc;
import edu.tigers.sumatra.ai.metis.statistics.stats.DuelStatsCalc;
import edu.tigers.sumatra.ai.metis.statistics.stats.GoalStatsCalc;
import edu.tigers.sumatra.ai.metis.statistics.stats.MatchStats;
import edu.tigers.sumatra.ai.metis.statistics.stats.PassStatsStatsCalc;
import edu.tigers.sumatra.ai.metis.statistics.stats.RoleTimeStatsCalc;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.KickedBall;
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

	@Configurable(defValue = "true", comment = "Enable statistics calculation")
	private static boolean enabled = true;


	@SuppressWarnings("squid:S107")
	public MatchStatisticsCalc(
			Supplier<Map<EPlay, Set<BotID>>> desiredBots,
			Supplier<BallPossession> ballPossession,
			Supplier<Set<BotID>> botsLastTouchedBall,
			Supplier<EPossibleGoal> possibleGoal,
			Supplier<KickedBall> detectedGoalKickTigers,
			Supplier<BotDistance> opponentClosestToBall,
			Supplier<ITrackedBot> opponentPassReceiver,
			Supplier<List<DefenseBotThreatDefData>> defenseBotThreats,
			Supplier<Map<Integer, Double>> defenseThreatRatingForNumDefender,
			Supplier<List<DefenseThreatAssignment>> defenseThreatAssignments,
			Supplier<Set<BotID>> currentlyTouchingBots,
			Supplier<PassStats> passStats)
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

		register(new PassStatsStatsCalc(
				passStats
		));
	}


	private <T extends AStatsCalc> T register(T calc)
	{
		statisticsSubscriber.add(calc);
		return calc;
	}


	@Override
	protected boolean isCalculationNecessary()
	{
		return enabled && !getWFrame().getBots().isEmpty() && getAiFrame().getGameState().isGameRunning();
	}


	@Override
	public void doCalc()
	{
		updateStatistics();
		matchStatistics = createMatchStatistics();
	}


	@Override
	protected void reset()
	{
		matchStatistics = createMatchStatistics();
	}


	private void updateStatistics()
	{
		for (AStatsCalc statisticToUpdate : statisticsSubscriber)
		{
			statisticToUpdate.onStatisticUpdate(getAiFrame());
		}
	}


	private MatchStats createMatchStatistics()
	{
		var matchStats = new MatchStats();
		for (AStatsCalc statisticsToReceiveDataFrom : statisticsSubscriber)
		{
			statisticsToReceiveDataFrom.saveStatsToMatchStatistics(matchStats);
		}
		return matchStats;
	}

	public static void setEnabled(final boolean enabled)
	{
		MatchStatisticsCalc.enabled = enabled;
	}
}
