/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseThreatType;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class DefenseThreatAssignmentSeparationCalc extends ACalculator
{
	private final Supplier<Map<EPlay, Set<BotID>>> desiredBotMap;
	private final Supplier<List<DefenseThreatAssignment>> defenseRawThreatAssignments;


	@Getter
	private List<DefenseThreatAssignment> defenseActualThreatAssignments;

	@Getter
	private Set<BotID> penAreaDefenders;
	@Getter
	private Set<BotID> outerDefenders;

	@Getter
	private List<DefenseThreatAssignment> defensePenAreaThreatAssignments;
	@Getter
	private List<DefenseThreatAssignment> defenseOuterThreatAssignments;


	@Override
	protected void doCalc()
	{
		var allDefender = desiredBotMap.get().getOrDefault(EPlay.DEFENSIVE, Collections.emptySet());

		defenseActualThreatAssignments = defenseRawThreatAssignments.get().stream()
				.map(ta -> this.getThreatAssignmentWithActualDefenders(ta, allDefender))
				.flatMap(Optional::stream)
				.toList();

		buildThreatAssignmentLists();
		outerDefenders = defenseOuterThreatAssignments.stream()
				.flatMap(ta -> ta.getBotIds().stream())
				.collect(Collectors.toSet());
		penAreaDefenders = allDefender.stream()
				.filter(botID -> !outerDefenders.contains(botID))
				.collect(Collectors.toSet());
	}


	private Optional<DefenseThreatAssignment> getThreatAssignmentWithActualDefenders(DefenseThreatAssignment assignment,
			Set<BotID> allDefender)
	{
		var assignedDefenders = assignment.getBotIds().stream().filter(allDefender::contains).collect(Collectors.toSet());
		if (assignedDefenders.isEmpty())
		{
			return Optional.empty();
		}
		return Optional.of(new DefenseThreatAssignment(assignment.getThreat(), assignedDefenders));
	}


	private void buildThreatAssignmentLists()
	{
		List<DefenseThreatAssignment> penArea = new ArrayList<>();
		List<DefenseThreatAssignment> outer = new ArrayList<>();
		for (var threatAssignment : defenseActualThreatAssignments)
		{
			if (isPenAreaThreatAssignment(threatAssignment))
			{
				penArea.add(threatAssignment);
			} else
			{
				outer.add(threatAssignment);
			}
		}
		defensePenAreaThreatAssignments = Collections.unmodifiableList(penArea);
		defenseOuterThreatAssignments = Collections.unmodifiableList(outer);
	}


	private boolean isPenAreaThreatAssignment(DefenseThreatAssignment assignment)
	{

		return stayOnPenAreaInCurrentGameState(assignment.getThreat())
				|| assignment.getBotIds().stream()
				.anyMatch(botID -> defenderNeedsTransitionViaPenAreGroup(assignment, botID));
	}


	private boolean stayOnPenAreaInCurrentGameState(final IDefenseThreat threat)
	{

		if (threat.getProtectionLine().isEmpty())
		{
			// protection not possible -> penalty area is only the fallback here
			return true;
		}
		if (getAiFrame().getGameState().isStoppedGame())
		{
			return true;
		}
		return switch (threat.getType())
				{
					case BALL, BOT_M2M -> false;
					case BOT_CB -> getAiFrame().getGameState().isStandardSituationForThem();
				};
	}


	private boolean defenderNeedsTransitionViaPenAreGroup(final DefenseThreatAssignment assignment, final BotID botID)
	{
		if (assignment.getThreat().getType() == EDefenseThreatType.BOT_M2M)
		{
			return false;
		}
		var botPos = getWFrame().getTiger(botID).getPos();
		// penAreaDefender is still unmodified from last Frame.
		var wasPenAreaDefenderLastFrame = penAreaDefenders != null && penAreaDefenders.contains(botID);
		var hysteresis = wasPenAreaDefenderLastFrame ? 50.0 : -50.0;

		final ILineSegment protectionLine = assignment.getThreat().getProtectionLine()
				.orElseThrow(IllegalStateException::new);
		final double distToProtectionLine = protectionLine.distanceTo(botPos);

		double goOutOffset = DefenseConstants.getMinGoOutDistance() + (Geometry.getBotRadius() * 2) - hysteresis;

		double onPenaltyAreaOffset = Geometry.getBotRadius() * 3 + hysteresis;

		final boolean outsideOfPenArea = !Geometry.getPenaltyAreaOur()
				.withMargin(onPenaltyAreaOffset)
				.isPointInShape(botPos);

		return distToProtectionLine >= goOutOffset && !outsideOfPenArea;
	}

}
