package edu.tigers.sumatra.ai.pandora.roles.offense.attacker.states;


import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
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
		skill.setSucceedEarlyVel(OffensiveConstants.getBallIsRollingThreshold());
		skill.setMoveCon(skill.getMoveCon().withObstacleAvoidanceMode(EObstacleAvoidanceMode.AGGRESSIVE));

		var dribblingInformation = getRole().getAiFrame().getTacticalField().getDribblingInformation();
		boolean violationImminent = dribblingInformation.isDribblingInProgress() && dribblingInformation.isViolationImminent();
		skill.setDribblingFoulImminent(violationImminent);
	}
}
