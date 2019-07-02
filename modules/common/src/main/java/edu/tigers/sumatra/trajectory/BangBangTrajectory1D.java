/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.trajectory;

import java.util.Arrays;
import java.util.List;

import edu.tigers.sumatra.math.SumatraMath;


/**
 * Bang Bang Trajectory for one dimension.
 * 
 * @author AndreR
 */
public class BangBangTrajectory1D implements ITrajectory<Double>
{
	private BBTrajectoryPart[] parts = new BBTrajectoryPart[3];
	private int numParts = 0;
	
	float initialPos; // [m]
	float finalPos; // [m]
	float initialVel; // [m/s]
	float maxAcc; // [m/s^2]
	float maxVel; // [m/s]
	
	
	BangBangTrajectory1D()
	{
		initialPos = 0;
		finalPos = 0;
		initialVel = 0;
		maxAcc = 0;
		maxVel = 0;
		init();
	}
	
	
	/**
	 * @param initialPos Initial position [m]
	 * @param finalPos Final position [m]
	 * @param initialVel Initial velocity [m/s]
	 * @param maxVel Maximum velocity [m/s]
	 * @param maxAcc Maximum acceleration [m/s^2]
	 */
	public BangBangTrajectory1D(final float initialPos, final float finalPos,
			final float initialVel, final float maxVel, final float maxAcc)
	{
		this.initialPos = initialPos;
		this.finalPos = finalPos;
		this.initialVel = initialVel;
		this.maxAcc = maxAcc;
		this.maxVel = maxVel;
		
		init();
		generateTrajectory();
	}
	
	
	protected void init()
	{
		for (int i = 0; i < 3; i++)
		{
			parts[i] = new BBTrajectoryPart();
		}
	}
	
	
	/**
	 * @param i
	 * @return
	 */
	BBTrajectoryPart getPart(final int i)
	{
		return parts[i];
	}
	
	
	protected void generateTrajectory()
	{
		float x0 = initialPos;
		float xd0 = initialVel;
		float xTrg = finalPos;
		float xdMax = maxVel;
		float xddMax = maxAcc;
		float sAtZeroAcc = velChangeToZero(x0, xd0, xddMax);
		
		if (sAtZeroAcc <= xTrg)
		{
			float sEnd = velTriToZero(x0, xd0, xdMax, xddMax);
			
			if (sEnd >= xTrg)
			{
				// Triangular profile
				calcTri(x0, xd0, xTrg, xddMax, true);
			} else
			{
				// Trapezoidal profile
				calcTrapz(x0, xd0, xdMax, xTrg, xddMax);
			}
		} else
		{
			// even with a full brake we miss xTrg
			float sEnd = velTriToZero(x0, xd0, -xdMax, xddMax);
			
			if (sEnd <= xTrg)
			{
				// Triangular profile
				calcTri(x0, xd0, xTrg, xddMax, false);
			} else
			{
				// Trapezoidal profile
				calcTrapz(x0, xd0, -xdMax, xTrg, xddMax);
			}
		}
	}
	
	
	private float velChangeToZero(final float s0, final float v0, final float aMax)
	{
		float a;
		
		if (0 >= v0)
		{
			a = aMax;
		} else
		{
			a = -aMax;
		}
		
		float t = -v0 / a;
		return s0 + (0.5f * v0 * t);
	}
	
	
	private float velTriToZero(final float s0, final float v0, final float v1, final float aMax)
	{
		float a1;
		float a2;
		
		if (v1 >= v0)
		{
			a1 = aMax;
			a2 = -aMax;
		} else
		{
			a1 = -aMax;
			a2 = aMax;
		}
		
		float t1 = (v1 - v0) / a1;
		float s1 = s0 + (0.5f * (v0 + v1) * t1);
		
		float t2 = -v1 / a2;
		return s1 + (0.5f * v1 * t2);
	}
	
	
	private void calcTri(final float s0, final float v0, final float s2, final float a, final boolean isPos)
	{
		final float t2;
		final float v1;
		final float t1;
		final float acc;
		final float s1;
		
		if (isPos)
		{
			// + -
			float sq = ((a * (s2 - s0)) + (0.5f * v0 * v0)) / (a * a);
			if (sq > 0.0)
			{
				t2 = (float) SumatraMath.sqrt(sq);
			} else
			{
				t2 = 0;
			}
			v1 = a * t2;
			t1 = (v1 - v0) / a;
			acc = a;
			s1 = s0 + ((v0 + v1) * 0.5f * t1);
		} else
		{
			// - +
			float sq = ((a * (s0 - s2)) + (0.5f * v0 * v0)) / (a * a);
			if (sq > 0.0f)
			{
				t2 = (float) SumatraMath.sqrt(sq);
			} else
			{
				t2 = 0;
			}
			v1 = -a * t2;
			t1 = (v1 - v0) / -a;
			acc = -a;
			s1 = s0 + ((v0 + v1) * 0.5f * t1);
		}
		
		updatePart(0, t1, acc, v0, s0);
		updatePart(1, t1 + t2, -acc, v1, s1);
		numParts = 2;
	}
	
	
	private void updatePart(int i, float tEnd, float acc, float v0, float s0)
	{
		parts[i].tEnd = tEnd;
		parts[i].acc = acc;
		parts[i].v0 = v0;
		parts[i].s0 = s0;
	}
	
	
	private void calcTrapz(final float s0, final float v0, final float v1, final float s3, final float aMax)
	{
		float a1;
		float a3;
		float t1;
		float t2;
		float t3;
		float v2;
		float s1;
		float s2;
		
		if (v0 > v1)
		{
			a1 = -aMax;
		} else
		{
			a1 = aMax;
		}
		
		if (v1 > 0)
		{
			a3 = -aMax;
		} else
		{
			a3 = aMax;
		}
		
		t1 = (v1 - v0) / a1;
		v2 = v1;
		t3 = -v2 / a3;
		
		s1 = s0 + (0.5f * (v0 + v1) * t1);
		s2 = s3 - (0.5f * v2 * t3);
		t2 = (s2 - s1) / v1;
		
		updatePart(0, t1, a1, v0, s0);
		updatePart(1, t1 + t2, 0, v1, s1);
		updatePart(2, t1 + t2 + t3, a3, v2, s2);
		numParts = 3;
	}
	
	
	protected PosVelAcc getValuesAtTime(final double tt)
	{
		float trajTime = Math.max(0, (float) tt);
		PosVelAcc result = new PosVelAcc();
		BBTrajectoryPart piece = parts[0];
		float tPieceStart = 0;
		float t;
		
		if (trajTime >= getTotalTime())
		{
			// requested time beyond final element
			BBTrajectoryPart lastPart = parts[numParts - 1];
			t = lastPart.tEnd - parts[numParts - 2].tEnd;
			result.acc = 0;
			result.vel = 0;
			result.pos = lastPart.s0 + (lastPart.v0 * t) + (0.5f * lastPart.acc * t * t);
			return result;
		}
		
		for (int i = 0; i < numParts; i++)
		{
			piece = parts[i];
			if (trajTime < piece.tEnd)
			{
				break;
			}
			tPieceStart = piece.tEnd;
		}
		
		t = trajTime - tPieceStart;
		result.acc = piece.acc;
		result.vel = piece.v0 + (piece.acc * t);
		result.pos = piece.s0 + (piece.v0 * t) + (0.5f * piece.acc * t * t);
		
		return result;
	}
	
	
	@Override
	public Double getPosition(final double t)
	{
		return (double) getValuesAtTime(t).pos;
	}
	
	
	@Override
	public Double getPositionMM(final double t)
	{
		return getPosition(t) * 1000.0f;
	}
	
	
	@Override
	public Double getVelocity(final double t)
	{
		return (double) getValuesAtTime(t).vel;
	}
	
	
	@Override
	public Double getAcceleration(final double t)
	{
		return (double) getValuesAtTime(t).acc;
	}
	
	
	@Override
	public double getTotalTime()
	{
		return parts[numParts - 1].tEnd;
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("Input: initialPos, finalPos, initialVel,	maxAcc, maxBrk, maxVel:\n");
		sb.append(String.format("%03.15f %03.15f %03.15f %03.15f %03.15f%n%nParts:%n", initialPos, finalPos,
				initialVel, maxAcc, maxVel));
		for (int i = 0; i < numParts; i++)
		{
			BBTrajectoryPart part = parts[i];
			sb.append(String.format("%03.15f %03.15f %03.15f %03.15f%n", part.s0, part.v0, part.acc, part.tEnd));
		}
		
		return sb.toString();
	}
	
	
	public List<BBTrajectoryPart> getParts()
	{
		return Arrays.asList(Arrays.copyOf(parts, numParts));
	}
	
	
	@Override
	public BangBangTrajectory1D mirrored()
	{
		return new BangBangTrajectory1D(-initialPos, -finalPos, -initialVel, maxVel, maxAcc);
	}
	
	/**
	 * Part of trajectory
	 */
	static class BBTrajectoryPart
	{
		/** */
		float tEnd;
		/** */
		float acc;
		/** */
		float v0;
		/** */
		float s0;
		
		
		@SuppressWarnings("unused")
		private BBTrajectoryPart()
		{
			// required for Berkeley
		}
	}
	
	static class PosVelAcc
	{
		float pos; // [m]
		float vel; // [m/s]
		float acc; // [m/s^2]
	}
}
