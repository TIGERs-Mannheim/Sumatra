/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics.stats;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.goal.EPossibleGoal;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.vision.data.IKickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Statistics for direct Shots to goal
 */
public class DirectShotsStatsCalc extends AEventWatchingStatsCalc<DirectShotsStatsCalcWatchData>
{
	@Configurable(comment = "[m/s] The minimal Ball velocity that a shot is considered", defValue = "3.0")
	private static double minimalBallVelocity = 3.0;
	@Configurable(comment = "[mm] The minimal Ball travel distance that a shot is considered", defValue = "100.0")
	private static double maxSkirmishBallTravelDistance = 100.0;
	@Configurable(comment = "[s] Time span after the direct shot, in which a Goal will still be counted", defValue = "1.0")
	private static double timeAfterDirectShot = 1.0;
	@Configurable(comment = "[s] DeadTime", defValue = "10.0")
	private static double deadTimeAfterOccurrence = 10.0;

	static
	{
		ConfigRegistration.registerClass("metis", DirectShotsStatsCalc.class);
	}

	private final Supplier<IKickEvent> detectedGoalKickTigers;
	private final Supplier<EPossibleGoal> possibleGoal;
	private final Supplier<Set<BotID>> currentlyTouchingBots;
	private final StatsData statsData = new StatsData();


	public DirectShotsStatsCalc(Supplier<IKickEvent> detectedGoalKickTigers, Supplier<EPossibleGoal> possibleGoal,
			Supplier<Set<BotID>> currentlyTouchingBots)
	{
		super(EAiShapesLayer.STATS_DEBUG_DIRECT_SHOTS);
		this.detectedGoalKickTigers = detectedGoalKickTigers;
		this.possibleGoal = possibleGoal;
		this.currentlyTouchingBots = currentlyTouchingBots;
	}


	@Override
	public void saveStatsToMatchStatistics(final MatchStats matchStatistics)
	{
		var totalDirectShotsSum = statsData.totalDirectShots.values().stream().mapToInt(Integer::intValue).sum();

		matchStatistics.putStatisticData(EMatchStatistics.DIRECT_SHOTS,
				new StatisticData(statsData.totalDirectShots, totalDirectShotsSum));

		matchStatistics.putStatisticData(EMatchStatistics.DIRECT_SHOTS_SUCCESS,
				new StatisticData(statsData.successful, total(statsData.successful, totalDirectShotsSum)));

		matchStatistics.putStatisticData(EMatchStatistics.DIRECT_SHOTS_BLOCKED_DEFENSE,
				new StatisticData(statsData.blockedByDefense, total(statsData.blockedByDefense, totalDirectShotsSum)));
		matchStatistics.putStatisticData(EMatchStatistics.DIRECT_SHOTS_BLOCKED_KEEPER,
				new StatisticData(statsData.blockedByKeeper, total(statsData.blockedByKeeper, totalDirectShotsSum)));
		matchStatistics.putStatisticData(EMatchStatistics.DIRECT_SHOTS_BLOCKED_BOTH,
				new StatisticData(statsData.blockedByBoth, total(statsData.blockedByBoth, totalDirectShotsSum)));
		matchStatistics.putStatisticData(EMatchStatistics.DIRECT_SHOTS_SKIRMISH,
				new StatisticData(statsData.stuckInSkirmish, total(statsData.stuckInSkirmish, totalDirectShotsSum)));
		matchStatistics.putStatisticData(EMatchStatistics.DIRECT_SHOTS_OTHER,
				new StatisticData(statsData.otherReason, total(statsData.otherReason, totalDirectShotsSum)));
	}


	private Percentage total(Map<Integer, Integer> data, int totalDirectShotsSum)
	{
		return new Percentage(data.values().stream().mapToInt(Integer::intValue).sum(), totalDirectShotsSum);
	}


	@Override
	protected DirectShotsStatsCalcWatchData getNewData(DirectShotsStatsCalcWatchData oldData)
	{
		return DirectShotsStatsCalcWatchData.fromCurrentShot(detectedGoalKickTigers.get());
	}


	@Override
	protected DirectShotsStatsCalcWatchData updateData(DirectShotsStatsCalcWatchData oldData)
	{
		var count = 0;
		for (var ballTouchingBot : oldData.ballTouchingOpponents())
		{
			drawBorderText(Vector2.fromXY(1.0, 10.0 + count++), ballTouchingBot.toString());
		}
		return oldData.update(currentlyTouchingBots.get().stream()
						.filter(botID -> botID.getTeamColor().opposite() == baseAiFrame.getTeamColor())
						.collect(Collectors.toSet()),
				baseAiFrame.getWorldFrame().getKickFitState().orElse(null));
	}


