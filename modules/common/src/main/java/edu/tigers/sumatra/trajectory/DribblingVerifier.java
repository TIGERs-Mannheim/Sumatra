/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trajectory;

import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.DrawableBotShape;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.BotMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import lombok.Getter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;


/**
 * If you ever wanted to know if you lose the ball during dribbling this class is for you.
 */
public class DribblingVerifier
{
	private final double maxDribbleBallAcceleration;
	@Getter
	private final double maxRetainingBallAngle;
	private final double center2DribblerDist;
	private final double ballRadius;
	private final double center2BallDist;


	/**
	 * @param maxDribbleBallAcceleration Available in BotParams [m/s^2]
	 * @param maxRetainingBallAngle      Available in BotParams [rad]
	 * @param center2DribblerDist        Available in BotParams [mm]
	 * @param ballRadius                 Available in Geometry [mm]
	 */
	public DribblingVerifier(
			double maxDribbleBallAcceleration,
			double maxRetainingBallAngle,
			double center2DribblerDist,
			double ballRadius)
	{
		this.maxDribbleBallAcceleration = maxDribbleBallAcceleration;
		this.maxRetainingBallAngle = maxRetainingBallAngle;
		this.center2DribblerDist = center2DribblerDist;
		this.ballRadius = ballRadius;
		center2BallDist = center2DribblerDist + ballRadius;
	}


	/**
	 * Compute a score for a given trajectory judging how likely it is to lose the ball.
	 *
	 * @param traj Some planned trajectory.
	 * @return score < 0 ball lost, score = 0 might just work, score = 1 sure not to lose ball
	 */
	public double minBallRetentionScore(ITrajectory<IVector3> traj)
	{
		return generateSampleTimes(traj.getTotalTime())
				.boxed()
				.map(t -> ballRetentionScore(ballAccelerationAngle(totalBallAccelerationAtDribbler(traj, t))))
				.min(Double::compareTo)
				.orElse(-1.0);
	}

	/**
	 * Compute a score for a given trajectory step judging how likely it is to lose the ball.
	 *
	 * @param orientation
	 * @param angularVel angular velocity
	 * @param acc acceleration
	 * @return score < 0 ball lost, score = 0 might just work, score = 1 sure not to lose ball
	 */
	public double getRetentationScoreForTrajStep(double orientation, double angularVel, IVector3 acc)
	{
		var localAcc = Vector3.from2d(BotMath.convertGlobalBotVector2Local(acc.getXYVector(), orientation), acc.z());
		IVector2 ballAccLocal = totalBallAccelerationAtDribbler(angularVel, localAcc);
		double accAngle = ballAccelerationAngle(ballAccLocal);
		return ballRetentionScore(accAngle);
	}

	/**
	 * Compute various shapes which help to get insight of dribbling behaviour.
	 *
	 * @param traj Some planned trajectory.
	 * @return List of debug shapes.
	 */
	public List<IDrawableShape> ballRetentionDebugShapes(ITrajectory<IVector3> traj)
	{
		List<IDrawableShape> shapes = new ArrayList<>();

		var times = generateSampleTimes(traj.getTotalTime()).boxed().toList();

		for (double t : times)
		{
			IVector2 ballAccLocal = totalBallAccelerationAtDribbler(traj, t);
			double accAngle = ballAccelerationAngle(ballAccLocal);
			double score = ballRetentionScore(accAngle);

			IVector3 pos = traj.getPositionMM(t);

			IVector2 ballAccGlobal = BotMath.convertLocalBotVector2Global(ballAccLocal, pos.z());
			IVector2 ballPos = BotMath.convertLocalBotVector2Global(Vector2.fromY(center2BallDist), pos.z())
					.add(pos.getXYVector());

			DrawableBotShape bot = new DrawableBotShape(pos.getXYVector(), pos.z(), 90.0, center2DribblerDist);
			bot.setBorderColor(new Color(50, 50, 50, 100));
			shapes.add(bot);

			shapes.add(new DrawableCircle(ballPos, ballRadius, new Color(150, 100, 50, 100)));

			Color scoreColor;

			if (score < 0.0)
				scoreColor = new Color(255, 0, 0, 100);
			else if (score < 0.5)
				scoreColor = new Color(255, (int) (255 * score * 2), 0, 100);
			else
				scoreColor = new Color((int) ((1.0 - score) * 255 * 2), 255, 0, 100);

			shapes.add(new DrawableArrow(ballPos, ballAccGlobal.multiplyNew(50.0), scoreColor, 20));

			shapes.add(new DrawableArrow(pos.getXYVector(), traj.getAcceleration(t).getXYVector().multiplyNew(50.0),
					new Color(100, 0, 100, 100), 20));

			shapes.add(new DrawableAnnotation(pos.getXYVector(),
					String.format("a: %.1f, s: %.2f", AngleMath.rad2deg(accAngle), score), true).withFontHeight(20));
		}

		return shapes;
	}


