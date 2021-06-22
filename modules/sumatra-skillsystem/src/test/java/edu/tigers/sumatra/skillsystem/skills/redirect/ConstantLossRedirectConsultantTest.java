/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.redirect;

import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.TrackedBall;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class ConstantLossRedirectConsultantTest
{
	private ITrackedBall genBall(final IVector2 vel)
	{
		IBallTrajectory ballTrajectory = Geometry.getBallFactory()
				.createTrajectoryFromKickedBallWithoutSpin(Vector2f.ZERO_VECTOR, Vector3.from2d(vel, 0));
		return TrackedBall.fromBallStateVisible(0, ballTrajectory.getMilliStateAtTime(0));
	}


	private double evaluateRedirect(IVector2 initialVel, IVector2 sourcePos, IVector2 targetPos, double targetSpeed)
	{
		var con = new ConstantLossRedirectConsultant();
		var ball = genBall(initialVel);

		var kickVel = con.getKickVelocity(ball, sourcePos, targetPos, targetSpeed);
		var redirectTraj = con.getRedirectTrajectory(ball, sourcePos, kickVel);

		return redirectTraj.getPlanarCurve().getMinimumDistanceToPoint(targetPos);
	}


	@Test
	public void testPossibleRedirect()
	{
		IVector2 initialVel = Vector2.fromXY(3000, 0);
		IVector2 sourcePos = Vector2.fromXY(3000, 0);
		IVector2 targetPos = Vector2.fromXY(0, 3000);
		double targetSpeed = 5.0;

		double minDist = evaluateRedirect(initialVel, sourcePos, targetPos, targetSpeed);

		assertThat(minDist)
				.withFailMessage("Missed redirect target by too much.")
				.isLessThanOrEqualTo(5.0);
	}


	@Test
	public void testImpossibleRedirectAngle()
	{
		IVector2 initialVel = Vector2.fromXY(3000, 0);
		IVector2 sourcePos = Vector2.fromXY(3000, 0);
		IVector2 targetPos = Vector2.fromXY(6000, 3000);
		double targetSpeed = 5.0;

		double minDist = evaluateRedirect(initialVel, sourcePos, targetPos, targetSpeed);

		assertThat(minDist)
				.withFailMessage("Missed redirect target by too much.")
				.isLessThanOrEqualTo(5.0);
	}


	@Test
	public void testSourceNotReached()
	{
		IVector2 initialVel = Vector2.fromXY(1000, 0);
		IVector2 sourcePos = Vector2.fromXY(3000, 0);
		IVector2 targetPos = Vector2.fromXY(0, 3000);
		double targetSpeed = 5.0;

		double minDist = evaluateRedirect(initialVel, sourcePos, targetPos, targetSpeed);

		assertThat(minDist)
				.withFailMessage("Missed redirect target by too much.")
				.isLessThanOrEqualTo(5.0);
	}


	@Test
	public void testTargetNotReached()
	{
		IVector2 initialVel = Vector2.fromXY(3000, 0);
		IVector2 sourcePos = Vector2.fromXY(3000, 0);
		IVector2 targetPos = Vector2.fromXY(0, 3000);
		double targetSpeed = 1.0;

		double minDist = evaluateRedirect(initialVel, sourcePos, targetPos, targetSpeed);

		assertThat(minDist)
				.withFailMessage("Too slow ball reached target, WTF?")
				.isGreaterThan(500.0);
	}


	@Test
	public void testKickWithoutSpin()
	{
		IVector2 inVel = Vector2.fromXY(3.0, 0);
		IVector2 outVel = Vector2.fromXY(-3.0, 3.0);

		var con = new ConstantLossRedirectConsultant();

		IVector2 kickVel = con.getKickVelocityWithoutSpin(inVel, outVel);
		IVector2 computedOutVel = con.computeKickVelPlusReflectVel(inVel, kickVel);

		assertThat(outVel.distanceTo(computedOutVel))
				.withFailMessage("Computed out velocity is not close to desired one.")
				.isLessThanOrEqualTo(0.1);
	}
}
