/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.match.defense;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePenAreaPositionAssignment;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderPenAreaRole;
import lombok.RequiredArgsConstructor;

import java.util.List;


/**
 * The group containing all penArea bots
 */
@RequiredArgsConstructor
public class PenAreaGroup extends ADefenseGroup
{
	private final List<DefensePenAreaPositionAssignment> positionAssignments;


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
	}
}
