/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics.stats;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseThreatType;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;


public class BallDefenderInTimeStatsCalc extends AEventWatchingStatsCalc<BotID>
{
	@Configurable(comment = "[s] Time span after the receiver vanished in which it can still touch the ball to be counted", defValue = "0.5")
	private static double timeAfterReceiverVanished = 0.5;
	@Configurable(comment = "[s] DeadTime", defValue = "0.5")
	private static double deadTimeAfterOccurrence = 0.5;

	static
	{
		ConfigRegistration.registerClass("metis", BallDefenderInTimeStatsCalc.class);
	}

	private final Supplier<ITrackedBot> opponentPassReceiver;
	private final Supplier<List<DefenseThreatAssignment>> threatAssignments;
	private final Supplier<Set<BotID>> currentlyTouchingBots;
	private final Supplier<Double> defenseCoverage;

	private final MovingAverage ballDefenderPosDistance = new MovingAverage();
	private final MovingAverage ballDefenderVelDistance = new MovingAverage();
	private final MovingAverage ballDefenderCoverage = new MovingAverage();


	public BallDefenderInTimeStatsCalc(Supplier<ITrackedBot> opponentPassReceiver,
			Supplier<List<DefenseThreatAssignment>> threatAssignments, Supplier<Set<BotID>> currentlyTouchingBots,
			Supplier<Double> defenseCoverage)
	{
		super(EAiShapesLayer.STATS_DEBUG_BALL_DEFENDER_IN_TIME);
		this.opponentPassReceiver = opponentPassReceiver;
		this.threatAssignments = threatAssignments;
		this.currentlyTouchingBots = currentlyTouchingBots;
		this.defenseCoverage = defenseCoverage;
	}


	@Override
	protected BotID getNewData(BotID oldData)
	{
		return opponentPassReceiver.get().getBotId();
	}


	@Override
	protected BotID updateData(BotID oldData)
	{
		drawBorderText(Vector2.fromXY(1.0, 10), oldData.toString());
		return oldData;
	}


	@Override
	protected boolean hasEventHappened(BotID data)
	{
		return currentlyTouchingBots.get().contains(data);
	}


	@Override
	protected void onEventHappened(BotID data)
	{
		var botMap = baseAiFrame.getWorldFrame().getBots();
		var threatAssignment = threatAssignments.get().stream()
				.filter(ta -> ta.getThreat().getType() == EDefenseThreatType.BALL).findAny();

		threatAssignment.map(ta -> ta.getBotIds().stream()
						.map(botMap::get)
						.filter(Objects::nonNull)
						.mapToDouble(bot -> ta.getThreat().getThreatLine().distanceTo(bot.getPos()))
						.sum()
				)
				.ifPresent(ballDefenderPosDistance::add);

		var passReceiver = botMap.get(data);
		if (passReceiver != null)
		{
			threatAssignment.map(ta -> ta.getBotIds().stream()
							.map(botMap::get)
							.filter(Objects::nonNull)
							.mapToDouble(bot -> bot.getVel().distanceTo(passReceiver.getVel()))
							.sum()
					)
					.ifPresent(ballDefenderVelDistance::add);
		}

		ballDefenderCoverage.add(defenseCoverage.get());
	}


	@Override
	protected void onEventNeverHappened(BotID data)
	{
		// Ball didn't reach the receiver so no statistics can be taken
	}


	@Override
	protected boolean canEventHappen()
	{
		return opponentPassReceiver.get() != null;
	}


	@Override
	protected Double getAfterWatchTime()
	{
		return timeAfterReceiverVanished;
	}


	@Override
	protected Double getDeadTime()
	{
		return deadTimeAfterOccurrence;
	}


	@Override
	public void saveStatsToMatchStatistics(MatchStats matchStatistics)
	{
		matchStatistics.putStatisticData(EMatchStatistics.DEFENSE_COVERAGE_AT_BALL_RECEIVED,
				new StatisticData(ballDefenderCoverage.getCombinedValue()));
		matchStatistics.putStatisticData(EMatchStatistics.DEFENSE_VEL_AT_BALL_RECEIVED,
				new StatisticData(ballDefenderVelDistance.getCombinedValue()));
		matchStatistics.putStatisticData(EMatchStatistics.DEFENSE_DIST_AT_BALL_RECEIVED,
				new StatisticData(ballDefenderPosDistance.getCombinedValue()));
	}

}
