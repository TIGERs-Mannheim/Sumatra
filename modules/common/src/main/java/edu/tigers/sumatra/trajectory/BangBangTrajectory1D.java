/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.06.2015
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.trajectory;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.SumatraMath;


/**
 * Bang Bang Trajectory for one dimension.
 * This trajectory class can handle different acceleration and deceleration values.
 * 
 * @author AndreR
 */
@Persistent
public class BangBangTrajectory1D implements ITrajectory<Double>
{
	/** */
	public static final int BANG_BANG_TRAJECTORY_1D_PARTS = 4;
	
	/** */
	@Persistent
	public static class BBTrajectoryPart
	{
		/** */
		public double	tEnd;
		/** */
		public double	acc;
		/** */
		public double	v0;
		/** */
		public double	s0;
	}
	
	@Persistent
	protected static class PosVelAcc
	{
		public double	pos;
		public double	vel;
		public double	acc;
	}
	
	private final BBTrajectoryPart[] parts = new BBTrajectoryPart[BANG_BANG_TRAJECTORY_1D_PARTS];
	
	
	{
		for (int i = 0; i < parts.length; i++)
		{
			parts[i] = new BBTrajectoryPart();
		}
	}
	
	
	protected double	initialPos;
	protected double	finalPos;
	protected double	initialVel;
	protected double	maxAcc;
	protected double	maxBrk;
	protected double	maxVel;
							
							
	@SuppressWarnings("unused")
	protected BangBangTrajectory1D()
	{
		initialPos = 0;
		finalPos = 0;
		initialVel = 0;
		maxAcc = 0;
		maxBrk = 0;
		maxVel = 0;
	}
	
	
	/**
	 * @param initialPos Initial position
	 * @param finalPos Final position
	 * @param initialVel Initial velocity
	 * @param maxAcc Maximum acceleration
	 * @param maxBrk Maximum deceleration
	 * @param maxVel Maximum velocity
	 */
	public BangBangTrajectory1D(final double initialPos, final double finalPos,
			final double initialVel, final double maxAcc, final double maxBrk, final double maxVel)
	{
		this.initialPos = initialPos;
		this.finalPos = finalPos;
		this.initialVel = initialVel;
		this.maxAcc = maxAcc;
		this.maxBrk = maxBrk;
		this.maxVel = maxVel;
		double sDiff = finalPos - initialPos;
		
		generateTrajectory(sDiff, initialVel, maxAcc, maxBrk, maxVel);
		
		calcVelPos(initialPos, initialVel);
	}
	
	
	/**
	 * @param i
	 * @return
	 */
	public BBTrajectoryPart getPart(final int i)
	{
		return parts[i];
	}
	
	
	private double dist(final double v0, final double v1, final double acc)
	{
		double t = Math.abs(v0 - v1) / acc;
		return 0.5 * (v0 + v1) * t;
	}
	
	
	protected void generateTrajectory(final double s, final double v0, final double acc, final double brk,
			final double vmax)
	{
		if (s < 0)
		{
			generateTrajectory(-s, -v0, acc, brk, vmax);
			
			for (BBTrajectoryPart p : parts)
			{
				p.acc = -p.acc;
			}
			
			return;
		}
		
		double T1 = 0;
		double T2 = 0;
		double T3 = 0;
		double T4 = 0;
		double[] Q;
		
		if (vmax == 0.0)
		{
			// type = TRAJ_TYPE_A;
			Q = new double[] { 0, 0, 0, 0 };
		} else
		{
			if (v0 >= 0)
			{
				double s_n = dist(v0, 0, brk);
				
				if (s_n > s)
				{
					double s_npn = dist(v0, 0, brk) + dist(0, -vmax, acc) + dist(-vmax, 0, brk);
					if (s_npn > s)
					{
						// Case G
						T1 = v0 / brk;
						T2 = vmax / acc;
						T4 = vmax / brk;
						T3 = ((v0 * T1) - (vmax * T4) - (vmax * T2) - (2 * s)) / (2.0 * vmax);
						Q = new double[] { -brk, -acc, 0, brk };
					} else
					{
						// Case F
						T1 = v0 / brk;
						T3 = (acc * ((v0 * v0) - (2 * brk * s))) / (brk * brk * (brk + acc));
						if (T3 <= 0.0)
						{
							T3 = 0.0;
							T2 = 0.0;
						} else
						{
							T3 = SumatraMath.sqrt(T3);
							double v1 = -brk * T3;
							T2 = -(((v1 * T3) + (v0 * T1)) - (2 * s)) / v1;
						}
						Q = new double[] { -brk, -acc, brk, 0 };
					}
				} else
				{
					if (v0 > vmax)
					{
						// Case C
						T1 = -(vmax - v0) / brk;
						T2 = -((v0 * v0) - (2 * brk * s)) / (2.0 * brk * vmax);
						T3 = vmax / brk;
						Q = new double[] { -brk, 0, -brk, 0 };
					} else
					{
						double s_pn = dist(v0, vmax, acc) + dist(vmax, 0, brk);
						if (s_pn > s)
						{
							// Case A
							T2 = SumatraMath.sqrt(((v0 * v0) + (2 * acc * s)) / (brk * (brk + acc)));
							double v1 = brk * T2;
							T1 = (v1 - v0) / acc;
							Q = new double[] { acc, -brk, 0, 0 };
						} else
						{
							// Case B
							T1 = (vmax - v0) / acc;
							T3 = vmax / brk;
							T2 = -(((vmax * T3) + ((vmax + v0) * T1)) - (2 * s)) / (2.0 * vmax);
							Q = new double[] { acc, 0, -brk, 0 };
						}
					}
				}
			} else
			{
				double s_npn = dist(v0, 0, brk) + dist(0, vmax, acc) + dist(vmax, 0, brk);
				if (s_npn > s)
				{
					// Case D
					T1 = -v0 / brk;
					T3 = (acc * ((v0 * v0) + (2 * brk * s))) / (brk * brk * (brk + acc));
					
					if (T3 <= 0.0)
					{
						T3 = 0.0;
						T2 = 0.0;
					} else
					{
						T3 = SumatraMath.sqrt(T3);
						double v1 = brk * T3;
						T2 = -(((v1 * T3) + (v0 * T1)) - (2 * s)) / v1;
					}
					Q = new double[] { brk, acc, -brk, 0 };
				} else
				{
					// Case E
					T1 = -v0 / brk;
					T2 = vmax / acc;
					T4 = vmax / brk;
					T3 = -(((vmax * T4) + (vmax * T2) + (v0 * T1)) - (2 * s)) / (2.0 * vmax);
					Q = new double[] { brk, acc, 0, -brk };
				}
			}
		}
		
		parts[0].acc = Q[0];
		parts[0].tEnd = T1;
		parts[1].acc = Q[1];
		parts[1].tEnd = T1 + T2;
		parts[2].acc = Q[2];
		parts[2].tEnd = T1 + T2 + T3;
		parts[3].acc = Q[3];
		parts[3].tEnd = T1 + T2 + T3 + T4;
	}
	
	
	protected void calcVelPos(final double s0, final double v0)
	{
		BBTrajectoryPart first = parts[0];
		
		first.v0 = v0;
		first.s0 = s0;
		
		for (int i = 1; i < BANG_BANG_TRAJECTORY_1D_PARTS; i++)
		{
			BBTrajectoryPart cur = parts[i];
			BBTrajectoryPart prev = parts[i - 1];
			
			double tStart = 0;
			if (i > 1)
			{
				tStart = parts[i - 2].tEnd;
			}
			
			double dT = prev.tEnd - tStart;
			
			cur.v0 = prev.v0 + (prev.acc * dT);
			cur.s0 = prev.s0 + (prev.v0 * dT) + (0.5f * prev.acc * dT * dT);
		}
	}
	
	
	protected PosVelAcc getValuesAtTime(final double tt)
	{
		double trajTime = Math.max(0, tt);
		PosVelAcc result = new PosVelAcc();
		int i;
		double t;
		BBTrajectoryPart piece = parts[0];
		double tPieceStart = 0;
		
		for (i = 0; i < BANG_BANG_TRAJECTORY_1D_PARTS; i++)
		{
			piece = parts[i];
			if (trajTime < piece.tEnd)
			{
				break;
			}
		}
		
		if (i == BANG_BANG_TRAJECTORY_1D_PARTS)
		{
			t = piece.tEnd - parts[BANG_BANG_TRAJECTORY_1D_PARTS - 2].tEnd; // trajectory complete, use end time
			result.acc = 0;
		} else
		{
			if (i > 0)
			{
				tPieceStart = parts[i - 1].tEnd;
			}
			
			t = trajTime - tPieceStart;
			result.acc = piece.acc;
		}
		
		result.vel = piece.v0 + (piece.acc * t);
		result.pos = piece.s0 + (piece.v0 * t) + (0.5f * piece.acc * t * t);
		
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
		return getPosition(t) * 1000;
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
		return parts[BANG_BANG_TRAJECTORY_1D_PARTS - 1].tEnd;
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("Input: initialPos, finalPos, initialVel,	maxAcc, maxBrk, maxVel:\n");
		sb.append(String.format("%03.15f %03.15f %03.15f %03.15f %03.15f %03.15f%n%nParts:%n", initialPos, finalPos,
				initialVel,
				maxAcc,
				maxBrk, maxVel));
		for (BBTrajectoryPart part : parts)
		{
			sb.append(String.format("%03.15f %03.15f %03.15f %03.15f%n", part.s0, part.v0, part.acc, part.tEnd));
		}
		
		return sb.toString();
	}
	
	
	/**
	 * @return
	 */
	public String getInitParams()
	{
		return String.format("%03.20f %03.20f %03.20f %03.20f %03.20f %03.20f", initialPos, finalPos,
				initialVel,
				maxAcc,
				maxBrk, maxVel);
	}
}
