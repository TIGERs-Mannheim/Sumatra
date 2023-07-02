/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker.states;

import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
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
		}

		skill.setTargetOrientation(getProtectionTarget().subtractNew(getRole().getPos()).multiplyNew(-1).getAngle());
	}
}
