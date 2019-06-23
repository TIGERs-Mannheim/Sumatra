/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trajectory;

import org.apache.commons.lang.Validate;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.planarcurve.PlanarCurve;
import edu.tigers.sumatra.trajectory.BangBangTrajectory1D.PosVelAcc;


/**
 * Asynchronous Bang Bang Trajectory for two dimensions.
 * X and Y are not synchronized in this version. The trajectory tries
 * to get on the line defined by target position and primary direction first.
 * 
 * @author AndreR
 */
@Persistent
public class BangBangTrajectory2DAsync extends BangBangTrajectory2D
{
	private IVector2 initialPos;
	private double rotation;
	
	
	@SuppressWarnings("unused")
	private BangBangTrajectory2DAsync()
	{
	}
	
	
	/**
	 * @param initialPos [m]
	 * @param finalPos [m]
	 * @param initialVel [m/s]
	 * @param maxVel [m/s]
	 * @param maxAcc [m/s^2]
	 * @param primaryDirection primary direction vector - must not be zero
	 */
	public BangBangTrajectory2DAsync(final IVector2 initialPos, final IVector2 finalPos,
			final IVector2 initialVel, final double maxVel, final double maxAcc, final IVector2 primaryDirection)
	{
		generateTrajectory(initialPos, finalPos, initialVel, maxVel, maxAcc, primaryDirection);
	}
	
	
	private BangBangTrajectory2DAsync(IVector2 initialPos, double rotation, final BangBangTrajectory1D x,
			final BangBangTrajectory1D y)
	{
		super(x, y);
		this.initialPos = initialPos;
		this.rotation = rotation;
	}
	
	
	private void generateTrajectory(final IVector2 s0, final IVector2 s1, final IVector2 v0, final double vmax,
			final double acc, final IVector2 primaryDirection)
	{
		Validate.notNull(primaryDirection);
		Validate.isTrue(!primaryDirection.isZeroVector(), "zero primary direction vector");
		
		initialPos = s0;
		rotation = primaryDirection.getAngle();
		IVector2 startToTarget = s1.subtractNew(s0).turn(-rotation);
		IVector2 v0Rotated = v0.turnNew(-rotation);
		
		generateTrajectory(Vector2f.ZERO_VECTOR, startToTarget, v0Rotated, vmax, acc,
				alpha -> alpha + ((AngleMath.PI_HALF - alpha) * 0.5));
	}
	
	
	public double getTotalTimeToPrimaryDirection()
	{
		return getY().getTotalTime();
	}
	
	
	@Override
	public Vector2 getPositionMM(final double t)
	{
		return super.getPositionMM(t).turn(rotation).add(initialPos.multiplyNew(1e3));
	}
	
	
	@Override
	public Vector2 getPosition(final double t)
	{
		return super.getPosition(t).turn(rotation).add(initialPos);
	}
	
	
	@Override
	public Vector2 getVelocity(final double t)
	{
		return super.getVelocity(t).turn(rotation);
	}
	
	
	@Override
	public Vector2 getAcceleration(final double t)
	{
		return super.getAcceleration(t).turn(rotation);
	}
	
	
	@Override
	public String toString()
	{
		return "x:\n" + getX() +
				"y:\n" + getY() +
				"initialPos:\n" + initialPos +
				"rotation:\n" + rotation;
	}
	
	
	@Override
	public PlanarCurve getPlanarCurve()
	{
		return getPlanarCurve(t -> {
			PosVelAcc stateX = getX().getValuesAtTime(t);
			PosVelAcc stateY = getY().getValuesAtTime(t);
			PosVelAcc2D result = new PosVelAcc2D();
			result.pos = Vector2.fromXY(stateX.pos, stateY.pos).turn(rotation).add(initialPos);
			result.vel = Vector2.fromXY(stateX.vel, stateY.vel).turn(rotation);
			result.acc = Vector2.fromXY(stateX.acc, stateY.acc).turn(rotation);
			return result;
		});
	}
	
	
	@Override
	public BangBangTrajectory2DAsync mirrored()
	{
		return new BangBangTrajectory2DAsync(initialPos.multiplyNew(-1), rotation, getX().mirrored(),
				getY().mirrored());
	}
}
