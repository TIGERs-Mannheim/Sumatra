/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker.states;


import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.pathfinder.EObstacleAvoidanceMode;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;

import java.util.Optional;


public class KickState extends AAttackerRoleState<TouchKickSkill>
{
	public KickState(AttackerRole role)
	{
		super(TouchKickSkill::new, role, EAttackerState.KICK);
	}


	@Override
	protected boolean isNecessaryDataAvailable()
	{
		return getRole().getAction().getKick() != null;
	}


	@Override
	protected void doStandardUpdate()
	{
		skill.getMoveCon().setObstacleAvoidanceMode(EObstacleAvoidanceMode.AGGRESSIVE);
		skill.setTarget(
				Optional.of(getRole().getAction().getKick().getTarget()).orElse(Geometry.getGoalTheir().getCenter()));
		skill.setPassRange(getRole().getAction().getKick().getAimingTolerance());
		skill.setDesiredKickParams(getRole().getAction().getKick().getKickParams());
		skill.setTurnAdvise(getRole().getTacticalField().getBallHandlingAdvise().getTurnAdvise());
		skill.setMoveAdvise(getRole().getTacticalField().getBallHandlingAdvise().getMoveAdvise());
		skill.setForcePushDuringKick(getRole().getAction().isForcePushDuringKick());
		skill.setMarginToTheirPenArea(15);
	}
}
