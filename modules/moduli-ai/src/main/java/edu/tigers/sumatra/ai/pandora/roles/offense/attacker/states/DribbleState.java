package edu.tigers.sumatra.ai.pandora.roles.offense.attacker.states;

import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.pathfinder.EObstacleAvoidanceMode;
import edu.tigers.sumatra.skillsystem.skills.dribbling.DragBallSkill;


public class DribbleState extends AAttackerRoleState<DragBallSkill>
{

	public DribbleState(AttackerRole role)
	{
		super(DragBallSkill::new, role, EAttackerState.DRIBBLE);
	}


	@Override
	protected void doStandardUpdate()
	{
		skill.setMoveCon(skill.getMoveCon().withObstacleAvoidanceMode(EObstacleAvoidanceMode.AGGRESSIVE));
		var dribbleToPos = getRole().getAction().getDribbleToPos();
		if (dribbleToPos != null && dribbleToPos.getDribbleToDestination() != null)
		{
			skill.setDestination(dribbleToPos.getDribbleToDestination());
			if (dribbleToPos.getDribbleToDestination().distanceTo(getRole().getPos()) < Geometry.getBotRadius() * 2.5)
			{
				// hold your ground!
				skill.setMoveCon(skill.getMoveCon()
						.withObstacleAvoidanceMode(EObstacleAvoidanceMode.AGGRESSIVE)
						.withTheirBotsObstacle(false));
			} else {
				skill.setMoveCon(skill.getMoveCon()
						.withObstacleAvoidanceMode(EObstacleAvoidanceMode.NORMAL)
						.withTheirBotsObstacle(true));
			}
		}

		var dribblingInformation = getRole().getAiFrame().getTacticalField().getDribblingInformation();
		boolean needToKick = dribblingInformation.isDribblingInProgress() && dribblingInformation.isViolationImminent();
		skill.setForceKick(needToKick);

		skill.setTargetOrientation(getProtectionTarget().subtractNew(getRole().getPos()).multiplyNew(-1).getAngle());
	}
}
