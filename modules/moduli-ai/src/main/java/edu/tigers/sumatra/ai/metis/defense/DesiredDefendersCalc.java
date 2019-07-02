/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.ai.metis.general.ADesiredBotCalc;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;


/**
 * Calculates desired defenders.
 */
public class DesiredDefendersCalc extends ADesiredBotCalc
{
	private static final int NUM_BOTS_PER_BALL_TO_BOT_THREAT = 1;
	private static final int NUM_BOTS_PER_BOT_THREAT = 1;
	private final DesiredDefendersCalcUtil util = new DesiredDefendersCalcUtil();


	public DesiredDefendersCalc()
	{
		super(EPlay.DEFENSIVE);
	}


	@Override
	public void doCalc()
	{
		util.update(getAiFrame());

		// Exclude already assigned offensive and crucial defender bots
		List<BotID> remainingDefenders = new ArrayList<>(getUnassignedBots());

		final Set<BotID> desiredBallDefenders = getDesiredBallDefenders(remainingDefenders);
		remainingDefenders.removeAll(desiredBallDefenders);

		// a LinkedHashSet is required here as we need the insertion order further down
		final Set<BotID> desiredDefenders = new LinkedHashSet<>(desiredBallDefenders);

		List<DefenseThreatAssignment> defenseThreatAssignments = new ArrayList<>();
		defenseThreatAssignments.add(new DefenseThreatAssignment(
				getNewTacticalField().getDefenseBallThreat(),
				desiredBallDefenders));

		for (IDefenseThreat threat : getNewTacticalField().getDefenseBallToBotThreats())
		{
			final Set<BotID> bestDefenders = util.nextBestDefenders(threat, remainingDefenders,
					NUM_BOTS_PER_BALL_TO_BOT_THREAT);
			remainingDefenders.removeAll(bestDefenders);
			desiredDefenders.addAll(bestDefenders);
			defenseThreatAssignments.add(new DefenseThreatAssignment(threat, bestDefenders));
		}

		for (IDefenseThreat threat : getNewTacticalField().getDefenseBotThreats())
		{
			final Set<BotID> bestDefenders = util.nextBestDefenders(threat, remainingDefenders, NUM_BOTS_PER_BOT_THREAT);
			remainingDefenders.removeAll(bestDefenders);
			desiredDefenders.addAll(bestDefenders);
			defenseThreatAssignments.add(new DefenseThreatAssignment(threat, bestDefenders));
		}

		remainingDefenders.sort(Comparator.comparingDouble(
				o -> getWFrame().getBot(o).getPos().distanceTo(Geometry.getGoalOur().getCenter())));

		desiredDefenders.addAll(remainingDefenders);

		Set<BotID> finalDesiredDefenders = desiredDefenders.stream()
				.limit(getNewTacticalField().getPlayNumbers().getOrDefault(EPlay.DEFENSIVE, 0))
				.collect(Collectors.toSet());

		addDesiredBots(finalDesiredDefenders);

		getNewTacticalField().setDefenseThreatAssignments(defenseThreatAssignments);
		getNewTacticalField().getDefenseThreatAssignments().forEach(this::drawThreatAssignment);
	}


	private Set<BotID> getDesiredBallDefenders(final List<BotID> remainingDefenders)
	{
		if (getNewTacticalField().getCrucialDefender().isEmpty())
		{
			return util.nextBestDefenders(
					getNewTacticalField().getDefenseBallThreat(),
					remainingDefenders,
					getNewTacticalField().getNumDefenderForBall());
		}
		return getNewTacticalField().getCrucialDefender();
	}


	private void drawThreatAssignment(DefenseThreatAssignment threatAssignment)
	{
		final List<IDrawableShape> shapes = getNewTacticalField().getDrawableShapes()
				.get(EAiShapesLayer.DEFENSE_THREAT_ASSIGNMENT);

		for (BotID botId : threatAssignment.getBotIds())
		{
			ILineSegment assignmentLine = Lines.segmentFromPoints(
					getWFrame().getBot(botId).getPos(),
					threatAssignment.getThreat().getPos());
			shapes.add(new DrawableLine(assignmentLine, Color.MAGENTA));
		}
	}
}
