/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreatDefData;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreatDefStrategyData;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseBotThreatDefStrategy;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseThreatType;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.math.StatisticsMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import org.apache.commons.lang.Validate;

import java.awt.Color;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;


public class DefenseThreatReductionRater
{
	@Configurable(comment = "[%] CenterBack provide this much risk reduction (1 -> perfect blocking, 0 -> no blocking)", defValue = "0.9")
	private static double centerBackSafetyFactor = 0.9;

	@Configurable(comment = "[%] Man2Man Blocking provides this much risk reduction (1 -> perfect blocking, 0 -> no blocking)", defValue = "0.8")
	private static double m2mSafetyFactor = 0.7;

	@Configurable(comment = "[%] PenArea CenterBack provide this much risk reduction (1 -> perfect blocking, 0 -> no blocking)", defValue = "0.95")
	private static double penAreaSafetyFactor = 0.95;

	@Configurable(comment = "[m/s] Max velocity difference between defender and threat", defValue = "1.0")
	private static double maxVelDifference = 1.0;

	@Getter
	@Configurable(comment = "[s] Lookahead time to project defender position in the future", defValue = "0.3")
	private static double defenderPosLookahead = 0.3;

	@Configurable(comment = "[%] positional factor hysteresis size compared to tube radius", defValue = "0.7")
	private static double positionalFactorHysteresis = 0.7;

	@Getter
	@Configurable(comment = "[factor] An opponent pass is this much faster than our defense reacting", defValue = "3.0")
	private static double opponentPassingSpeedAdvantage = 3.0;

	@Configurable(comment = "draw shapes", defValue = "false")
	private static boolean drawShapes = false;

	static
	{
		ConfigRegistration.registerClass("metis", DefenseThreatReductionRater.class);
	}

	@Getter
	private List<IDrawableShape> shapes = List.of();


	public double calcThreatRatingWanted(List<DefenseBotThreatDefData> allThreats,
			List<DefenseBotThreat> defendedThreats, DefenseBallThreat ballThreat, int numBallDefender)
	{
		var defendersData = Stream.concat(
				DefenderData.fromWantedBallDefender(ballThreat, numBallDefender),
				defendedThreats.stream().map(dt -> DefenderData.fromWantedData(dt, allThreats))
		).toList();
		return calcThreatRating(allThreats, defendersData);
	}


	public double calcThreatRatingActual(List<DefenseBotThreatDefData> threats, List<ITrackedBot> defenders,
			List<DefenseThreatAssignment> defenseThreatAssignments)
	{
		var defendersData = Stream.concat(
				DefenderData.fromActualBallDefender(),
				defenders.stream().map(defender -> DefenderData.fromActualData(defender, defenseThreatAssignments, threats))
		).toList();
		return calcThreatRating(threats, defendersData);
	}


	private double calcThreatRating(List<DefenseBotThreatDefData> allThreats, List<DefenderData> defenders)
	{
		shapes = drawShapes ? defenders.stream().flatMap(DefenderData::toShape).toList() : List.of();
		return StatisticsMath.anyOccurs(allThreats.stream()
				.map(t -> t.threatRating() * calcThreatRatingMultiplier(t, defenders)));
	}


	private double calcThreatRatingMultiplier(DefenseBotThreatDefData threat, List<DefenderData> defenders)
	{
		double multiplier = 1.0;
		if (threat.canBeOuterDefendedWithStrategy(EDefenseBotThreatDefStrategy.CENTER_BACK))
		{
			multiplier *= calcChanceNoCenterBackWillBlock(threat, defenders);
		} else if (defenders.stream().filter(DefenderData::isPenAreaDefender)
				.anyMatch(d -> Objects.equals(d.defendedID, threat.getBotId())))
		{
			multiplier *= 1 - penAreaSafetyFactor;
		}

		if (threat.canBeOuterDefendedWithStrategy(EDefenseBotThreatDefStrategy.MAN_2_MAN_MARKER))
		{
			multiplier *= calcChanceNoMan2ManWillBlock(threat, defenders);
		}
		assert multiplier >= 0.0;
		assert multiplier <= 1.0;
		return multiplier;
	}


	private double calcChanceNoCenterBackWillBlock(DefenseBotThreatDefData threat, List<DefenderData> defenders)
	{
		return defenders.stream()
				.mapToDouble(
						def -> Math.max(0, 1 - calcChanceCenterBackWillBlock(threat, def)))
				.reduce(1.0, (a, b) -> a * b);
	}


	private double calcChanceCenterBackWillBlock(DefenseBotThreatDefData threat, DefenderData defender)
	{
		if (defender.position == null)
		{
			return 0.0;
		}
		var strategy = threat.centerBackDefStrategyData();
		var protectionLine = strategy.protectionLine();
		var threatPos = strategy.threatPos();
		var protectionPos = protectionLine.closestPointOnPath(defender.position);
		var blockDist = protectionPos.distanceTo(threatPos);

		return centerBackSafetyFactor
				* positionalFactor(defender, strategy)
				* (0.67 + 0.33 * strategy.bestDistToThreat() / Math.max(strategy.bestDistToThreat(), blockDist))
				* velocityFactor(defender.velocity, threat.bot().getVel());
	}


	private double calcChanceNoMan2ManWillBlock(DefenseBotThreatDefData threat, List<DefenderData> defenders)
	{
		return defenders.stream()
				.mapToDouble(def -> (1 - calcChanceMan2ManWillBlock(threat, def)))
				.reduce(1.0, (a, b) -> a * b);

	}


