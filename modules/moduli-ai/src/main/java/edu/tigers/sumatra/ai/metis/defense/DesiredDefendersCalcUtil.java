package edu.tigers.sumatra.ai.metis.defense;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.wp.data.ITrackedBot;


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


	void update(final BaseAiFrame aiFrame)
	{
		this.aiFrame = aiFrame;
	}


	Set<BotID> nextBestDefenders(
			final IDefenseThreat threat,
			final List<BotID> remainingDefenders,
			final int numDefenders)
	{
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
		return desiredDefenders;
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
				.orElseGet(() -> DefenseMath.getThreatDefendingLineForCenterBack(threat.getThreatLine()));

		Map<BotID, Double> interceptionRating = new HashMap<>();
		for (BotID botID : remainingDefenders)
		{
			ITrackedBot tBot = getAiFrame().getWorldFrame().getBot(botID);
			double dist = dist2Threat(threat, protectionLine, tBot);
			interceptionRating.put(botID, dist - assignedLastFrameBonus(threat, botID));
		}
		return interceptionRating;
	}


	private double dist2Threat(final IDefenseThreat threat, final ILineSegment threadLine, final ITrackedBot tBot)
	{
		if (threadLine.getLength() > 1)
		{
			return threadLine.distanceTo(tBot.getPos());
		}
		return threat.getPos().distanceTo(tBot.getPos());
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
		for (DefenseThreatAssignment lastAssignment : getAiFrame().getPrevFrame().getTacticalField()
				.getDefenseThreatAssignments())
		{
			if (lastAssignment.getThreat().sameAs(threat))
			{
				return lastAssignment.getBotIds().contains(botID);
			}
		}
		return false;
	}


	private BaseAiFrame getAiFrame()
	{
		return aiFrame;
	}
}
