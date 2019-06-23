/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.ApproachBallLineSkill;


/**
 * Catch up with the ball, if the bot is in front of the ball.
 * The state is over, when the bot is near the ball travel line
 */
public class ApproachBallLineState extends AOffensiveState
{
	@Configurable(defValue = "20.0", comment = "When distance is below, the ball is considered catched up")
	private static double maxDistanceToBallLine = 20;
	
	static
	{
		ConfigRegistration.registerClass("roles", ApproachBallLineState.class);
	}
	
	
	public ApproachBallLineState(final ARole role)
	{
		super(role);
	}
	
	
	@Override
	public void doEntryActions()
	{
		setNewSkill(new ApproachBallLineSkill());
	}
	
	
	@Override
	public void doUpdate()
	{
		if (getBall().getTrajectory().getTravelLine()
				.distanceTo(getBot().getBotKickerPos()) < maxDistanceToBallLine)
		{
			triggerEvent(EBallHandlingEvent.BALL_LINE_APPROACHED);
		} else if (ballMovesAwayFromMe())
		{
			triggerEvent(EBallHandlingEvent.BALL_MOVES_AWAY_FROM_ME);
		} else if (getBall().getVel().getLength2() < 0.1)
		{
			triggerEvent(EBallHandlingEvent.BALL_STOPPED_MOVING);
		}
	}
	
	
	private boolean ballMovesAwayFromMe()
	{
		IVector2 base = getBall().getPos().subtractNew(getBall().getVel().scaleToNew(200));
		return getPos().subtractNew(base).angleToAbs(getBall().getVel()).map(a -> a > AngleMath.PI_HALF).orElse(false);
	}
}
