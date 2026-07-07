package edu.tigers.sumatra.trajectory;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import org.apache.commons.lang3.Validate;

import java.util.function.UnaryOperator;


public final class BangBangTrajectoryFactory
{
	private static final double MAX_VEL_TOLERANCE = 0.2;
	static final float SYNC_ACCURACY = 1e-3f;
	static final UnaryOperator<Float> ALPHA_FN_ASYNC = alpha -> alpha + (((float) AngleMath.PI_HALF - alpha)
			* 0.5f);


	public BangBangTrajectory2DAsync async(
			final IVector2 s0,
			final IVector2 s1,
			final IVector2 v0,
			final double vmax,
			final double acc,
			final IVector2 primaryDirection
	)
	{
		Validate.notNull(primaryDirection);
		Validate.isTrue(!primaryDirection.isZeroVector(), "zero primary direction vector");
		validateParameters(s0, s1, v0, vmax, acc);

		final var rotation = primaryDirection.getAngle();
		final var startToTarget = s1.subtractNew(s0).turn(-rotation);
		final var v0Rotated = v0.turnNew(-rotation);

		BangBangTrajectory2D child = new BangBangTrajectory2D().generate(
				Vector2f.ZERO_VECTOR,
				startToTarget,
				v0Rotated,
				(float) vmax,
				(float) acc,
				SYNC_ACCURACY,
				ALPHA_FN_ASYNC
		);
		return new BangBangTrajectory2DAsync(child, s0, rotation);
	}


	public BangBangTrajectory2D sync(
			final IVector2 s0,
			final IVector2 s1,
			final IVector2 v0,
			final double vmax,
			final double acc
	)
	{
		validateParameters(s0, s1, v0, vmax, acc);
		return new BangBangTrajectory2D().generate(
				s0,
				s1,
				adaptVel(v0, vmax),
				(float) vmax,
				(float) acc,
				SYNC_ACCURACY,
				f -> f
		);
	}


	private static void validateParameters(IVector2 s0, IVector2 s1, IVector2 v0, double vmax, double acc)
	{
		Validate.isTrue(s0.isFinite(), "initial position is not finite: ", s0);
		Validate.isTrue(s1.isFinite(), "target position is not finite: ", s1);
		Validate.isTrue(v0.isFinite(), "initial velocity is not finite: ", s0);
		Validate.isTrue(vmax > 0, "max velocity must be >=0: ", vmax);
		Validate.isTrue(acc > 0, "max acceleration must be >0: ", acc);
	}


	public ITrajectory<Double> single(
			final double initialPos,
			final double finalPos,
			final double initialVel,
			final double maxVel,
			final double maxAcc
	)
	{
		return singleDim(initialPos, finalPos, initialVel, maxVel, maxAcc);
	}


	public BangBangTrajectory1D singleDim(
			final double initialPos,
			final double finalPos,
			final double initialVel,
			final double maxVel,
			final double maxAcc
	)
	{
		return new BangBangTrajectory1D().generate(
				(float) initialPos,
				(float) finalPos,
				(float) adaptVel(initialVel, maxVel),
				(float) maxVel,
				(float) maxAcc
		);
	}


	public BangBangTrajectory1DOrient orientation(
			final double initialPos,
			final double finalPos,
			final double initialVel,
			final double maxVel,
			final double maxAcc
	)
	{
		var adaptedFinalPos = initialPos + AngleMath.normalizeAngle(finalPos - AngleMath.normalizeAngle(initialPos));
		return new BangBangTrajectory1DOrient(
				singleDim(initialPos, adaptedFinalPos, adaptVel(initialVel, maxVel), maxVel, maxAcc));
	}


	/**
	 * Limit the current speed to the max speed, if it is only slightly above velMax.
	 * Trajectories are often generated based on the last trajectory and imprecision will lead to a
	 * propagating error that lets the robot drive significantly faster than velMax (like >+0.2m/s with 1.5m/s)
	 *
	 * @param v0
	 * @param vMax
	 * @return
	 */
	private static IVector2 adaptVel(IVector2 v0, double vMax)
	{
		var curVelAbs = v0.getLength2();
		if (curVelAbs > vMax && curVelAbs < vMax + MAX_VEL_TOLERANCE)
		{
			return v0.scaleToNew(vMax);
		}
		return v0;
	}


	private static double adaptVel(double v0, double vMax)
	{
		var curVelAbs = v0;
		if (curVelAbs > vMax && curVelAbs < vMax + MAX_VEL_TOLERANCE)
		{
			return vMax;
		}
		return v0;
	}
}
