/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test.move;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import lombok.Setter;


public class EnduranceRole extends ARole
{
	@Setter
	private IVector2[] destinations = new IVector2[] {};

	@Setter
	private double nearDestTolerance;


	public EnduranceRole()
	{
		super(ERole.ENDURANCE);
		setInitialState(new DefaultState());
	}


	private class DefaultState extends RoleState<MoveToSkill>
	{
		private int currentDestinationIdx = 0;


		public DefaultState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			IVector2 currentDestination = destinations[currentDestinationIdx];
			if (currentDestination.distanceTo(getPos()) < nearDestTolerance)
			{
				currentDestinationIdx = (currentDestinationIdx + 1) % destinations.length;
			}
			skill.updateDestination(currentDestination);
			skill.updateTargetAngle(0);
		}
	}
}
