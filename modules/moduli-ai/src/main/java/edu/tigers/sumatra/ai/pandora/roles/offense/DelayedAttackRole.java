/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.statemachine.AState;


public class DelayedAttackRole extends ARole
{
	public DelayedAttackRole()
	{
		super(ERole.DELAYED_ATTACK);
		
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
			IVector2 dir = Geometry.getGoalTheir().getCenter().subtractNew(getBall().getPos()).scaleToNew(-250);
			if (dir.isZeroVector())
			{
				dir = Vector2.fromXY(-300, 0);
			}
			IVector2 movePos = getBall().getPos().addNew(dir);
			
			getCurrentSkill().getMoveCon().updateLookAtTarget(getWFrame().getBall().getPos());
			getCurrentSkill().getMoveCon().updateDestination(movePos);
		}
	}
}
