/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.botmanager.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.DynamicPosition;


public class ReceiveBallSkill extends ABallArrivalSkill
{

	@Configurable(defValue = "135.0", comment = "Margin between penaltyarea and bot destination [mm]")
	private static double marginBetweenDestAndPenArea = 135.0;

	@Configurable(comment = "Dribble speed during receive", defValue = "6000.0")
	private static double dribbleSpeed = 6000;


	private final TimestampTimer receiveDelayTimer = new TimestampTimer(0.2);


	public ReceiveBallSkill(final IVector2 receivingPosition)
	{
		this(new DynamicPosition(receivingPosition));
	}


	public ReceiveBallSkill(final DynamicPosition receivingPosition)
	{
		super(ESkill.RECEIVE_BALL, receivingPosition);

		setInitialState(new ReceiveState());
	}


	@Override
	protected void updateKickerDribbler(final KickerDribblerCommands kickerDribblerOutput)
	{
		super.updateKickerDribbler(kickerDribblerOutput);

		if (getTBot().hasBallContact())
		{
			kickerDribblerOutput.setDribblerSpeed(0);
		} else
		{
			kickerDribblerOutput.setDribblerSpeed(dribbleSpeed);
		}
	}


	/**
	 * @return true, if the ball is moving towards the receiver and receiver can reach the receiving position
	 */
	public boolean ballCanBeReceivedAtReceivingPosition()
	{
		return !isInitialized()
				|| (ballIsMovingTowardsMe() && receivingPositionIsReachableByBall(receivingPosition.getPos()));
	}


	/**
	 * @return true, if the bot is still in the process of receiving the ball. The skill should not be stopped now.
	 */
	public boolean receivingBall()
	{
		return receiveDelayTimer.isRunning() && !receiveDelayTimer.isTimeUp(getWorldFrame().getTimestamp());
	}


	/**
	 * @return true, if the ball is in front of the receiver and stopped
	 */
	public boolean ballHasBeenReceived()
	{
		boolean received = isInitialized() && !ballIsMoving() && ballNearKicker();
		if (received)
		{
			receiveDelayTimer.update(getWorldFrame().getTimestamp());
			return receiveDelayTimer.isTimeUp(getWorldFrame().getTimestamp());
		}
		receiveDelayTimer.reset();
		return false;
	}


	private class ReceiveState extends ABallArrivalState
	{
		@Override
		protected double calcMyTargetAngle(final IVector2 kickerPos)
		{
			IVector2 ballPos = ballStabilizer.getBallPos();
			IVector2 kickerToBall = ballPos.subtractNew(kickerPos);
			if (kickerToBall.getLength2() > 50)
			{
				return kickerToBall.getAngle();
			}
			return ballPos.subtractNew(getPos()).getAngle(0);
		}


		@Override
		protected double getMarginBetweenDestAndPenArea()
		{
			return marginBetweenDestAndPenArea;
		}
	}
}
