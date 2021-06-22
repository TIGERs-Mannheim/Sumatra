/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.redirect;

import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.ball.trajectory.flat.FlatBallTrajectory;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBall;

import java.util.Optional;


public class ConstantLossRedirectConsultant implements IRedirectConsultant
{
	@Override
	public double getKickSpeed(IVector2 incomingBallVel, IVector2 desiredOutgoingBallVel)
	{
		return getKickVelocityWithoutSpin(incomingBallVel, desiredOutgoingBallVel).getLength2();
	}


	@Override
	public double getTargetAngle(IVector2 incomingBallVel, IVector2 desiredOutgoingBallVel)
	{
		return getKickVelocityWithoutSpin(incomingBallVel, desiredOutgoingBallVel).getAngle();
	}


	@Override
	public double getTargetAngle(ITrackedBall ball, IVector2 source, IVector2 target, double targetBallSpeed)
	{
		return getKickVelocity(ball, source, target, targetBallSpeed).getAngle();
	}


	@Override
	public double getKickSpeed(ITrackedBall ball, IVector2 source, IVector2 target, double targetBallSpeed)
	{
		return getKickVelocity(ball, source, target, targetBallSpeed).getLength2();
	}


	public IBallTrajectory getRedirectTrajectory(ITrackedBall ball, IVector2 redirectPos, IVector2 kickVel)
	{
		var timeTillBallArrives = ball.getTrajectory().getTimeByPos(redirectPos);
		var stateAtCollision = ball.getTrajectory().getMilliStateAtTime(timeTillBallArrives);

		if (stateAtCollision.getVel().isZeroVector())
		{
			// teleport ball to source pos if it will not reach that pos, best option here
			stateAtCollision = stateAtCollision.toBuilder().withPos(redirectPos.getXYZVector()).build();
		}

		return getFlatTrajectory(stateAtCollision, kickVel);
	}


	IVector2 getKickVelocityWithoutSpin(IVector2 incomingBallVel, IVector2 desiredOutgoingBallVel)
	{
		// This implementation ignores ball spin
		double targetBallSpeed = desiredOutgoingBallVel.getLength2();

		Vector2 kickVel = Vector2.copy(desiredOutgoingBallVel);

		for (int i = 0; i < 5; i++)
		{
			IVector2 outVel = computeKickVelPlusReflectVel(incomingBallVel, kickVel);

			kickVel.multiply(targetBallSpeed / outVel.getLength2());

			double angleError = outVel.angleTo(desiredOutgoingBallVel).orElse(0.0);
			kickVel.turn(angleError);
		}

		return kickVel;
	}


	IVector2 getKickVelocity(ITrackedBall ball, IVector2 redirectPos, IVector2 target, double targetBallSpeed)
	{
		var aimingTarget = Optional.ofNullable(target).orElse(ball.getPos());

		var timeTillBallArrives = ball.getTrajectory().getTimeByPos(redirectPos);
		var stateAtCollision = ball.getTrajectory().getMilliStateAtTime(timeTillBallArrives);

		if (stateAtCollision.getVel().isZeroVector())
		{
			// teleport ball to source pos if it will not reach that pos, best option here
			stateAtCollision = stateAtCollision.toBuilder().withPos(redirectPos.getXYZVector()).build();
		}

		// the limits for the redirect orientation is between inbound velocity angle and angle to target pos
		IVector2 centerRedirectVector = stateAtCollision.getVel().getXYVector().multiplyNew(-1).normalize()
				.add(Vector2.fromPoints(redirectPos, aimingTarget).normalize()).multiply(0.5);
		double maxHalfAngleRedirect = Vector2.fromPoints(redirectPos, aimingTarget).angleToAbs(centerRedirectVector)
				.orElse(0.0);
		double inc = maxHalfAngleRedirect / 2;

		Vector2 kickVel = centerRedirectVector.scaleToNew(targetBallSpeed);

		// perform a binary search to minimize ball trajectory distance to target
		while (inc > 0.0001)
		{
			// iteratively adjust the kick velocity until the initial trajectory velocity is close to targetBallSpeed
			for (int i = 0; i < 5; i++)
			{
				kickVel.multiply(
						targetBallSpeed / computeKickVelPlusReflectVel(
								stateAtCollision.getVel().multiplyNew(1e-3).getXYVector(), kickVel).getLength2());
			}

			var trajPDist = getFlatTrajectory(stateAtCollision, kickVel.turnNew(0.001)).getPlanarCurve()
					.getMinimumDistanceToPoint(aimingTarget);
			var trajNDist = getFlatTrajectory(stateAtCollision, kickVel.turnNew(-0.001)).getPlanarCurve()
					.getMinimumDistanceToPoint(aimingTarget);

			if (trajPDist < trajNDist)
			{
				kickVel.turn(inc);
			} else
			{
				kickVel.turn(-inc);
			}

			inc *= 0.5;
		}

		return kickVel;
	}


	private FlatBallTrajectory getFlatTrajectory(BallState ballStateAtCollision, IVector2 kickVel)
	{
		return FlatBallTrajectory.fromKick(Geometry.getBallFactory().getBallParams(),
				ballStateAtCollision.getPos().getXYVector(),
				computeKickVelPlusReflectVel(ballStateAtCollision.getVel().multiplyNew(1e-3).getXYVector(), kickVel)
						.multiplyNew(1e3),
				ballStateAtCollision.getSpin());
	}


	IVector2 computeKickVelPlusReflectVel(IVector2 ballVel, IVector2 kickVel)
	{
		double orientation = kickVel.getAngle();

		IVector2 velocityFactors = Vector2.fromXY(-Geometry.getBallParameters().getRedirectRestitutionCoefficient(),
				Geometry.getBallParameters().getRedirectSpinFactor());

		// pointing at the robot
		IVector2 ballVelInRobotFrame = ballVel.turnNew(-orientation);
		IVector2 ballVelOutRobotFrame = ballVelInRobotFrame.multiplyNew(velocityFactors);
		IVector2 ballVelOut = ballVelOutRobotFrame.turnNew(orientation);

		return kickVel.addNew(ballVelOut);
	}
}
