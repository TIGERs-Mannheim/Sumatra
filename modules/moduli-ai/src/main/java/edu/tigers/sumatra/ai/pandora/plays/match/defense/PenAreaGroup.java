/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.match.defense;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.ballresponsibility.EBallResponsibility;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePenAreaPositionAssignment;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderPenAreaRole;
import edu.tigers.sumatra.geometry.Geometry;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;


/**
 * The group containing all penArea bots
 */
@RequiredArgsConstructor
public class PenAreaGroup extends ADefenseGroup
{
	private final List<DefensePenAreaPositionAssignment> positionAssignments;
	private AthenaAiFrame aiFrame;


	@Override
	public void assignRoles()
	{
		getRoles().stream()
				.filter(sdr -> sdr.getOriginalRole().getType() != ERole.DEFENDER_PEN_AREA)
				.forEach(sdr -> sdr.setNewRole(new DefenderPenAreaRole()));
	}


	@Override
	public void updateRoles(final AthenaAiFrame aiFrame)
	{
		super.updateRoles(aiFrame);
		this.aiFrame = aiFrame;

		var penAreaBoundary = aiFrame.getTacticalField().getDefensePenAreaBoundaryForPenAreaGroup();

		for (var positionAssignment : positionAssignments)
		{
			var newRole = getRoles().stream()
					.filter(role -> role.getOriginalRole().getBotID().equals(positionAssignment.botID()))
					.map(SwitchableDefenderRole::getNewRole)
					.filter(role -> role.getClass() == DefenderPenAreaRole.class)
					.map(DefenderPenAreaRole.class::cast)
					.findAny().orElseThrow();
			newRole.setDestination(positionAssignment.movementDestination());
			newRole.setPenAreaBoundary(penAreaBoundary);
		}
		assignActiveKicker();
	}


	private void assignActiveKicker()
	{
		if (!defendersAreAllowedToKick())
		{
			penAreaDefendersStream().forEach(r -> r.setAllowedToKickBall(false));
			return;
		}
		boolean oneDefenderIsAllowedToKickTheBall = penAreaDefendersStream()
				.anyMatch(DefenderPenAreaRole::isAllowedToKickBall);

		if (!oneDefenderIsAllowedToKickTheBall)
		{
			penAreaDefendersStream()
					.min(Comparator.comparingDouble(this::distanceToBall))
					.ifPresent(r -> r.setAllowedToKickBall(true));
		}
	}


	private boolean defendersAreAllowedToKick()
	{
		return aiFrame.getGameState().isRunning()
				&& defenseResponsibleForBall()
				&& notTooCloseToPenArea();
	}


	private boolean defenseResponsibleForBall()
	{
		return aiFrame.getTacticalField().getBallResponsibility() == EBallResponsibility.DEFENSE;
	}


	private boolean notTooCloseToPenArea()
	{
		return !Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius() * 2)
				.isPointInShape(aiFrame.getWorldFrame().getBall().getPos());
	}


	private double distanceToBall(final ARole role)
	{
		return role.getBot().getPos().distanceTo(aiFrame.getWorldFrame().getBall().getPos());
	}


	private Stream<DefenderPenAreaRole> penAreaDefendersStream()
	{
		return getRoles().stream().map(SwitchableDefenderRole::getNewRole)
				.map(DefenderPenAreaRole.class::cast);
	}

}
