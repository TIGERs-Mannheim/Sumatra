/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import lombok.Setter;


public class DribbleTestRole extends ARole
{
	@Setter
	private IVector2 target;
	@Setter
	private double velMax;


	public DribbleTestRole()
	{
		super(ERole.DRIBBLE_TEST);
		setInitialState(new DefaultState());
	}


	private class DefaultState extends RoleState<TouchKickSkill>
	{
		DefaultState()
		{
			super(TouchKickSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			skill.getMoveCon().physicalObstaclesOnly();
			skill.getMoveCon().setBallObstacle(false);
			skill.getMoveConstraints().setVelMax(velMax);
			skill.setAdaptKickSpeedToRobotSpeed(false);
			skill.setDesiredKickParams(KickParams.straight(0.2));
			skill.setTarget(target);
		}
	}
}