	private double calcChanceMan2ManWillBlock(DefenseBotThreatDefData threat, DefenderData defender)
	{
		if (defender.position == null)
		{
			return 0.0;
		}
		return m2mSafetyFactor
				* positionalFactor(defender, threat.man2manDefStrategyData())
				* velocityFactor(defender.velocity, threat.bot().getVel());
	}


	private double positionalFactor(DefenderData defender, DefenseBotThreatDefStrategyData strategy)
	{
		var distToProtectNow = strategy
				.protectionLine()
				.distanceTo(defender.position) - strategy.maxDistToProtectLine();
		var distToProtectFuture = strategy
				.futureProtectionLine()
				.distanceTo(defender.futurePos) - strategy.maxDistToProtectLine();

		var maxDefenceDist = Math.max(
				2 * Geometry.getBotRadius(),
				positionalFactorHysteresis * strategy.maxDistToProtectLine()
		);

		var ret = 0.5 * (1 - SumatraMath.cap(distToProtectNow / (maxDefenceDist), 0, 1))
				+ 0.5 * (1 - SumatraMath.cap(distToProtectFuture / (maxDefenceDist), 0, 1));
		Validate.isTrue(ret >= 0);
		Validate.isTrue(ret <= 1);
		return ret;
	}


	private double velocityFactor(IVector2 vel1, IVector2 vel2)
	{
		var ret = Math.max(1 - vel1.subtractNew(vel2).getLength() / maxVelDifference, 0.0);
		Validate.isTrue(ret >= 0);
		Validate.isTrue(ret <= 1);
		return ret;
	}


	private record DefenderData(IVector2 position, IVector2 futurePos, IVector2 velocity, AObjectID defendedID,
	                            boolean isPenAreaDefender)
	{
		static DefenderData fromWantedData(DefenseBotThreat defenseBotThreat, List<DefenseBotThreatDefData> threats)
		{
			var threat = threats.stream().filter(t -> t.getBotId().equals(defenseBotThreat.getBotID())).findAny()
					.orElseThrow();

			var isPenAreaDefender = defenseBotThreat.getDefendStrategy() == EDefenseBotThreatDefStrategy.CENTER_BACK
					&& !threat.canBeOuterDefendedWithStrategy(EDefenseBotThreatDefStrategy.CENTER_BACK);
			return new DefenderData(
					defenseBotThreat.getProtectionPosition().orElse(null),
					defenseBotThreat.getProtectionPosition()
							.map(p -> p.addNew(defenseBotThreat.getVel().multiplyNew(1000 * defenderPosLookahead)))
							.orElse(null),
					defenseBotThreat.getVel(),
					threat.getBotId(),
					isPenAreaDefender
			);
		}


		static Stream<DefenderData> fromWantedBallDefender(DefenseBallThreat ballThreat, int numBallDefender)
		{
			var protectionOffset = ballThreat.getThreatLine().directionVector().getNormalVector()
					.scaleTo(2 * Geometry.getBotRadius());
			var initialProtectionCenter = DefenseMath.calculateGoalDefPoint(ballThreat.getPos(),
					Geometry.getBotRadius() * numBallDefender);
			var protectionCenter = ballThreat.getProtectionLine()
					.map(line -> line.closestPointOnPath(initialProtectionCenter)).orElse(initialProtectionCenter);

			return Stream.iterate(-0.5 * numBallDefender + 0.5, x -> x + 1)
					.limit(numBallDefender)
					.map(i -> protectionCenter.addNew(protectionOffset.multiplyNew(i)))
					.map(protectionPos -> new DefenderData(
							protectionPos,
							ballThreat.getVel().multiplyNew(1000 * defenderPosLookahead).add(protectionPos),
							ballThreat.getVel(),
							ballThreat.getObjectId(),
							ballThreat.getProtectionLine().isEmpty()
					));
		}


		static DefenderData fromActualData(ITrackedBot bot, List<DefenseThreatAssignment> threatAssignments,
				List<DefenseBotThreatDefData> threats)
		{
			var threatAssignment = threatAssignments.stream()
					.filter(ta -> ta.getBotIds().contains(bot.getBotId()))
					.findAny().orElseThrow();
			var isPenAreaDefender = threatAssignment.getThreat().getType() == EDefenseThreatType.BOT_CB && threats.stream()
					.filter(t -> Objects.equals(t.getBotId(), threatAssignment.getThreat().getObjectId()))
					.anyMatch(t -> t.canBeOuterDefendedWithStrategy(EDefenseBotThreatDefStrategy.CENTER_BACK));
			return new DefenderData(
					bot.getPos(),
					bot.getPosByTime(defenderPosLookahead),
					bot.getVel(),
					threatAssignment.getThreat().getObjectId(),
					isPenAreaDefender
			);
		}


		static Stream<DefenderData> fromActualBallDefender()
		{
			return Stream.of();
		}


		Stream<IDrawableShape> toShape()
		{
			if (position == null)
				return Stream.of();
			return Stream.of(
					new DrawableArrow(
							position,
							Vector2.fromPoints(position, futurePos),
							isPenAreaDefender ? Color.RED : Color.GREEN
					),
					new DrawableAnnotation(position, defendedID.toString())
			);
		}
	}
}
