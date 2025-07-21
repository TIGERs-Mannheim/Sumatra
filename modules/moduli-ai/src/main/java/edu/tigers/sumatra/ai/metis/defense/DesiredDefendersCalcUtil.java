/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Helper class for Desired defenders calculators.
 */
public class DesiredDefendersCalcUtil
{
	@Configurable(comment = " [mm] Min threshold - multiplied with num bots requested", defValue = "70.0")
	private static double interceptionDistanceHysteresisMinPerBot = 70.0;

	@Configurable(comment = "Min + this [mm] * botVel [m/s] is the threshold for switching to another threat", defValue = "300.0")
	private static double interceptionDistanceHysteresis = 300.0;

	@Configurable(comment = "[%] maximum portion of the interception rating influenced by the velocity", defValue = "0.67")
	private static double interceptionRatingMaxVelocityPortion = 0.67;

	@Configurable(comment = "[mm] Distance to start reducing threat velocity impact", defValue = "2000.0")
	private static double threatVelocityImpactReductionDistance = 2000.0;

	static
	{
		ConfigRegistration.registerClass("metis", DesiredDefendersCalcUtil.class);
	}

	private BaseAiFrame aiFrame;
	private Map<BotID, Set<AObjectID>> lastFrameDefenderMapping;


	public void update(final BaseAiFrame aiFrame)
	{
		this.aiFrame = aiFrame;
		lastFrameDefenderMapping = buildLastFrameDefenderIdToThreatMapping();
	}


	public Set<BotID> nextBestDefenders(
			final IDefenseThreat threat,
			final List<BotID> remainingDefenders,
			final int numDefenders)
	{
		if (numDefenders <= 0)
		{
			return Collections.emptySet();
		}
		return interceptionRatings(threat, remainingDefenders, numDefenders).stream()
				.sorted(Comparator.comparingDouble(InterceptionRating::adjustedDistance))
				.limit(numDefenders)
				.map(InterceptionRating::defender)
				.collect(Collectors.toUnmodifiableSet());
	}


	private Map<BotID, Set<AObjectID>> buildLastFrameDefenderIdToThreatMapping()
	{
		var prev = getAiFrame().getPrevFrame();
		if (prev == null)
		{
			return Collections.emptyMap();
		}
		var tacticalField = prev.getTacticalField();
		Map<BotID, Set<AObjectID>> mapping = new HashMap<>();

		tacticalField.getDefenseOuterThreatAssignments()
				.forEach(assignment -> assignment.getBotIds()
						.forEach(botID -> mapping.put(botID, Set.of(assignment.getThreat().getObjectId()))));

		tacticalField.getDefensePenAreaPositionAssignments()
				.forEach(assignment -> mapping.put(assignment.botID(), assignment.defendedThreats()
						.stream().map(IDefenseThreat::getObjectId).collect(Collectors.toSet())));

		return Collections.unmodifiableMap(mapping);
	}


	private List<InterceptionRating> interceptionRatings(IDefenseThreat threat, List<BotID> remainingDefenders,
			int numDefenders)
	{
		final ILineSegment protectionLine = threat.getProtectionLine()
				.orElseGet(() -> getThreatDefendingLineForCenterBack(threat.getThreatLine()));

		return remainingDefenders.stream()
				.map(botID -> getAiFrame().getWorldFrame().getBot(botID))
				.filter(Objects::nonNull)
				.map(bot -> rateInterceptability(threat, protectionLine, bot, numDefenders))
				.toList();
	}


	private ILineSegment getThreatDefendingLineForCenterBack(final ILineSegment threatLine)
	{
		return DefenseMath.getProtectionLine(threatLine,
				Geometry.getBotRadius() * 2,
				Math.min(Geometry.getBotRadius() * 2, DefenseConstants.getMinGoOutDistance()),
				DefenseConstants.getMaxGoOutDistance());
	}


