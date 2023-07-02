/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker.states;


import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveActionType;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.EObstacleAvoidanceMode;
import edu.tigers.sumatra.skillsystem.skills.ProtectiveGetBallSkill;


public class ProtectState extends AAttackerRoleState<ProtectiveGetBallSkill>
{
	private IVector2 protectionPos;


	public ProtectState(AttackerRole role)
	{
		super(ProtectiveGetBallSkill::new, role, EAttackerState.PROTECT);
	}


	@Override
	protected void doStandardUpdate()
	{
		skill.getMoveCon().setObstacleAvoidanceMode(EObstacleAvoidanceMode.AGGRESSIVE);
		var action = getRole().getAction();
		if (action != null)
		{
			skill.setStrongDribblerContactNeeded(
					action.getType() == EOffensiveActionType.DRIBBLE_KICK ||
							action.getType() == EOffensiveActionType.PROTECT);
		}
		if (protectionPos == null || getRole().getBot().getBotKickerPos().distanceTo(getRole().getBot().getPos())
				> Geometry.getBallRadius() + Geometry.getBotRadius())
		{
			if (getRole().getBot().getBallContact().hadContact(0.3))
			{
				// if already has contact, keep orientation
				protectionPos = getRole().getBot().getBotKickerPos();
			} else
			{
				protectionPos = getProtectionTarget();
			}
		}
		var posToBall = getRole().getBall().getPos().subtractNew(protectionPos);
		var mirroredPos = protectionPos.addNew(posToBall.multiplyNew(2.0));
		skill.setProtectionTarget(mirroredPos);
	}
}

