/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreatDefData;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseBotThreatDefStrategy;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseThreatType;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;


public class DefenseThreatReductionRater
{
	@Configurable(comment = "[%] The CenterBacks BotRadius is considered this much smaller, where it can safely block", defValue = "0.9")
	private static double centerBackSafetyFactor = 0.9;

	@Configurable(comment = "[%] Man2Man Blocking provides this much risk reduction (1 -> perfect blocking, 0 -> no blocking)", defValue = "0.9")
	private static double m2mSafetyFactor = 0.9;

	@Configurable(comment = "[%] PenArea CenterBack provide this much risk reduction", defValue = "0.95")
	private static double penAreaSafetyFactor = 0.95;

	@Configurable(comment = "[factor] An opponent pass is this much faster than our defense reacting", defValue = "10.0")
	private static double opponentPassingSpeedAdvantage = 10.0;

	@Configurable(comment = "[m/s] Max velocity difference between defender and threat", defValue = "1.0")
	private static double maxVelDifference = 1.0;

	static
	{
		ConfigRegistration.registerClass("metis", DefenseThreatReductionRater.class);
	}

	public double combineThreatRatings(final List<Double> threatRatings)
	{
		return combineThreatRatings(threatRatings.stream());
	}


	public double combineThreatRatings(final Stream<Double> threatRatings)
	{
		// Combine threat ratings via complementary probability
		// pAll = 1 - (1-p1) * (1-p2) * ...
		// pAll = 1 - exp(log((1-p1) * (1-p2) * ...))
		// pAll = 1 - exp(log(1-p1) + log(1-p2) + ...)
		return 1.0 - Math.exp(threatRatings.mapToDouble(t -> 1.0 - t).map(Math::log).sum());
	}


	public double calcThreatRatingWanted(final List<DefenseBotThreatDefData> allThreats,
			final List<DefenseBotThreat> defendedThreats, final IVector2 ballPos)
	{
		var defendersData = defendedThreats.stream()
				.map(dt -> DefenderData.fromWantedData(dt, allThreats))
				.toList();
		return calcThreatRating(allThreats, defendersData, ballPos);
	}


	public double calcThreatRatingActual(final List<DefenseBotThreatDefData> threats,
			final List<ITrackedBot> defenders,
			final List<DefenseThreatAssignment> defenseThreatAssignments, final IVector2 ballPos)
	{
		var defendersData = defenders.stream()
				.map(bot -> DefenderData.fromActualData(bot, defenseThreatAssignments, threats))
				.toList();
		return calcThreatRating(threats, defendersData, ballPos);
	}


	private double calcThreatRating(final List<DefenseBotThreatDefData> allThreats,
			final List<DefenderData> defenders, final IVector2 ballPos)
	{
		return combineThreatRatings(allThreats.stream()
				.map(t -> t.threatRating() * calcThreatRatingMultiplier(t, defenders, ballPos)));
	}


	private double calcThreatRatingMultiplier(final DefenseBotThreatDefData threat,
			final List<DefenderData> defenders,
			final IVector2 ballPos)
	{
		double multiplier = 1.0;
		if (threat.canBeOuterDefendedWithStrategy(EDefenseBotThreatDefStrategy.CENTER_BACK))
		{
			multiplier *= calcChanceNoCenterBackWillBlock(threat, defenders, ballPos);
		} else if (defenders.stream().filter(DefenderData::isPenAreaDefender)
				.anyMatch(d -> Objects.equals(d.defendedID, threat.getBotId())))
		{
			multiplier *= 1 - penAreaSafetyFactor;
		}

		if (threat.canBeOuterDefendedWithStrategy(EDefenseBotThreatDefStrategy.MAN_2_MAN_MARKER))
		{
			multiplier *= calcChanceNoMan2ManWillBlock(threat, defenders, ballPos);
		}
		assert multiplier >= 0.0;
		assert multiplier <= 1.0;
		return multiplier;
	}


	private double calcChanceNoCenterBackWillBlock(final DefenseBotThreatDefData threat,
			final List<DefenderData> defenders, final IVector2 ballPos)
	{
		final var threatPos = threat.centerBackDefStrategyData().threatPos();
		final var leftPostAngle = Vector2.fromPoints(threatPos, Geometry.getGoalOur().getLeftPost()).getAngle();
		final var rightPostAngle = Vector2.fromPoints(threatPos, Geometry.getGoalOur().getRightPost()).getAngle();
		final var wholeGoal = AngleMath.diffAbs(leftPostAngle, rightPostAngle);
		final var bestBlockDist = Geometry.getBotRadius() * centerBackSafetyFactor / AngleMath.tan(wholeGoal * 0.5);

		final var maxTravelDist = calcMaxTravelDistFactor(threatPos, ballPos);

		return defenders.stream()
				.mapToDouble(
						def -> Math.max(0, 1 - calcChanceCenterBackWillBlock(threat, def, bestBlockDist, maxTravelDist)))
				.reduce(1.0, (a, b) -> a * b);
	}


