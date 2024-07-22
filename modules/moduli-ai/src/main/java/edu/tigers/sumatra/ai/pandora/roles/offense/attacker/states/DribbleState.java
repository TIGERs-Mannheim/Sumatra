/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

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
		skill.getMoveCon().setObstacleAvoidanceMode(EObstacleAvoidanceMode.AGGRESSIVE);
		var dribbleToPos = getRole().getAction().getDribbleToPos();
		if (dribbleToPos != null && dribbleToPos.getDribbleToDestination() != null)
		{
			skill.setDestination(dribbleToPos.getDribbleToDestination());
			if (dribbleToPos.getDribbleToDestination().distanceTo(getRole().getPos()) < Geometry.getBotRadius() * 2.5)
			{
				// hold your ground!
				skill.getMoveCon().setObstacleAvoidanceMode(EObstacleAvoidanceMode.AGGRESSIVE);
				skill.getMoveCon().setTheirBotsObstacle(false);
			} else {
				skill.getMoveCon().setObstacleAvoidanceMode(EObstacleAvoidanceMode.NORMAL);
				skill.getMoveCon().setTheirBotsObstacle(true);
			}
		}

		var dribblingInformation = getRole().getAiFrame().getTacticalField().getDribblingInformation();
		boolean needToKick = dribblingInformation.isDribblingInProgress() && dribblingInformation.isViolationImminent();
		skill.setForceKick(needToKick);

		skill.setTargetOrientation(getProtectionTarget().subtractNew(getRole().getPos()).multiplyNew(-1).getAngle());
	}
}
