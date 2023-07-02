/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePassDisruptionAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.ai.metis.general.ADesiredBotCalc;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import lombok.Getter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
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
	private final Supplier<DefensePassDisruptionAssignment> defensePassDisruptionAssignment;

	@Getter
	private List<DefenseThreatAssignment> defenseRawThreatAssignments;

	private boolean disruptorActive = false;


	public DesiredDefendersCalc(
			Supplier<Map<EPlay, Set<BotID>>> desiredBotMap,
			Supplier<Map<EPlay, Integer>> playNumbers,
			Supplier<DefenseBallThreat> defenseBallThreat,
			Supplier<List<DefenseBotThreat>> defenseBotThreats,
			Supplier<Set<BotID>> crucialDefender,
			Supplier<Integer> numDefenderForBall,
			Supplier<DefensePassDisruptionAssignment> defensePassDisruptionAssignment)
	{
		super(desiredBotMap);
		this.playNumbers = playNumbers;
		this.defenseBallThreat = defenseBallThreat;
		this.defenseBotThreats = defenseBotThreats;
		this.crucialDefender = crucialDefender;
		this.numDefenderForBall = numDefenderForBall;
		this.defensePassDisruptionAssignment = defensePassDisruptionAssignment;
	}


	@Override
	public void doCalc()
	{
		disruptorActive = isDisruptorActive();
		util.update(getAiFrame());

		var defender = new ArrayList<>(getUnassignedBots());
		List<DefenseThreatAssignment> assignments = new ArrayList<>();

		EnumMap<EDefenseThreatAssignment, List<BotID>> selectedBots = new EnumMap<>(
				EDefenseThreatAssignment.class);
		// Note the different ordering of EAssignmentType
		put(selectedBots, EDefenseThreatAssignment.PASS_DISRUPTION, getPassDisruptorIDs(defender), defender);
		put(selectedBots, EDefenseThreatAssignment.BLOCKING_BALL, createBallThreatAssignment(defender, assignments),
				defender);
		put(selectedBots, EDefenseThreatAssignment.BLOCKING_BOT, createBotThreatAssignments(defender, assignments),
				defender);

		defenseRawThreatAssignments = Collections.unmodifiableList(assignments);
		defenseRawThreatAssignments.forEach(this::drawThreatAssignment);
		defender.sort(Comparator.comparingDouble(
				o -> getWFrame().getBot(o).getPos().distanceTo(Geometry.getGoalOur().getCenter())));


		List<BotID> desiredDefenders = new ArrayList<>();
		// Note the different ordering of EAssignmentType
		desiredDefenders.addAll(selectedBots.get(EDefenseThreatAssignment.BLOCKING_BALL));
		desiredDefenders.addAll(selectedBots.get(EDefenseThreatAssignment.PASS_DISRUPTION));
		desiredDefenders.addAll(selectedBots.get(EDefenseThreatAssignment.BLOCKING_BOT));
		desiredDefenders.addAll(defender);

		addDesiredBots(EPlay.DEFENSIVE,
				desiredDefenders.stream().distinct()
						.limit(playNumbers.get().getOrDefault(EPlay.DEFENSIVE, 0))
						.collect(Collectors.toSet())
		);
	}


	private void put(EnumMap<EDefenseThreatAssignment, List<BotID>> selectedBotsPerAssignmentType,
			EDefenseThreatAssignment type,
			List<BotID> selectedDefenders, List<BotID> availableBots)
	{
		selectedBotsPerAssignmentType.put(type, selectedDefenders);
		availableBots.removeAll(selectedDefenders);
	}


	private boolean isDisruptorActive()
	{
		return defensePassDisruptionAssignment.get() != null
				&& getUnassignedBots().contains(defensePassDisruptionAssignment.get().getDefenderId());
	}


	public DefensePassDisruptionAssignment getDefensePassDisruptionAssignment()
	{
		return disruptorActive ? defensePassDisruptionAssignment.get() : null;
	}


	private List<BotID> getPassDisruptorIDs(List<BotID> availableBots)
	{
		if (!disruptorActive)
		{
			return List.of();
		}
		availableBots.remove(defensePassDisruptionAssignment.get().getDefenderId());
		return List.of(defensePassDisruptionAssignment.get().getDefenderId());
	}


	private List<BotID> createBallThreatAssignment(List<BotID> availableBots, List<DefenseThreatAssignment> assignments)
	{
		var bestDefenders = crucialDefender.get().stream()
				.filter(availableBots::contains)
				.collect(Collectors.toSet());

		var numMissingDefender = numDefenderForBall.get() - crucialDefender.get().size();
		bestDefenders.addAll(util.nextBestDefenders(defenseBallThreat.get(), availableBots, numMissingDefender));
		assignments.add(createSingleThreatAssignment(defenseBallThreat.get(), availableBots, bestDefenders));
		return bestDefenders.stream().toList();
	}


	private List<BotID> createBotThreatAssignments(
			List<BotID> availableBots,
			List<DefenseThreatAssignment> assignments)
	{
		var botAssignments = defenseBotThreats.get().stream()
				.map(t -> createSingleBotThreatAssignment(t, availableBots))
				.toList();
		assignments.addAll(botAssignments);
		return botAssignments.stream().flatMap(a -> a.getBotIds().stream()).toList();
	}


	private DefenseThreatAssignment createSingleBotThreatAssignment(
			IDefenseThreat threat,
			List<BotID> remainingDefenders)
	{
		var bestDefenders = util.nextBestDefenders(threat, remainingDefenders, 1);

		return createSingleThreatAssignment(threat, remainingDefenders, bestDefenders);
	}


	private DefenseThreatAssignment createSingleThreatAssignment(
			IDefenseThreat threat,
			List<BotID> remainingDefenders,
			Set<BotID> bestDefenders)
	{
		remainingDefenders.removeAll(bestDefenders);
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
