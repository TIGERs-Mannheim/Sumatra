/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallToBotThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreat;
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
import lombok.Getter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Calculates desired defenders.
 */
public class DesiredDefendersCalc extends ADesiredBotCalc
{
	private static final int NUM_BOTS_PER_BALL_TO_BOT_THREAT = 1;
	private static final int NUM_BOTS_PER_BOT_THREAT = 1;
	private final DesiredDefendersCalcUtil util = new DesiredDefendersCalcUtil();

	private final Supplier<Map<EPlay, Integer>> playNumbers;
	private final Supplier<DefenseBallThreat> defenseBallThreat;
	private final Supplier<List<DefenseBotThreat>> defenseBotThreats;
	private final Supplier<List<DefenseBallToBotThreat>> defenseBallToBotThreats;
	private final Supplier<Set<BotID>> crucialDefender;
	private final Supplier<Integer> numDefenderForBall;

	@Getter
	private List<DefenseThreatAssignment> defenseThreatAssignments;


	public DesiredDefendersCalc(
			Supplier<Map<EPlay, Set<BotID>>> desiredBotMap,
			Supplier<Map<EPlay, Integer>> playNumbers,
			Supplier<DefenseBallThreat> defenseBallThreat,
			Supplier<List<DefenseBotThreat>> defenseBotThreats,
			Supplier<List<DefenseBallToBotThreat>> defenseBallToBotThreats,
			Supplier<Set<BotID>> crucialDefender, Supplier<Integer> numDefenderForBall)
	{
		super(desiredBotMap);
		this.playNumbers = playNumbers;
		this.defenseBallThreat = defenseBallThreat;
		this.defenseBotThreats = defenseBotThreats;
		this.defenseBallToBotThreats = defenseBallToBotThreats;
		this.crucialDefender = crucialDefender;
		this.numDefenderForBall = numDefenderForBall;
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

		defenseThreatAssignments = new ArrayList<>();
		defenseThreatAssignments.add(new DefenseThreatAssignment(
				defenseBallThreat.get(),
				desiredBallDefenders));

		for (IDefenseThreat botThreat : defenseBotThreats.get())
		{
			IDefenseThreat threat = defenseBallToBotThreats.get().stream()
					.filter(ballToBotThreat -> ballToBotThreat.getObjectId() == botThreat.getObjectId())
					.map(IDefenseThreat.class::cast)
					.findAny().orElse(botThreat);
			int numBotsPerThreat = (threat == botThreat) ? NUM_BOTS_PER_BOT_THREAT : NUM_BOTS_PER_BALL_TO_BOT_THREAT;

			final Set<BotID> bestDefenders = util.nextBestDefenders(threat, remainingDefenders, numBotsPerThreat);
			remainingDefenders.removeAll(bestDefenders);
			desiredDefenders.addAll(bestDefenders);
			defenseThreatAssignments.add(new DefenseThreatAssignment(threat, bestDefenders));
		}

		remainingDefenders.sort(Comparator.comparingDouble(
				o -> getWFrame().getBot(o).getPos().distanceTo(Geometry.getGoalOur().getCenter())));

		desiredDefenders.addAll(remainingDefenders);

		Set<BotID> finalDesiredDefenders = desiredDefenders.stream()
				.limit(playNumbers.get().getOrDefault(EPlay.DEFENSIVE, 0))
				.collect(Collectors.toSet());

		addDesiredBots(EPlay.DEFENSIVE, finalDesiredDefenders);

		defenseThreatAssignments.forEach(this::drawThreatAssignment);
	}


	private Set<BotID> getDesiredBallDefenders(final List<BotID> remainingDefenders)
	{
		if (crucialDefender.get().isEmpty())
		{
			return util.nextBestDefenders(
					defenseBallThreat.get(),
					remainingDefenders,
					numDefenderForBall.get());
		}
		return crucialDefender.get().stream().filter(remainingDefenders::contains).collect(Collectors.toSet());
	}


	private void drawThreatAssignment(DefenseThreatAssignment threatAssignment)
	{
		final List<IDrawableShape> shapes = getShapes(EAiShapesLayer.DEFENSE_THREAT_ASSIGNMENT);

		for (BotID botId : threatAssignment.getBotIds())
		{
			ILineSegment assignmentLine = Lines.segmentFromPoints(
					getWFrame().getBot(botId).getPos(),
					threatAssignment.getThreat().getPos());
			shapes.add(new DrawableLine(assignmentLine, Color.MAGENTA));
		}
	}
}
