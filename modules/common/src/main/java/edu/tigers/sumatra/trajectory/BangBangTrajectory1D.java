/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.trajectory;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.SumatraMath;


/**
 * Bang Bang Trajectory for one dimension.
 * 
 * @author AndreR
 */
@Persistent
public class BangBangTrajectory1D implements ITrajectory<Double>
{
	private final List<BBTrajectoryPart> parts = new ArrayList<>();
	
	double initialPos; // [m]
	double finalPos; // [m]
	double initialVel; // [m/s]
	double maxAcc; // [m/s^2]
	double maxVel; // [m/s]
	
	
	BangBangTrajectory1D()
	{
		initialPos = 0;
		finalPos = 0;
		initialVel = 0;
		maxAcc = 0;
		maxVel = 0;
	}
	
	
	/**
	 * @param initialPos Initial position [m]
	 * @param finalPos Final position [m]
	 * @param initialVel Initial velocity [m/s]
	 * @param maxVel Maximum velocity [m/s]
	 * @param maxAcc Maximum acceleration [m/s^2]
	 */
	public BangBangTrajectory1D(final double initialPos, final double finalPos,
			final double initialVel, final double maxVel, final double maxAcc)
	{
		this.initialPos = initialPos;
		this.finalPos = finalPos;
		this.initialVel = initialVel;
		this.maxAcc = maxAcc;
		this.maxVel = maxVel;
		
		generateTrajectory(initialPos, initialVel, finalPos, maxVel, maxAcc);
	}
	
	
	/**
	 * @param i
	 * @return
	 */
	BBTrajectoryPart getPart(final int i)
	{
		return parts.get(i);
	}
	
	
	void generateTrajectory(final double x0, final double xd0, final double xTrg, final double xdMax,
			final double xddMax)
	{
		parts.clear();
		
		double sAtZeroAcc = velChangeToZero(x0, xd0, xddMax);
		
		if (sAtZeroAcc <= xTrg)
		{
			double sEnd = velTriToZero(x0, xd0, xdMax, xddMax);
			
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
			double sEnd = velTriToZero(x0, xd0, -xdMax, xddMax);
			
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
	
	
	private double velChangeToZero(final double s0, final double v0, final double aMax)
	{
		double a;
		
		if (0 >= v0)
		{
			a = aMax;
		} else
		{
			a = -aMax;
		}
		
		double t = -v0 / a;
		return s0 + (0.5 * v0 * t);
	}
	
	
	private double velTriToZero(final double s0, final double v0, final double v1, final double aMax)
	{
		double a1;
		double a2;
		
		if (v1 >= v0)
		{
			a1 = aMax;
			a2 = -aMax;
		} else
		{
			a1 = -aMax;
			a2 = aMax;
		}
		
		double t1 = (v1 - v0) / a1;
		double s1 = s0 + (0.5 * (v0 + v1) * t1);
		
		double t2 = -v1 / a2;
		return s1 + (0.5 * v1 * t2);
	}
	
	
	private void calcTri(final double s0, final double v0, final double s2, final double a, final boolean isPos)
	{
		final double t2;
		final double v1;
		final double t1;
		final double acc;
		final double s1;
		
		if (isPos)
		{
			// + -
			double sq = ((a * (s2 - s0)) + (0.5 * v0 * v0)) / (a * a);
			if (sq > 0.0)
			{
				t2 = SumatraMath.sqrt(sq);
			} else
			{
				t2 = 0;
			}
			v1 = a * t2;
			t1 = (v1 - v0) / a;
			acc = a;
			s1 = s0 + ((v0 + v1) * 0.5 * t1);
		} else
		{
			// - +
			double sq = ((a * (s0 - s2)) + (0.5 * v0 * v0)) / (a * a);
			if (sq > 0.0f)
			{
				t2 = SumatraMath.sqrt(sq);
			} else
			{
				t2 = 0;
			}
			v1 = -a * t2;
			t1 = (v1 - v0) / -a;
			acc = -a;
			s1 = s0 + ((v0 + v1) * 0.5 * t1);
		}
		
		parts.add(new BBTrajectoryPart(t1, acc, v0, s0));
		parts.add(new BBTrajectoryPart(t1 + t2, -acc, v1, s1));
	}
	
	
	private void calcTrapz(final double s0, final double v0, final double v1, final double s3, final double aMax)
	{
		double a1;
		double a3;
		double t1;
		double t2;
		double t3;
		double v2;
		double s1;
		double s2;
		
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
		
		s1 = s0 + (0.5 * (v0 + v1) * t1);
		s2 = s3 - (0.5 * v2 * t3);
		t2 = (s2 - s1) / v1;
		
		parts.add(new BBTrajectoryPart(t1, a1, v0, s0));
		parts.add(new BBTrajectoryPart(t1 + t2, 0, v1, s1));
		parts.add(new BBTrajectoryPart(t1 + t2 + t3, a3, v2, s2));
	}
	
	
	protected PosVelAcc getValuesAtTime(final double tt)
	{
		double trajTime = Math.max(0, tt);
		PosVelAcc result = new PosVelAcc();
		BBTrajectoryPart piece = parts.get(0);
		double tPieceStart = 0;
		double t;
		
		if (trajTime >= getTotalTime())
		{
			// requested time beyond final element
			BBTrajectoryPart lastPart = parts.get(parts.size() - 1);
			t = lastPart.tEnd - parts.get(parts.size() - 2).tEnd;
			result.acc = 0;
			result.vel = 0;
			result.pos = lastPart.s0 + (lastPart.v0 * t) + (0.5 * lastPart.acc * t * t);
			return result;
		}
		
		for (BBTrajectoryPart part : parts)
		{
			piece = part;
			if (trajTime < piece.tEnd)
			{
				break;
			}
			tPieceStart = piece.tEnd;
		}
		
		t = trajTime - tPieceStart;
		result.acc = piece.acc;
		result.vel = piece.v0 + (piece.acc * t);
		result.pos = piece.s0 + (piece.v0 * t) + (0.5 * piece.acc * t * t);
		
		return result;
	}
	
	
	@Override
	public Double getPosition(final double t)
	{
		return getValuesAtTime(t).pos;
	}
	
	
	@Override
	public Double getPositionMM(final double t)
	{
		return getPosition(t) * 1000.0;
	}
	
	
	@Override
	public Double getVelocity(final double t)
	{
		return getValuesAtTime(t).vel;
	}
	
	
	@Override
	public Double getAcceleration(final double t)
	{
		return getValuesAtTime(t).acc;
	}
	
	
	@Override
	public double getTotalTime()
	{
		return parts.get(parts.size() - 1).tEnd;
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("Input: initialPos, finalPos, initialVel,	maxAcc, maxBrk, maxVel:\n");
		sb.append(String.format("%03.15f %03.15f %03.15f %03.15f %03.15f%n%nParts:%n", initialPos, finalPos,
				initialVel, maxAcc, maxVel));
		for (BBTrajectoryPart part : parts)
		{
			sb.append(String.format("%03.15f %03.15f %03.15f %03.15f%n", part.s0, part.v0, part.acc, part.tEnd));
		}
		
		return sb.toString();
	}
	
	
	public List<BBTrajectoryPart> getParts()
	{
		return parts;
	}
	
	
	@Override
	public BangBangTrajectory1D mirrored()
	{
		return new BangBangTrajectory1D(-initialPos, -finalPos, -initialVel, maxVel, maxAcc);
	}
	
	/**
	 * Part of trajectory
	 */
	@Persistent
	static class BBTrajectoryPart
	{
		/** */
		double tEnd;
		/** */
		double acc;
		/** */
		double v0;
		/** */
		double s0;
		
		
		@SuppressWarnings("unused")
		private BBTrajectoryPart()
		{
			// required for Berkeley
		}
		
		
		/**
		 * @param tEnd
		 * @param acc
		 * @param v0
		 * @param s0
		 */
		private BBTrajectoryPart(final double tEnd, final double acc, final double v0, final double s0)
		{
			this.tEnd = tEnd;
			this.acc = acc;
			this.v0 = v0;
			this.s0 = s0;
		}
	}
	
	@Persistent
	static class PosVelAcc
	{
		double pos; // [m]
		double vel; // [m/s]
		double acc; // [m/s^2]
	}
}
