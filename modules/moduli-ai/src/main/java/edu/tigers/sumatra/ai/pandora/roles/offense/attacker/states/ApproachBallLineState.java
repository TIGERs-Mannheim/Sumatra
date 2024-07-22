/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker.states;

import edu.tigers.sumatra.ai.metis.kicking.Kick;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.pathfinder.EObstacleAvoidanceMode;
import edu.tigers.sumatra.skillsystem.skills.ApproachBallLineSkill;


/**
 * Approach the ball line of a moving ball (intercept the ball)
 */
public class ApproachBallLineState extends AAttackerRoleState<ApproachBallLineSkill>
{
	public ApproachBallLineState(AttackerRole role)
	{
		super(ApproachBallLineSkill::new, role, EAttackerState.APPROACH_BALL_LINE);
	}


	@Override
	protected void doStandardUpdate()
	{
		skill.setMaximumReasonableRedirectAngle(OffensiveConstants.getMaximumReasonableRedirectAngle());
		skill.setDesiredBallCatchPos(getRole().getAction().getBallContactPos());
		skill.getMoveCon().setObstacleAvoidanceMode(EObstacleAvoidanceMode.AGGRESSIVE);
		skill.setApproachFailedMinBallVel(OffensiveConstants.getAbortBallInterceptionVelThreshold());

		getRole().getAction().getKickOpt()
				.map(Kick::getTarget).ifPresentOrElse(skill::setTarget, () -> skill.setTarget(null));
	}
}