	/**
	 * Turns out an even sampling is more robust than using the trajectories time sections.
	 *
	 * @param totalTime Total time of trajectory.
	 * @return A list of reasonable sampling points.
	 */
	private DoubleStream generateSampleTimes(double totalTime)
	{
		int segments = Math.min(20, (int) (totalTime / 0.1));
		double step = totalTime / segments;

		return DoubleStream.iterate(0.0, d -> d <= totalTime, d -> d + step);
	}


	/**
	 * Compute a score for a given ball acceleration judging how likely it is to lose the ball.
	 *
	 * @param ballAccelerationAngle Absolute angle of the ball acceleration vector from negative Y axis [rad]
	 * @return score < 0 ball lost, score = 0 might just work, score = 1 sure not to lose ball
	 */
	public double ballRetentionScore(double ballAccelerationAngle)
	{
		return (maxRetainingBallAngle - ballAccelerationAngle) / maxRetainingBallAngle;
	}


	/**
	 * Can be used to check if the acceleration vector would point outside the dribbler's geometry,
	 * leading to a ball loss.
	 *
	 * @param ballAcc Ball acceleration in robot reference frame [m/s^2]
	 * @return Absolute angle of the ball acceleration vector from negative Y axis.
	 */
	public double ballAccelerationAngle(IVector2 ballAcc)
	{
		// Swap of X and Y for atan2 is intentional to get an angle from Y axis, not from X axis
		return Math.abs(SumatraMath.atan2(ballAcc.x(), -ballAcc.y()));
	}


	/**
	 * Total ball acceleration at active dribbler.
	 *
	 * @param traj Some planned trajectory.
	 * @param t    Sampling time.
	 * @return Acceleration of the ball in the robot's reference frame including dribbler acceleration [m/s^2]
	 */
	public IVector2 totalBallAccelerationAtDribbler(ITrajectory<IVector3> traj, double t)
	{
		var pos = traj.getPosition(t);
		var vel = traj.getVelocity(t);
		var acc = traj.getAcceleration(t);

		var localAcc = Vector3.from2d(BotMath.convertGlobalBotVector2Local(acc.getXYVector(), pos.z()), acc.z());

		return totalBallAccelerationAtDribbler(vel.z(), localAcc);
	}


	/**
	 * Total ball acceleration at active dribbler.
	 *
	 * @param robotAngularVel Robot angular velocity [rad/s]
	 * @param robotAcc        Acceleration in local robot frame.
	 * @return Acceleration of the ball in the robot's reference frame including dribbler acceleration [m/s^2]
	 */
	public IVector2 totalBallAccelerationAtDribbler(double robotAngularVel, IVector3 robotAcc)
	{
		return kinematicBallAcceleration(robotAngularVel, robotAcc).addNew(Vector2.fromY(-maxDribbleBallAcceleration));
	}


	/**
	 * Kinematics computation for an accelerated object (ball) in an accelerated reference frame (robot).
	 *
	 * @param robotAngularVel [rad/s]
	 * @param robotAcc        Acceleration in local robot frame [m/s^2, rad/s^2]
	 * @return Acceleration of the ball in the robot's reference frame [m/s^2]
	 */
	public IVector2 kinematicBallAcceleration(double robotAngularVel, IVector3 robotAcc)
	{
		return Vector2.fromXY(
				-robotAcc.x() + robotAcc.z() * center2BallDist * 1e-3,
				-robotAcc.y() + robotAngularVel * robotAngularVel * center2BallDist * 1e-3);
	}
}
