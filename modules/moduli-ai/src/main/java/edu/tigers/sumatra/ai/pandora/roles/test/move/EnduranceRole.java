package edu.tigers.sumatra.ai.pandora.roles.test.move;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Setter;


/**
 * Move through a list of destinations.
 */
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


	private class DefaultState extends MoveState
	{
		private int currentDestinationIdx = 0;


		@Override
		protected void onUpdate()
		{
			IVector2 currentDestination = destinations[currentDestinationIdx];
			if (currentDestination.distanceTo(getPos()) < nearDestTolerance)
			{
				currentDestinationIdx = (currentDestinationIdx + 1) % destinations.length;
			}
			skill.updateDestination(currentDestination);
			skill.updateTargetAngle(0.0);
		}
	}
}
