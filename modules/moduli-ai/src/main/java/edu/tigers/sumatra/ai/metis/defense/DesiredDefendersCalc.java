/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePassDisruptionAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.general.ADesiredBotCalc;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import lombok.Getter;
import org.apache.commons.lang.Validate;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Calculates desired defenders.
 */
public class DesiredDefendersCalc extends ADesiredBotCalc
{
	private final DesiredDefendersCalcUtil util = new DesiredDefendersCalcUtil();

	private final Supplier<Map<EPlay, Integer>> playNumbers;
	private final Supplier<DefenseBallThreat> ballThreat;
	private final Supplier<Optional<DefenseBallThreat>> secondaryBallThreat;
	private final Supplier<List<DefenseBotThreat>> defenseBotThreats;
	private final Supplier<ECrucialDefenderStrategy> crucialDefenderStrategy;
	private final Supplier<Integer> numDefenderForBall;
	private final Supplier<DefensePassDisruptionAssignment> defensePassDisruptionAssignment;

	@Getter
	private List<DefenseThreatAssignment> defenseRawThreatAssignments = List.of();

	@Getter
	private DefensePassDisruptionAssignment desiredPassDisruptionAssignment = null;


	public DesiredDefendersCalc(
			Supplier<Map<EPlay, Set<BotID>>> desiredBotMap,
			Supplier<Map<EPlay, Integer>> playNumbers,
			Supplier<DefenseBallThreat> ballThreat,
			Supplier<Optional<DefenseBallThreat>> secondaryBallThreat,
			Supplier<List<DefenseBotThreat>> defenseBotThreats,
			Supplier<ECrucialDefenderStrategy> crucialDefenderStrategy,
			Supplier<Integer> numDefenderForBall,
			Supplier<DefensePassDisruptionAssignment> defensePassDisruptionAssignment)
	{
		super(desiredBotMap);
		this.playNumbers = playNumbers;
		this.ballThreat = ballThreat;
		this.secondaryBallThreat = secondaryBallThreat;
		this.defenseBotThreats = defenseBotThreats;
		this.crucialDefenderStrategy = crucialDefenderStrategy;
		this.numDefenderForBall = numDefenderForBall;
		this.defensePassDisruptionAssignment = defensePassDisruptionAssignment;
	}


	@Override
	public void doCalc()
	{
		util.update(getAiFrame());

		List<DefenseThreatAssignment> allAssignments = new ArrayList<>();
		List<BotID> desiredDefenders = new ArrayList<>();

		desiredPassDisruptionAssignment = null;
		var availableDefender = new ArrayList<>(getUnassignedBots());
		for (var selector : getSelectorsOrderedByPriority(crucialDefenderStrategy.get()))
		{
			var result = selector.selectBestDefender(util, Collections.unmodifiableList(availableDefender));
			var selectedBots = result.selectedBots();
			Validate.isTrue(availableDefender.containsAll(selectedBots));
			availableDefender.removeAll(selectedBots);

			desiredDefenders.addAll(selectedBots);
			result.threatAssignment.ifPresent(allAssignments::add);
			result.disruptionAssignment.ifPresent(a -> Validate.isTrue(desiredPassDisruptionAssignment == null));
			result.disruptionAssignment.ifPresent(assignment -> desiredPassDisruptionAssignment = assignment);
		}
		desiredDefenders.addAll(availableDefender);

		defenseRawThreatAssignments = allAssignments.stream()
				.filter(assignment -> !assignment.getBotIds().isEmpty())
				.toList();
		defenseRawThreatAssignments.forEach(this::drawThreatAssignment);
		availableDefender.sort(Comparator.comparingDouble(
				o -> getWFrame().getBot(o).getPos().distanceTo(Geometry.getGoalOur().getCenter())));


		for (var assignment : defenseRawThreatAssignments)
		{
			for (var other : defenseRawThreatAssignments)
			{
				if (assignment.equals(other))
				{
					continue;
				}
				Validate.isTrue(other.getBotIds().stream().noneMatch(o -> assignment.getBotIds().contains(o)));
				Validate.isTrue(assignment.getBotIds().stream().noneMatch(a -> other.getBotIds().contains(a)));
			}
		}

		addDesiredBots(EPlay.DEFENSIVE,
				desiredDefenders.stream().distinct()
						.limit(playNumbers.get().getOrDefault(EPlay.DEFENSIVE, 0))
						.collect(Collectors.toSet())
		);
	}


