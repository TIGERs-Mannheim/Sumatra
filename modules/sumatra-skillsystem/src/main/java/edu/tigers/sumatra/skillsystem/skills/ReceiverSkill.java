/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.ITrackedBall;


/**
 * Receive a ball and stop it
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ReceiverSkill extends AReceiveSkill
{
	@Configurable(comment = "Distance bot pos to ball [mm] to fix the target orientation of the bot.")
	private static double distThresholdToFixOrientation = 200;
	
	@Configurable(comment = "Dribble speed during receive", defValue = "10000.0")
	private static double dribbleSpeed = 10000;
	
	
	/**
	 * Default
	 */
	public ReceiverSkill()
	{
		super(ESkill.RECEIVER);
		
		IState receive = new ReceiveState();
		setInitialState(receive);
	}
	
	
	@Override
	protected double calcTargetOrientation(final IVector2 kickerPos)
	{
		ITrackedBall ball = getWorldFrame().getBall();
		
		double distBallBot = ball.getPos().distanceTo(kickerPos);
		if (distBallBot < distThresholdToFixOrientation)
		{
			// just keep last position -> this is probably most safe to not push ball away again
			return getLastTargetOrientation();
		}
		
		return ball.getPos().subtractNew(kickerPos).getAngle(0);
	}
	
	
	private class ReceiveState extends AReceiveState
	{
		@Override
		public void doUpdate()
		{
			super.doUpdate();
			// set dribble speed to at least 1 to improve receiving in simulation
			setDribblerSpeed(Math.max(dribbleSpeed, 1));
		}
	}
	
	
	/**
	 * @param dest dest
	 */
	public final void setReceiveDestination(final IVector2 dest)
	{
		setDesiredDestination(dest);
	}
}
