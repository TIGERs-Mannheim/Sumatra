/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trajectory;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.ToString;
import net.jafama.DoubleWrapper;
import net.jafama.FastMath;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;


/**
 * Bang Bang Trajectory for two dimensions.
 */
@ToString
@Persistent
class BangBangTrajectory2D implements ITrajectory<IVector2>
{
	final BangBangTrajectory1D x = new BangBangTrajectory1D();
	final BangBangTrajectory1D y = new BangBangTrajectory1D();


	@Override
	public Vector2 getPositionMM(final double t)
	{
		return Vector2.fromXY(x.getPositionMM(t), y.getPositionMM(t));
	}


	@Override
	public Vector2 getPosition(final double t)
	{
		return Vector2.fromXY(x.getPosition(t), y.getPosition(t));
	}


	@Override
	public Vector2 getVelocity(final double t)
	{
		return Vector2.fromXY(x.getVelocity(t), y.getVelocity(t));
	}


	@Override
	public Vector2 getAcceleration(final double t)
	{
		return Vector2.fromXY(x.getAcceleration(t), y.getAcceleration(t));
	}


	@Override
	public double getTotalTime()
	{
		return Math.max(x.getTotalTime(), y.getTotalTime());
	}


	@Override
	public BangBangTrajectory2D mirrored()
	{
		BangBangTrajectory2D mirrored = new BangBangTrajectory2D();
		mirrored.x.numParts = x.numParts;
		mirrored.y.numParts = y.numParts;
		for (int i = 0; i < BangBangTrajectory1D.MAX_PARTS; i++)
		{
			mirrored.x.parts[i].tEnd = x.parts[i].tEnd;
			mirrored.x.parts[i].acc = -x.parts[i].acc;
			mirrored.x.parts[i].v0 = -x.parts[i].v0;
			mirrored.x.parts[i].s0 = -x.parts[i].s0;
			mirrored.y.parts[i].tEnd = y.parts[i].tEnd;
			mirrored.y.parts[i].acc = -y.parts[i].acc;
			mirrored.y.parts[i].v0 = -y.parts[i].v0;
			mirrored.y.parts[i].s0 = -y.parts[i].s0;
		}
		return mirrored;
	}


	@Override
	public PosVelAcc<IVector2> getValuesAtTime(final double tt)
	{
		PosVelAcc<Double> xValues = x.getValuesAtTime(tt);
		PosVelAcc<Double> yValues = y.getValuesAtTime(tt);
		return new PosVelAcc<>(
				Vector2.fromXY(xValues.getPos(), yValues.getPos()),
				Vector2.fromXY(xValues.getVel(), yValues.getVel()),
				Vector2.fromXY(xValues.getAcc(), yValues.getAcc())
		);
	}


	@Override
	public List<Double> getTimeSections()
	{
		List<Double> list = new ArrayList<>(x.getTimeSections());
		list.addAll(y.getTimeSections());
		return list;
	}


	/**
	 * Generate the trajectory based on the input parameters and returns this instance for chaining.
	 *
	 * @param s0       initial position
	 * @param s1       target position
	 * @param v0       initial velocity
	 * @param vmax     max velocity
	 * @param acc      acceleration
	 * @param accuracy synchronization accuracy
	 * @param alphaFn  alpha function for synchronization
	 * @return this for chaining
	 */
	BangBangTrajectory2D generate(
			final IVector2 s0,
			final IVector2 s1,
			final IVector2 v0,
			final float vmax,
			final float acc,
			final float accuracy,
			final UnaryOperator<Float> alphaFn
	)
	{
		final var s0x = (float) s0.x();
		final var s0y = (float) s0.y();
		final var s1x = (float) s1.x();
		final var s1y = (float) s1.y();
		final var v0x = (float) v0.x();
		final var v0y = (float) v0.y();

		float inc = (float) AngleMath.PI / 8.0f;
		float alpha = (float) AngleMath.PI / 4.0f;

		// binary search, some iterations (fixed)
		while (inc > 1e-7)
		{
			DoubleWrapper cos = new DoubleWrapper();
			final float sA = (float) FastMath.sinAndCos(alphaFn.apply(alpha), cos);
			final float cA = (float) cos.value;

			x.generate(s0x, s1x, v0x, vmax * cA, acc * cA);
			y.generate(s0y, s1y, v0y, vmax * sA, acc * sA);

			double diff = Math.abs(x.getTotalTime() - y.getTotalTime());
			if (diff < accuracy)
			{
				break;
			}
			if (x.getTotalTime() > y.getTotalTime())
			{
				alpha -= inc;
			} else
			{
				alpha += inc;
			}

			inc *= 0.5f;
		}
		return this;
	}
}