	private List<IDefenderSelector> getSelectorsOrderedByPriority(ECrucialDefenderStrategy strategy)
	{
		Stream<IDefenderSelector> ballAndPassDisrupt = switch (strategy)
		{
			case ONLY_BLOCKING -> Stream.of(
					new BallThreatSelector(ballThreat.get(), numDefenderForBall.get()),
					new SecondaryBallThreatSelector(secondaryBallThreat.get(), numDefenderForBall.get())
			);
			case NO_CRUCIAL_DEFENDER, DISRUPT_PREFERRED_OVER_BLOCKING -> Stream.of(
					new PassDisruptionSelector(Optional.ofNullable(defensePassDisruptionAssignment.get())),
					new BallThreatSelector(ballThreat.get(), numDefenderForBall.get()),
					new SecondaryBallThreatSelector(secondaryBallThreat.get(), numDefenderForBall.get())
			);
			case BLOCKING_PREFERRED_OVER_DISRUPT -> Stream.of(
					new BallThreatSelector(ballThreat.get(), numDefenderForBall.get()),
					new PassDisruptionSelector(Optional.ofNullable(defensePassDisruptionAssignment.get())),
					new SecondaryBallThreatSelector(secondaryBallThreat.get(), numDefenderForBall.get())
			);
		};

		return Stream.concat(
				ballAndPassDisrupt,
				defenseBotThreats.get().stream()
						.map(BotThreatSelector::new)
		).toList();
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


	private interface IDefenderSelector
	{
		SelectorResult selectBestDefender(DesiredDefendersCalcUtil util, List<BotID> availableBots);
	}

	private record SelectorResult(Optional<DefenseThreatAssignment> threatAssignment,
	                              Optional<DefensePassDisruptionAssignment> disruptionAssignment)
	{
		static SelectorResult ofThreat(DefenseThreatAssignment assignment)
		{
			return new SelectorResult(Optional.of(assignment), Optional.empty());
		}


		static SelectorResult ofDisruption(DefensePassDisruptionAssignment disruptionAssignment)
		{
			return new SelectorResult(Optional.empty(), Optional.of(disruptionAssignment));
		}


		static SelectorResult empty()
		{
			return new SelectorResult(Optional.empty(), Optional.empty());
		}


		List<BotID> selectedBots()
		{
			return Stream.concat(
					threatAssignment.stream()
							.map(DefenseThreatAssignment::getBotIds)
							.flatMap(Set::stream),
					disruptionAssignment.stream()
							.map(DefensePassDisruptionAssignment::getDefenderId)
			).distinct().toList();
		}
	}

	private record BallThreatSelector(DefenseBallThreat threat, int numDefender) implements IDefenderSelector
	{
		@Override
		public SelectorResult selectBestDefender(DesiredDefendersCalcUtil util, List<BotID> availableBots)
		{
			return SelectorResult.ofThreat(
					new DefenseThreatAssignment(threat, util.nextBestDefenders(threat, availableBots, numDefender))
			);
		}
	}

	private record PassDisruptionSelector(Optional<DefensePassDisruptionAssignment> assignment)
			implements IDefenderSelector
	{
		@Override
		public SelectorResult selectBestDefender(DesiredDefendersCalcUtil util, List<BotID> availableBots)
		{
			return assignment.filter(assignment -> availableBots.contains(assignment.getDefenderId()))
					.map(SelectorResult::ofDisruption)
					.orElseGet(SelectorResult::empty);
		}
	}


	private record SecondaryBallThreatSelector(Optional<DefenseBallThreat> threat, int numDefender)
			implements IDefenderSelector
	{
		@Override
		public SelectorResult selectBestDefender(DesiredDefendersCalcUtil util, List<BotID> availableBots)
		{
			return threat.map(t -> new DefenseThreatAssignment(t, util.nextBestDefenders(t, availableBots, numDefender)))
					.map(SelectorResult::ofThreat)
					.orElseGet(SelectorResult::empty);
		}
	}

	private record BotThreatSelector(DefenseBotThreat threat) implements IDefenderSelector
	{
		@Override
		public SelectorResult selectBestDefender(DesiredDefendersCalcUtil util, List<BotID> availableBots)
		{
			return SelectorResult.ofThreat(
					new DefenseThreatAssignment(threat, util.nextBestDefenders(threat, availableBots, 1))
			);
		}
	}

}
