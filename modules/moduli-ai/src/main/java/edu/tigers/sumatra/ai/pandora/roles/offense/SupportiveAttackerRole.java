/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense;

import edu.tigers.sumatra.ai.math.AiMath;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;


/**
 * Support the primary attacker by moving to supportive positions.
 */
public class SupportiveAttackerRole extends ARole
{
	public SupportiveAttackerRole()
	{
		super(ERole.SUPPORTIVE_ATTACKER);

		setInitialState(new DefaultState());
	}


	private class DefaultState extends RoleState<MoveToSkill>
	{
		DefaultState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			IVector2 movePos = getAiFrame().getTacticalField().getSupportiveAttackerMovePos();
			var dest = AiMath.adjustMovePositionWhenItsInvalid(getWFrame(), getBotID(), movePos);
			skill.updateLookAtTarget(getWFrame().getBall());
			skill.updateDestination(dest);
		}
	}
}
