/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker.states;


import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.pathfinder.EObstacleAvoidanceMode;
import edu.tigers.sumatra.skillsystem.skills.ReceiveBallSkill;


public class ReceiveState extends AAttackerRoleState<ReceiveBallSkill>
{
	public ReceiveState(AttackerRole role)
	{
		super(ReceiveBallSkill::new, role, EAttackerState.RECEIVE);
	}


	@Override
	protected void onInit()
	{
		super.onInit();
		skill.setBallSpeedHysteresis(new Hysteresis(
				OffensiveConstants.getAbortBallInterceptionVelThreshold(),
				OffensiveConstants.getBallIsRollingThreshold()));
	}


	@Override
	protected void doStandardUpdate()
	{
		// State is only activated when close. Let it hold its position!
		skill.getMoveCon().setObstacleAvoidanceMode(EObstacleAvoidanceMode.AGGRESSIVE);
		skill.getMoveCon().setTheirBotsObstacle(false);
		skill.setBallReceivingPosition(getRole().getAction().getBallContactPos());
		skill.setMaxReceptionHeight(OffensiveConstants.getMaxInterceptHeight());
		skill.setUseOvershoot(OffensiveConstants.isAllowOvershootingBallInterceptions());
	}
}

