/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.06.2015
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;


/**
 * Bang Bang Trajectory for two dimensions.
 * This trajectory class can handle different acceleration and deceleration values.
 * 
 * @author AndreR
 */
@Persistent
public class BangBangTrajectory2D implements ITrajectory2D
{
	private BangBangTrajectory1D	x;
	private BangBangTrajectory1D	y;
	
	
	@SuppressWarnings("unused")
	private BangBangTrajectory2D()
	{
	}
	
	
	/**
	 * @param initialPos
	 * @param finalPos
	 * @param initialVel
	 * @param maxAcc
	 * @param maxBrk
	 * @param maxVel
	 */
	public BangBangTrajectory2D(final IVector2 initialPos, final IVector2 finalPos,
			final IVector2 initialVel, final float maxAcc, final float maxBrk, final float maxVel)
	{
		generateTrajectory(initialPos, finalPos, initialVel, maxAcc, maxBrk, maxVel);
	}
	
	
	/**
	 * @param x
	 * @param y
	 */
	public BangBangTrajectory2D(final BangBangTrajectory1D x, final BangBangTrajectory1D y)
	{
		this.x = x;
		this.y = y;
	}
	
	
	private void generateTrajectory(final IVector2 s0, final IVector2 s1, final IVector2 v0, final float acc,
			final float brk,
			final float vmax)
	{
		float inc = SumatraMath.PI / 8.0f;
		float alpha = SumatraMath.PI / 4.0f;
		
		// binary search, some iterations (fixed)
		while (inc > 0.001f)
		{
			float cA = SumatraMath.cos(alpha);
			float sA = SumatraMath.sin(alpha);
			
			x = new BangBangTrajectory1D(s0.x(), s1.x(), v0.x(), acc * cA, brk * cA, vmax * cA);
			y = new BangBangTrajectory1D(s0.y(), s1.y(), v0.y(), acc * sA, brk * sA, vmax * sA);
			
			if (x.getTotalTime() > y.getTotalTime())
			{
				alpha = alpha - inc;
			} else
			{
				alpha = alpha + inc;
			}
			
			inc *= 0.5f;
		}
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
	public Vector2 getPosition(final float t)
	{
		return new Vector2(x.getPosition(t), y.getPosition(t)).multiply(1000);
	}
	
	
	@Override
	public Vector2 getVelocity(final float t)
	{
		return new Vector2(x.getVelocity(t), y.getVelocity(t));
	}
	
	
	@Override
	public Vector2 getAcceleration(final float t)
	{
		return new Vector2(x.getAcceleration(t), y.getAcceleration(t));
	}
	
	
	/**
	 * returns time in seconds
	 */
	@Override
	public float getTotalTime()
	{
		return SumatraMath.max(x.getTotalTime(), y.getTotalTime());
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("x:\n");
		sb.append(x.toString());
		sb.append("y:\n");
		sb.append(y.toString());
		return sb.toString();
	}
}
