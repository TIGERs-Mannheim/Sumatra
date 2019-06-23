/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.ApproachAndStopBallSkill;


public class ApproachAndStopBallState extends AOffensiveState
{
	public ApproachAndStopBallState(final ARole role)
	{
		super(role);
	}
	
	
	@Override
	public void doEntryActions()
	{
		setNewSkill(new ApproachAndStopBallSkill());
	}
	
	
	@Override
	public void doUpdate()
	{
		if (((ApproachAndStopBallSkill) getCurrentSkill()).ballStoppedByBot())
		{
			triggerEvent(EBallHandlingEvent.BALL_STOPPED_BY_BOT);
		} else if (((ApproachAndStopBallSkill) getCurrentSkill()).ballStoppedMoving())
		{
			triggerEvent(EBallHandlingEvent.BALL_STOPPED_MOVING);
		} else if (ballMovesTowardsMe())
		{
			triggerEvent(EBallHandlingEvent.BALL_MOVES_TOWARDS_ME);
		}
	}
	
	
	private boolean ballMovesTowardsMe()
	{
		IVector2 base = getBall().getPos().addNew(getBall().getVel().scaleToNew(200));
		return getPos().subtractNew(base).angleToAbs(getBall().getVel()).map(a -> a < AngleMath.PI_HALF).orElse(false);
	}
}
