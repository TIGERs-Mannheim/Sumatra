/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.RotationSkill;
import edu.tigers.sumatra.statemachine.AState;

import java.util.Optional;


public class FreeSkirmishRole extends ARole
{
	public FreeSkirmishRole()
	{
		super(ERole.FREE_SKIRMISH);

		setInitialState(new DefaultState());
	}


	private class DefaultState extends AState
	{
		@Override
		public void doEntryActions()
		{
			IVector2 target = getAiFrame().getTacticalField().getSkirmishInformation().getSupportiveCircleCatchPos();
			Optional<Double> angle;
			if (target != null)
			{
				IVector2 ballToTarget = target.subtractNew(getWFrame().getBall().getPos());
				IVector2 meToBall = getWFrame().getBall().getPos().subtractNew(getPos());
				angle = meToBall.angleTo(ballToTarget);
			} else
			{
				angle = Optional.empty();
			}

			RotationSkill skill;
			if (angle.isEmpty())
			{
				skill = new RotationSkill(AngleMath.deg2rad(170));
			} else if (angle.get() > 0)
			{
				// right
				skill = new RotationSkill(AngleMath.deg2rad(170));
			} else
			{
				// left
				skill = new RotationSkill(AngleMath.deg2rad(-170));
			}
			setNewSkill(skill);
		}
	}
}
