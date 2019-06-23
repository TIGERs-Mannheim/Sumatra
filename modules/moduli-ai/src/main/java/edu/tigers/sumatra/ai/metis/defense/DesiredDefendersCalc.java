/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import java.awt.Color;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.math.DefenseMath;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.skillsystem.skills.util.InterceptorUtil;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Calculates desired and crucial defenders
 *
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 * @author AndreR <andre@ryll.cc>
 */
public class DesiredDefendersCalc extends ACalculator
{
	private final boolean assignCrucialOnly;
	
	
	/**
	 * @param assignCrucialOnly
	 */
	public DesiredDefendersCalc(final Boolean assignCrucialOnly)
	{
		this.assignCrucialOnly = assignCrucialOnly;
	}
	
	
	@Override
	public void doCalc(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		List<ITrackedBot> remainingDefenders;
		List<DefenseThreatAssignment> threatAssignments = tacticalField.getDefenseThreatAssignments().stream()
				.filter(t -> t.isCrucial() == assignCrucialOnly)
				.collect(Collectors.toList());
		
		// a LinkedHashSet is required here as we need the insertion order further down
		Set<BotID> desiredDefenders = new LinkedHashSet<>();
		
		if (assignCrucialOnly)
		{
			// All visible bots except keeper and crucial offender
			remainingDefenders = aiFrame.getWorldFrame().getTigerBotsVisible().values().stream()
					.filter(bot -> !aiFrame.getKeeperId().equals(bot.getBotId()))
					.filter(bot -> !tacticalField.getCrucialOffender().contains(bot.getBotId()))
					.collect(Collectors.toList());
		} else
		{
			// Exclude already assigned offensive and crucial defender bots
			remainingDefenders = aiFrame.getWorldFrame().getTigerBotsVisible().values().stream()
					.filter(bot -> !tacticalField.getDesiredBotMap().values().stream().flatMap(Collection::stream)
							.collect(Collectors.toSet()).contains(bot.getBotId()))
					.filter(bot -> !tacticalField.getCrucialDefender().contains(bot.getBotId()))
					.collect(Collectors.toList());
			
			desiredDefenders.addAll(tacticalField.getCrucialDefender());
		}
		
		for (DefenseThreatAssignment threatAssignment : threatAssignments)
		{
			for (int i = 0; i < threatAssignment.getNumDefenders(); i++)
			{
				if (remainingDefenders.isEmpty())
				{
					break;
				}
				
				remainingDefenders.sort(Comparator.comparingDouble(tBot -> InterceptorUtil.fastestPointOnLine(
						DefenseMath.getThreatDefendingLineForCenterBack(threatAssignment.getThreat().getThreatLine()),
						tBot,
						tBot.getMoveConstraints()).getTime()
						+ getPreviouslyAssignedBonus(tBot.getBotId(), threatAssignment, aiFrame,
								getBall().getVel().getLength())));
				
				BotID bot = remainingDefenders.remove(0).getBotId();
				
				if (getWFrame().getTigerBotsAvailable().containsKey(bot))
				{
					desiredDefenders.add(bot);
					threatAssignment.addBotId(bot);
				}
			}
		}
		
		if (assignCrucialOnly)
		{
			tacticalField.setCrucialDefender(desiredDefenders);
		} else
		{
			int numDefenders = tacticalField.getPlayNumbers().getOrDefault(EPlay.DEFENSIVE, 0);
			
			remainingDefenders
					.sort(Comparator.comparingDouble(o -> o.getPos().distanceTo(Geometry.getGoalOur().getCenter())));
			
			desiredDefenders.addAll(remainingDefenders.stream()
					.map(ITrackedBot::getBotId)
					.collect(Collectors.toList()));
			
			desiredDefenders = desiredDefenders.stream()
					.filter(botID -> getWFrame().getTigerBotsAvailable().containsKey(botID))
					.limit(numDefenders)
					.collect(Collectors.toSet());
			
			tacticalField.addDesiredBots(EPlay.DEFENSIVE, desiredDefenders);
		}
		
		final List<IDrawableShape> defenseShapes = tacticalField.getDrawableShapes()
				.get(EAiShapesLayer.DEFENSE_BOT_THREATS);
		
		for (DefenseThreatAssignment threatAssignment : threatAssignments)
		{
			for (BotID botId : threatAssignment.getBotIds())
			{
				ILineSegment assignmentLine = Lines.segmentFromPoints(getWFrame().getBot(botId).getPos(),
						threatAssignment.getThreat().getPos());
				DrawableLine drawLine = new DrawableLine(assignmentLine, Color.MAGENTA);
				
				defenseShapes.add(drawLine);
			}
		}
	}
	
	
	/**
	 * Adds a bonus to the previous crucial bot
	 * 
	 * @param botID
	 * @param newAssignment
	 * @param aiFrame
	 * @return
	 */
	private static double getPreviouslyAssignedBonus(
			final BotID botID,
			final DefenseThreatAssignment newAssignment,
			final BaseAiFrame aiFrame,
			final double ballVelocity)
	{
		double factor = SumatraMath.relative(ballVelocity,
				DefenseConstants.getMinSwitchSlackThresholdVelocity(),
				DefenseConstants.getMaxSwitchSlackThresholdVelocity());
		double linearPart = DefenseConstants.getMaxSwitchSlackThreshold()
				- DefenseConstants.getMinSwitchSlackThreshold();
		double constantPart = DefenseConstants.getMinSwitchSlackThreshold();
		
		double switchSlackThreshold = (factor * linearPart) + constantPart;
		
		if (!aiFrame.getWorldFrame().getTigerBotsAvailable().containsKey(botID))
		{
			// give foreign bots an extra bonus
			return -switchSlackThreshold - 0.3;
		}
		
		for (DefenseThreatAssignment lastAssignment : aiFrame.getPrevFrame().getTacticalField()
				.getDefenseThreatAssignments())
		{
			if (lastAssignment.getObjectID().equals(newAssignment.getObjectID()))
			{
				
				if (lastAssignment.getBotIds().contains(botID))
				{
					return -switchSlackThreshold;
				}
				return 0;
			}
		}
		return 0;
	}
}