	private ILineSegment getDefenderToThreatLine(IDefenseThreat threat, ILineSegment protectionLine, IVector2 botPos)
	{
		if (protectionLine.getLength() > 1)
		{
			return Lines.segmentFromPoints(botPos, protectionLine.closestPointOnPath(botPos));
		} else
		{
			return Lines.segmentFromPoints(botPos, threat.getPos());
		}
	}


	private double rawDistanceFromDefenderToThreatLine(ILineSegment defenderToThreatLine)
	{
		var intersectionsWithPenaltyArea = Geometry.getPenaltyAreaOur().intersectPerimeterPath(defenderToThreatLine);
		if (intersectionsWithPenaltyArea.size() == 2)
		{
			// Distances within our PenaltyArea count twice
			return defenderToThreatLine.getLength() + intersectionsWithPenaltyArea.getFirst()
					.distanceTo(intersectionsWithPenaltyArea.getLast());
		}
		return defenderToThreatLine.getLength();
	}


	private IVector2 futureMovementDirectionRelevantForDefense(ILineSegment defenderToThreat, IVector2 threatVelocity)
	{
		var threatVelocityImpact = 1 - SumatraMath.cap(
				(defenderToThreat.getLength() - threatVelocityImpactReductionDistance)
						/ threatVelocityImpactReductionDistance, 0, 1);

		return threatVelocity.normalizeNew().multiply(threatVelocityImpact)
				.add(defenderToThreat.directionVector().normalizeNew().multiplyNew(1 - threatVelocityImpact))
				.normalizeNew();

	}


	private InterceptionRating rateInterceptability(IDefenseThreat threat, ILineSegment protectionLine, ITrackedBot bot,
			int numDefenders)
	{
		var defenderToThreatLine = getDefenderToThreatLine(threat, protectionLine, bot.getPos());
		var rawDistance = rawDistanceFromDefenderToThreatLine(defenderToThreatLine);

		double adjusted = rawDistance - assignedLastFrameBonus(threat, bot, numDefenders);

		var botMovementDir = bot.getVel().normalizeNew();
		var futureMovementDir = futureMovementDirectionRelevantForDefense(defenderToThreatLine, threat.getVel());
		// 1 -> same direction, 0 -> opposite direction
		var directionFactor = -0.5 * (botMovementDir.scalarProduct(futureMovementDir) + 1);
		// Square it to penalize opposite same direction even more
		directionFactor = directionFactor * directionFactor;
		// Invert it same direction is good -> 0, opposite is bad -> 1
		directionFactor = 1 - directionFactor;

		var velocitiesAdded = bot.getVel().getLength() + futureMovementDir.getLength();
		// If both are fast or one extremely fast -> up to 2/3 of distance are influenced by velocity direction
		// If both are slow velocity direction will take close to no influence and overall distance is more important
		var velocityPortion = interceptionRatingMaxVelocityPortion * SumatraMath.relative(velocitiesAdded, 0, 5);

		adjusted = adjusted * directionFactor * velocityPortion + adjusted * (1 - velocityPortion);

		return new InterceptionRating(
				threat,
				bot.getBotId(),
				adjusted,
				rawDistance
		);
	}


	private double assignedLastFrameBonus(IDefenseThreat threat, ITrackedBot bot, int numDefenders)
	{
		if (wasAssignedLastFrame(bot.getBotId(), threat))
		{
			// velocity based hysteresis:
			// slow -> low threshold for changing
			return interceptionDistanceHysteresisMinPerBot * numDefenders
					+ interceptionDistanceHysteresis * bot.getVel().getLength2();
		}
		return 0;
	}


	private boolean wasAssignedLastFrame(final BotID botID, final IDefenseThreat threat)
	{
		return lastFrameDefenderMapping.getOrDefault(botID, Collections.emptySet()).contains(threat.getObjectId());
	}


	private BaseAiFrame getAiFrame()
	{
		return aiFrame;
	}


	private record InterceptionRating(
			IDefenseThreat threat,
			BotID defender,
			double adjustedDistance,
			double originalDistance
	)
	{
	}
}