	@Override
	protected boolean hasEventHappened(DirectShotsStatsCalcWatchData data)
	{
		return possibleGoal.get() == EPossibleGoal.WE;
	}


	@Override
	protected void onEventHappened(DirectShotsStatsCalcWatchData data)
	{
		statsData.incSuccessful(data.currentShot().getKickingBot());
	}


	@Override
	protected void onEventNeverHappened(DirectShotsStatsCalcWatchData data)
	{
		if (data != null && data.maxShotSpeed() >= minimalBallVelocity)
		{
			//Shot was fast enough -> consider it a shot and not just a wrong detection

			var shooterID = data.currentShot().getKickingBot();
			var keeperID = baseAiFrame.getKeeperOpponentId();
			var ballPos = baseAiFrame.getWorldFrame().getBall().getPos();

			var isBlockedByKeeper = data.ballTouchingOpponents().contains(keeperID);
			var isBlockedByDefense = data.ballTouchingOpponents().stream().anyMatch(botID -> botID != keeperID);

			if (data.currentShot().getPosition().distanceTo(ballPos) < maxSkirmishBallTravelDistance
					&& isBlockedByDefense)
			{
				statsData.incFailedStuckInSkirmish(shooterID);
			} else if (isBlockedByKeeper && isBlockedByDefense)
			{
				statsData.incFailedBlockedByBoth(shooterID);
			} else if (isBlockedByDefense)
			{
				statsData.incFailedBlockedByDefense(shooterID);
			} else if (isBlockedByKeeper)
			{
				statsData.incFailedBlockedByKeeper(shooterID);
			} else
			{
				statsData.incFailedOtherReason(shooterID);
			}
		}
	}


	@Override
	protected boolean canEventHappen()
	{
		return detectedGoalKickTigers.get() != null;
	}


	@Override
	protected Double getAfterWatchTime()
	{
		return timeAfterDirectShot;
	}


	@Override
	protected Double getDeadTime()
	{
		return deadTimeAfterOccurrence;
	}


	private record StatsData(
			Map<Integer, Integer> totalDirectShots,
			Map<Integer, Integer> successful,
			Map<Integer, Integer> blockedByDefense,
			Map<Integer, Integer> blockedByKeeper,
			Map<Integer, Integer> blockedByBoth,
			Map<Integer, Integer> stuckInSkirmish,
			Map<Integer, Integer> otherReason
	)
	{
		public StatsData()
		{
			this(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(),
					new HashMap<>(), new HashMap<>());
		}


		public void incSuccessful(BotID botID)
		{
			totalDirectShots.compute(botID.getNumber(), (number, count) -> count != null ? count + 1 : 1);
			successful.compute(botID.getNumber(), (number, count) -> count != null ? count + 1 : 1);
		}


		public void incFailedBlockedByDefense(BotID botID)
		{
			totalDirectShots.compute(botID.getNumber(), (number, count) -> count != null ? count + 1 : 1);
			blockedByDefense.compute(botID.getNumber(), (number, count) -> count != null ? count + 1 : 1);
		}


		public void incFailedBlockedByKeeper(BotID botID)
		{
			totalDirectShots.compute(botID.getNumber(), (number, count) -> count != null ? count + 1 : 1);
			blockedByKeeper.compute(botID.getNumber(), (number, count) -> count != null ? count + 1 : 1);
		}


		public void incFailedBlockedByBoth(BotID botID)
		{
			totalDirectShots.compute(botID.getNumber(), (number, count) -> count != null ? count + 1 : 1);
			blockedByBoth.compute(botID.getNumber(), (number, count) -> count != null ? count + 1 : 1);
		}


		public void incFailedStuckInSkirmish(BotID botID)
		{
			totalDirectShots.compute(botID.getNumber(), (number, count) -> count != null ? count + 1 : 1);
			stuckInSkirmish.compute(botID.getNumber(), (number, count) -> count != null ? count + 1 : 1);
		}


		public void incFailedOtherReason(BotID botID)
		{
			totalDirectShots.compute(botID.getNumber(), (number, count) -> count != null ? count + 1 : 1);
			otherReason.compute(botID.getNumber(), (number, count) -> count != null ? count + 1 : 1);
		}
	}
}
