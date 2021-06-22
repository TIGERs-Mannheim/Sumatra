/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.redirect;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBall;

import java.util.Optional;


/**
 * Abstract base class for all redirect consultants
 */
public interface IRedirectConsultant
{
	double getKickSpeed(final IVector2 incomingBallVel, final IVector2 desiredOutgoingBallVel);


	double getTargetAngle(final IVector2 incomingBallVel, final IVector2 desiredOutgoingBallVel);


	default IVector2 getBallVelAtCollision(ITrackedBall ball, IVector2 collisionPos)
	{
		var timeTillBallArrives = ball.getTrajectory().getTimeByPos(collisionPos);
		var ballVelAtCollision = ball.getTrajectory().getVelByTime(timeTillBallArrives).getXYVector();
		if (ball.getVel().isZeroVector() || ballVelAtCollision.isZeroVector())
		{
			// use a rough guess to have a good default target angle
			return collisionPos.subtractNew(ball.getPos()).scaleTo(3.0);
		}
		return ballVelAtCollision;
	}


	default double getTargetAngle(ITrackedBall ball, IVector2 source, IVector2 target, double targetBallSpeed)
	{
		var incomingBallVel = getBallVelAtCollision(ball, source);
		var aimingTarget = Optional.ofNullable(target).orElse(ball.getPos());
		var desiredBallVel = aimingTarget.subtractNew(source).scaleTo(targetBallSpeed);
		return getTargetAngle(incomingBallVel, desiredBallVel);
	}

	default double getKickSpeed(ITrackedBall ball, IVector2 source, IVector2 target, double targetBallSpeed)
	{
		var incomingBallVel = getBallVelAtCollision(ball, source);
		var aimingTarget = Optional.ofNullable(target).orElse(ball.getPos());
		var desiredBallVel = aimingTarget.subtractNew(source).scaleTo(targetBallSpeed);
		return getKickSpeed(incomingBallVel, desiredBallVel);
	}
}
