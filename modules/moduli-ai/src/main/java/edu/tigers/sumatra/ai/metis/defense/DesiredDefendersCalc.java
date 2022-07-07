/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
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
import java.util.Collections;
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
	private final DesiredDefendersCalcUtil util = new DesiredDefendersCalcUtil();

	private final Supplier<Map<EPlay, Integer>> playNumbers;
	private final Supplier<DefenseBallThreat> defenseBallThreat;
	private final Supplier<List<DefenseBotThreat>> defenseBotThreats;
	private final Supplier<Set<BotID>> crucialDefender;
	private final Supplier<Integer> numDefenderForBall;

	@Getter
	private List<DefenseThreatAssignment> defenseRawThreatAssignments;


	public DesiredDefendersCalc(
			Supplier<Map<EPlay, Set<BotID>>> desiredBotMap,
			Supplier<Map<EPlay, Integer>> playNumbers,
			Supplier<DefenseBallThreat> defenseBallThreat,
			Supplier<List<DefenseBotThreat>> defenseBotThreats,
			Supplier<Set<BotID>> crucialDefender,
			Supplier<Integer> numDefenderForBall)
	{
		super(desiredBotMap);
		this.playNumbers = playNumbers;
		this.defenseBallThreat = defenseBallThreat;
		this.defenseBotThreats = defenseBotThreats;
		this.crucialDefender = crucialDefender;
		this.numDefenderForBall = numDefenderForBall;
	}


	@Override
	public void doCalc()
	{
		util.update(getAiFrame());

		// Exclude already assigned offensive and other crucial bots (Keeper, BallPlacement, etc.)
		var remainingDefenders = new ArrayList<>(getUnassignedBots());
		// a LinkedHashSet is required here as we need the insertion order further down
		var desiredDefenders = new LinkedHashSet<BotID>();


		defenseRawThreatAssignments = createAllThreatAssignments(remainingDefenders, desiredDefenders);
		defenseRawThreatAssignments.forEach(this::drawThreatAssignment);

		remainingDefenders.sort(Comparator.comparingDouble(
				o -> getWFrame().getBot(o).getPos().distanceTo(Geometry.getGoalOur().getCenter())));

		desiredDefenders.addAll(remainingDefenders);

		Set<BotID> finalDesiredDefenders = desiredDefenders.stream()
				.limit(playNumbers.get().getOrDefault(EPlay.DEFENSIVE, 0))
				.collect(Collectors.toSet());

		addDesiredBots(EPlay.DEFENSIVE, finalDesiredDefenders);
	}


	private List<DefenseThreatAssignment> createAllThreatAssignments(List<BotID> remainingDefenders,
			Set<BotID> desiredDefenders)
	{
		var threatAssignments = new ArrayList<DefenseThreatAssignment>();

		threatAssignments.add(createBallThreatAssignment(remainingDefenders, desiredDefenders));
		defenseBotThreats.get().stream()
				.map(t -> createBotThreatAssignment(t, remainingDefenders, desiredDefenders))
				.forEach(threatAssignments::add);

		return Collections.unmodifiableList(threatAssignments);
	}


	private DefenseThreatAssignment createBallThreatAssignment(
			List<BotID> remainingDefenders,
			Set<BotID> desiredDefenders)
	{
		var bestDefenders = crucialDefender.get().stream()
				.filter(remainingDefenders::contains)
				.collect(Collectors.toSet());

		var numMissingDefender = numDefenderForBall.get() - crucialDefender.get().size();
		bestDefenders.addAll(util.nextBestDefenders(defenseBallThreat.get(), remainingDefenders, numMissingDefender));

		return createSingleThreatAssignment(defenseBallThreat.get(), remainingDefenders, desiredDefenders, bestDefenders);
	}


	private DefenseThreatAssignment createBotThreatAssignment(
			IDefenseThreat threat,
			List<BotID> remainingDefenders,
			Set<BotID> desiredDefenders)
	{
		var bestDefenders = util.nextBestDefenders(threat, remainingDefenders, 1);

		return createSingleThreatAssignment(threat, remainingDefenders, desiredDefenders, bestDefenders);
	}


	private DefenseThreatAssignment createSingleThreatAssignment(
			IDefenseThreat threat,
			List<BotID> remainingDefenders,
			Set<BotID> desiredDefenders,
			Set<BotID> bestDefenders)
	{
		remainingDefenders.removeAll(bestDefenders);
		desiredDefenders.addAll(bestDefenders);
		return new DefenseThreatAssignment(threat, bestDefenders);
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
