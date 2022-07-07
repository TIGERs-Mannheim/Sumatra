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
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Helper class for Desired defenders calculators.
 */
class DesiredDefendersCalcUtil
{
	@Configurable(comment = "Min threshold [mm]", defValue = "140.0")
	private static double interceptionDistanceHysteresisMin = 140.0;

	@Configurable(comment = "Min + this [mm] * botVel [m/s] is the threshold for switching to another threat", defValue = "300.0")
	private static double interceptionDistanceHysteresis = 300.0;

	static
	{
		ConfigRegistration.registerClass("metis", DesiredDefendersCalcUtil.class);
	}

	private BaseAiFrame aiFrame;
	private Map<BotID, Set<AObjectID>> lastFrameDefenderMapping;


	void update(final BaseAiFrame aiFrame)
	{
		this.aiFrame = aiFrame;
		lastFrameDefenderMapping = buildLastFrameDefenderIdToThreatMapping();
	}


	Set<BotID> nextBestDefenders(
			final IDefenseThreat threat,
			final List<BotID> remainingDefenders,
			final int numDefenders)
	{
		if (numDefenders <= 0)
		{
			return Collections.emptySet();
		}
		final Set<BotID> desiredDefenders = new HashSet<>();
		Map<BotID, Double> interceptionRatings = interceptionRatings(threat, remainingDefenders);
		while (desiredDefenders.size() < numDefenders)
		{
			if (interceptionRatings.isEmpty())
			{
				return desiredDefenders;
			}

			BotID defender = nextBestDefender(interceptionRatings);
			interceptionRatings.remove(defender);
			desiredDefenders.add(defender);
		}
		return Collections.unmodifiableSet(desiredDefenders);
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


	private BotID nextBestDefender(final Map<BotID, Double> interceptionRatings)
	{
		return interceptionRatings.entrySet().stream().min(Comparator.comparingDouble(Map.Entry::getValue))
				.orElseThrow(IllegalStateException::new).getKey();
	}


	private Map<BotID, Double> interceptionRatings(final IDefenseThreat threat,
			final List<BotID> remainingDefenders)
	{
		final ILineSegment protectionLine = threat.getProtectionLine()
				.orElseGet(() -> getThreatDefendingLineForCenterBack(threat.getThreatLine()));

		Map<BotID, Double> interceptionRating = new HashMap<>();
		for (BotID botID : remainingDefenders)
		{
			ITrackedBot tBot = getAiFrame().getWorldFrame().getBot(botID);
			double dist = dist2Threat(threat, protectionLine, tBot);
			interceptionRating.put(botID, dist - assignedLastFrameBonus(threat, botID));
		}
		return interceptionRating;
	}


	private ILineSegment getThreatDefendingLineForCenterBack(final ILineSegment threatLine)
	{
		return DefenseMath.getProtectionLine(threatLine,
				Geometry.getBotRadius() * 2,
				DefenseConstants.getMinGoOutDistance(),
				DefenseConstants.getMaxGoOutDistance());
	}


	private double dist2Threat(final IDefenseThreat threat, final ILineSegment protectionLine, final ITrackedBot tBot)
	{
		IVector2 botPos = tBot.getPosByTime(DefenseConstants.getLookaheadBotThreats(tBot.getVel().getLength()));
		if (protectionLine.getLength() > 1)
		{
			return protectionLine.distanceTo(botPos);
		}
		return threat.getPos().distanceTo(botPos);
	}


	private double assignedLastFrameBonus(final IDefenseThreat threat, final BotID botID)
	{
		if (wasAssignedLastFrame(botID, threat))
		{
			// velocity based hysteresis:
			// slow -> low threshold for changing
			return interceptionDistanceHysteresisMin
					+ interceptionDistanceHysteresis * getAiFrame().getWorldFrame().getBot(botID).getVel().getLength2();
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
}
