/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trajectory;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.planarcurve.IPlanarCurveProvider;
import edu.tigers.sumatra.planarcurve.PlanarCurve;
import edu.tigers.sumatra.planarcurve.PlanarCurveSegment;
import edu.tigers.sumatra.trajectory.BangBangTrajectory1D.PosVelAcc;


/**
 * Bang Bang Trajectory for two dimensions.
 * 
 * @author AndreR
 */
@Persistent
public class BangBangTrajectory2D implements ITrajectory<IVector2>, IPlanarCurveProvider
{
	private BangBangTrajectory1D x;
	private BangBangTrajectory1D y;
	
	
	@SuppressWarnings("unused")
	protected BangBangTrajectory2D()
	{
	}
	
	
	/**
	 * @param x
	 * @param y
	 */
	protected BangBangTrajectory2D(final BangBangTrajectory1D x, final BangBangTrajectory1D y)
	{
		this.x = x;
		this.y = y;
	}
	
	
	/**
	 * @param initialPos [m]
	 * @param finalPos [m]
	 * @param initialVel [m/s]
	 * @param maxVel [m/s]
	 * @param maxAcc [m/s^2]
	 */
	public BangBangTrajectory2D(final IVector2 initialPos, final IVector2 finalPos,
			final IVector2 initialVel, final double maxVel, final double maxAcc)
	{
		generateTrajectory(initialPos, finalPos, initialVel, maxVel, maxAcc, a -> a);
	}
	
	
	/**
	 * @return
	 */
	public BangBangTrajectory1D getX()
	{
		return x;
	}
	
	
	/**
	 * @return
	 */
	public BangBangTrajectory1D getY()
	{
		return y;
	}
	
	
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
	
	
	/**
	 * returns time in seconds
	 */
	@Override
	public double getTotalTime()
	{
		return Math.max(x.getTotalTime(), y.getTotalTime());
	}
	
	
	@Override
	public PlanarCurve getPlanarCurve()
	{
		return getPlanarCurve(t -> {
			PosVelAcc stateX = x.getValuesAtTime(t);
			PosVelAcc stateY = y.getValuesAtTime(t);
			PosVelAcc2D result = new PosVelAcc2D();
			result.pos = Vector2.fromXY(stateX.pos, stateY.pos);
			result.vel = Vector2.fromXY(stateX.vel, stateY.vel);
			result.acc = Vector2.fromXY(stateX.acc, stateY.acc);
			return result;
		});
	}
	
	
	@Override
	public String toString()
	{
		return "x:\n" +
				x +
				"y:\n" +
				y;
	}
	
	
	@FunctionalInterface
	protected interface AlphaProvider
	{
		double getAlpha(double alpha);
	}
	
	
	protected void generateTrajectory(final IVector2 s0, final IVector2 s1, final IVector2 v0, final double vmax,
			final double acc, final AlphaProvider calc)
	{
		double inc = Math.PI / 8.0;
		double alpha = Math.PI / 4.0;
		
		// binary search, some iterations (fixed)
		while (inc > 1e-7)
		{
			generateTrajectory(s0, s1, v0, vmax, acc, calc.getAlpha(alpha));
			
			double diff = Math.abs(x.getTotalTime() - y.getTotalTime());
			if (diff < 0.0001)
			{
				break;
			}
			if (x.getTotalTime() > y.getTotalTime())
			{
				alpha = alpha - inc;
			} else
			{
				alpha = alpha + inc;
			}
			
			inc *= 0.5;
		}
	}
	
	
	private void generateTrajectory(final IVector2 s0, final IVector2 s1, final IVector2 v0, final double vmax,
			final double acc, final double alpha)
	{
		double cA = SumatraMath.cos(alpha);
		double sA = SumatraMath.sin(alpha);
		
		x = new BangBangTrajectory1D(s0.x(), s1.x(), v0.x(), vmax * cA, acc * cA);
		y = new BangBangTrajectory1D(s0.y(), s1.y(), v0.y(), vmax * sA, acc * sA);
	}
	
	protected static class PosVelAcc2D
	{
		protected Vector2 pos; // [m]
		protected Vector2 vel; // [m/s]
		protected Vector2 acc; // [m/s^2]
	}
	
	@FunctionalInterface
	protected interface StateProvider
	{
		PosVelAcc2D getState(double t);
	}
	
	
	protected PlanarCurve getPlanarCurve(final StateProvider stateProvider)
	{
		List<PlanarCurveSegment> segments = new ArrayList<>();
		
		List<Double> tQuery = new ArrayList<>();
		x.getParts().forEach(p -> tQuery.add(p.tEnd));
		y.getParts().forEach(p -> tQuery.add(p.tEnd));
		
		tQuery.sort(Double::compare);
		
		PosVelAcc2D state = stateProvider.getState(0);
		
		double tLast = 0;
		
		for (Double t : tQuery)
		{
			IVector2 pos = state.pos.multiply(1e3);
			IVector2 vel = state.vel.multiply(1e3);
			IVector2 acc = state.acc.multiply(1e3);
			if (!SumatraMath.isZero(t - tLast))
			{
				if (SumatraMath.isZero(acc.getLength2()))
				{
					segments.add(PlanarCurveSegment.fromFirstOrder(pos, vel, tLast, t));
				} else
				{
					segments.add(PlanarCurveSegment.fromSecondOrder(pos, vel, acc, tLast, t));
				}
			}
			
			state = stateProvider.getState(t);
			tLast = t;
		}
		
		if (segments.isEmpty())
		{
			segments.add(PlanarCurveSegment.fromPoint(Vector2.fromXY(x.initialPos, y.initialPos).multiply(1e3), 0, 1.0));
		}
		
		return new PlanarCurve(segments);
	}
	
	
	@Override
	public BangBangTrajectory2D mirrored()
	{
		return new BangBangTrajectory2D(x.mirrored(), y.mirrored());
	}
}
