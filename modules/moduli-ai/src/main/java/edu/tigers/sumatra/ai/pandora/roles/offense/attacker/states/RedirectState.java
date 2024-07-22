/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker.states;


import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.pathfinder.EObstacleAvoidanceMode;
import edu.tigers.sumatra.skillsystem.skills.RedirectBallSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;


public class RedirectState extends AAttackerRoleState<RedirectBallSkill>
{
	public RedirectState(AttackerRole role)
	{
		super(RedirectBallSkill::new, role, EAttackerState.REDIRECT);
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
	protected boolean isNecessaryDataAvailable()
	{
		return getRole().getBall() != null && getRole().getAction().getKick() != null;
	}


	@Override
	protected void doStandardUpdate()
	{
		// State is only activated when close. Let it hold its position!
		skill.getMoveCon().setObstacleAvoidanceMode(EObstacleAvoidanceMode.AGGRESSIVE);
		skill.getMoveCon().setTheirBotsObstacle(false);
		skill.setBallReceivingPosition(getRole().getAction().getBallContactPos());
		skill.setTarget(getRole().getAction().getKick().getTarget());
		skill.setDesiredKickParams(getRole().getAction().getKick().getKickParams());
	}


	@Override
	protected void doFallbackUpdate()
	{
		// State is only activated when close. Let it hold its position!
		skill.getMoveCon().setTheirBotsObstacle(false);
		skill.setBallReceivingPosition(getRole().getPos());
		skill.setTarget(Geometry.getCenter());
		skill.setDesiredKickParams(KickParams.maxStraight());
	}
}
