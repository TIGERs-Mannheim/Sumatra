/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker.states;


import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.pathfinder.EObstacleAvoidanceMode;
import edu.tigers.sumatra.skillsystem.skills.ApproachAndStopBallSkill;


public class ApproachAndStopBallState extends AAttackerRoleState<ApproachAndStopBallSkill>
{
	public ApproachAndStopBallState(AttackerRole role)
	{
		super(ApproachAndStopBallSkill::new, role, EAttackerState.APPROACH_AND_STOP_BALL);
	}

	@Override
	protected void doStandardUpdate()
	{
		skill.setSucceedEarly(true);
		skill.getMoveCon().setObstacleAvoidanceMode(EObstacleAvoidanceMode.AGGRESSIVE);
	}
}
