/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense;

import edu.tigers.sumatra.ai.math.AiMath;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.statemachine.AState;


public class SupportiveAttackerRole extends ARole
{
	public SupportiveAttackerRole()
	{
		super(ERole.SUPPORTIVE_ATTACKER);
		
		setInitialState(new DefaultState());
	}
	
	private class DefaultState extends AState
	{
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(AMoveToSkill.createMoveToSkill());
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 movePos;
			switch (getAiFrame().getTacticalField().getSkirmishInformation().getStrategy())
			{
				case FREE_BALL:
					movePos = calcMovePositionFreeBall();
					break;
				default:
					movePos = getAiFrame().getTacticalField().getSupportiveAttackerMovePos();
			}
			movePos = AiMath.adjustMovePositionWhenItsInvalid(getWFrame(), getBotID(), movePos);
			getCurrentSkill().getMoveCon().updateLookAtTarget(getWFrame().getBall());
			getCurrentSkill().getMoveCon().updateDestination(movePos);
		}
		
		
		private IVector2 calcMovePositionFreeBall()
		{
			return getAiFrame().getTacticalField().getSkirmishInformation().getSupportiveCircleCatchPos();
		}
	}
}