	private double calcChanceCenterBackWillBlock(final DefenseBotThreatDefData threat,
			final DefenderData defender,
			final double bestBlockDist, final double maxTravelDist)
	{
		if (defender.position == null)
		{
			return 0.0;
		}
		final var protectionLine = threat.centerBackDefStrategyData().protectionLine();
		final var threatPos = threat.centerBackDefStrategyData().threatPos();
		return bestBlockDist / Math.max(bestBlockDist,
				protectionLine.closestPointOnPath(defender.position).distanceTo(threatPos))
				* calcVelocityDifference(defender.velocity, threat.bot().getVel())
				* Math.max(0, 1 - protectionLine.distanceTo(defender.position) / maxTravelDist);
	}


	private double calcChanceNoMan2ManWillBlock(final DefenseBotThreatDefData threat,
			final List<DefenderData> defenders, final IVector2 ballPos)
	{
		final var maxTravelDist = calcMaxTravelDistFactor(threat.man2manDefStrategyData().threatPos(), ballPos);

		return defenders.stream()
				.mapToDouble(def -> (1 - calcChanceMan2ManWillBlock(threat, def, maxTravelDist)))
				.reduce(1.0, (a, b) -> a * b);

	}


	private double calcChanceMan2ManWillBlock(final DefenseBotThreatDefData threat, final DefenderData defender,
			final double maxTravelDist)
	{
		if (defender.position == null)
		{
			return 0.0;
		}
		final var protectionLine = threat.man2manDefStrategyData().protectionLine();
		return m2mSafetyFactor
				* calcVelocityDifference(defender.velocity, threat.bot().getVel())
				* Math.max(0, 1 - protectionLine.distanceTo(defender.position) / maxTravelDist);
	}


	private double calcMaxTravelDistFactor(final IVector2 threatPos, final IVector2 ballPos)
	{
		return threatPos.distanceTo(ballPos) / opponentPassingSpeedAdvantage;
	}


	private double calcVelocityDifference(final IVector2 vel1, final IVector2 vel2)
	{
		return SumatraMath.cap(1 - vel1.subtractNew(vel2).getLength() / maxVelDifference, 0.0, 1.0);
	}


	private record DefenderData(IVector2 position, IVector2 velocity, AObjectID defendedID, boolean isPenAreaDefender)
	{
		static DefenderData fromWantedData(final DefenseBotThreat defenseBotThreat,
				final List<DefenseBotThreatDefData> threats)
		{
			var threat = threats.stream().filter(t -> t.getBotId().equals(defenseBotThreat.getBotID())).findAny()
					.orElseThrow();

			var isPenAreaDefender = defenseBotThreat.getDefendStrategy() == EDefenseBotThreatDefStrategy.CENTER_BACK
					&& !threat.canBeOuterDefendedWithStrategy(EDefenseBotThreatDefStrategy.CENTER_BACK);
			return new DefenderData(defenseBotThreat.getProtectionPosition().orElse(null),
					defenseBotThreat.getBot().getVel(), threat.getBotId(), isPenAreaDefender);
		}


		static DefenderData fromActualData(final ITrackedBot bot, final List<DefenseThreatAssignment> threatAssignments,
				final List<DefenseBotThreatDefData> threats)
		{
			var threatAssignment = threatAssignments.stream()
					.filter(ta -> ta.getBotIds().contains(bot.getBotId()))
					.findAny().orElseThrow();
			var isPenAreaDefender = threatAssignment.getThreat().getType() == EDefenseThreatType.BOT_CB && threats.stream()
					.filter(t -> Objects.equals(t.getBotId(), threatAssignment.getThreat().getObjectId()))
					.anyMatch(t -> t.canBeOuterDefendedWithStrategy(EDefenseBotThreatDefStrategy.CENTER_BACK));
			return new DefenderData(bot.getPos(), bot.getVel(), threatAssignment.getThreat().getObjectId(),
					isPenAreaDefender);
		}
	}
}
