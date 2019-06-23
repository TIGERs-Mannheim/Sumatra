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


/**
 * Bang Bang Trajectory for one dimension.
 * This trajectory class can handle different acceleration and deceleration values.
 * 
 * @author AndreR
 */
@Persistent
public class BangBangTrajectory1D implements ITrajectory1D
{
	/** */
	public static final int	BANG_BANG_TRAJECTORY_1D_PARTS	= 4;
	
	/** */
	@Persistent
	public static class BBTrajectoryPart
	{
		/** */
		public float	tEnd;
		/** */
		public float	acc;
		/** */
		public float	v0;
		/** */
		public float	s0;
	}
	
	@Persistent
	protected static class PosVelAcc
	{
		public float	pos;
		public float	vel;
		public float	acc;
	}
	
	private BBTrajectoryPart[]	parts	= new BBTrajectoryPart[BANG_BANG_TRAJECTORY_1D_PARTS];
	
	{
		for (int i = 0; i < parts.length; i++)
		{
			parts[i] = new BBTrajectoryPart();
		}
	}
	
	
	protected float				initialPos;
	protected float				finalPos;
	protected float				initialVel;
	protected float				maxAcc;
	protected float				maxBrk;
	protected float				maxVel;
	
	
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
	public BangBangTrajectory1D(final float initialPos, final float finalPos,
			final float initialVel, final float maxAcc, final float maxBrk, final float maxVel)
	{
		this.initialPos = initialPos;
		this.finalPos = finalPos;
		this.initialVel = initialVel;
		this.maxAcc = maxAcc;
		this.maxBrk = maxBrk;
		this.maxVel = maxVel;
		float sDiff = finalPos - initialPos;
		
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
	
	
	private float dist(final float v0, final float v1, final float acc)
	{
		float t = Math.abs(v0 - v1) / acc;
		return 0.5f * (v0 + v1) * t;
	}
	
	
	protected void generateTrajectory(final float s, final float v0, final float acc, final float brk, final float vmax)
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
		
		float T1 = 0;
		float T2 = 0;
		float T3 = 0;
		float T4 = 0;
		float[] Q;
		
		if (vmax == 0.0f)
		{
			// type = TRAJ_TYPE_A;
			Q = new float[] { 0, 0, 0, 0 };
		}
		else
		{
			if (v0 >= 0)
			{
				float s_n = dist(v0, 0, brk);
				
				if (s_n > s)
				{
					float s_npn = dist(v0, 0, brk) + dist(0, -vmax, acc) + dist(-vmax, 0, brk);
					if (s_npn > s)
					{
						// Case G
						T1 = v0 / brk;
						T2 = vmax / acc;
						T4 = vmax / brk;
						T3 = ((v0 * T1) - (vmax * T4) - (vmax * T2) - (2 * s)) / (2 * vmax);
						Q = new float[] { -brk, -acc, 0, brk };
					}
					else
					{
						// Case F
						T1 = v0 / brk;
						T3 = (acc * ((v0 * v0) - (2 * brk * s))) / (brk * brk * (brk + acc));
						if (T3 <= 0.0f)
						{
							T3 = 0.0f;
							T2 = 0.0f;
						}
						else
						{
							T3 = SumatraMath.sqrt(T3);
							float v1 = -brk * T3;
							T2 = -(((v1 * T3) + (v0 * T1)) - (2 * s)) / v1;
						}
						Q = new float[] { -brk, -acc, brk, 0 };
					}
				}
				else
				{
					if (v0 > vmax)
					{
						// Case C
						T1 = -(vmax - v0) / brk;
						T2 = -((v0 * v0) - (2 * brk * s)) / (2 * brk * vmax);
						T3 = vmax / brk;
						Q = new float[] { -brk, 0, -brk, 0 };
					}
					else
					{
						float s_pn = dist(v0, vmax, acc) + dist(vmax, 0, brk);
						if (s_pn > s)
						{
							// Case A
							T2 = SumatraMath.sqrt(((v0 * v0) + (2 * acc * s)) / (brk * (brk + acc)));
							float v1 = brk * T2;
							T1 = (v1 - v0) / acc;
							Q = new float[] { acc, -brk, 0, 0 };
						}
						else
						{
							// Case B
							T1 = (vmax - v0) / acc;
							T3 = vmax / brk;
							T2 = -(((vmax * T3) + ((vmax + v0) * T1)) - (2 * s)) / (2 * vmax);
							Q = new float[] { acc, 0, -brk, 0 };
						}
					}
				}
			}
			else
			{
				float s_npn = dist(v0, 0, brk) + dist(0, vmax, acc) + dist(vmax, 0, brk);
				if (s_npn > s)
				{
					// Case D
					T1 = -v0 / brk;
					T3 = (acc * ((v0 * v0) + (2 * brk * s))) / (brk * brk * (brk + acc));
					
					if (T3 <= 0.0f)
					{
						T3 = 0.0f;
						T2 = 0.0f;
					}
					else
					{
						T3 = SumatraMath.sqrt(T3);
						float v1 = brk * T3;
						T2 = -(((v1 * T3) + (v0 * T1)) - (2 * s)) / v1;
					}
					Q = new float[] { brk, acc, -brk, 0 };
				}
				else
				{
					// Case E
					T1 = -v0 / brk;
					T2 = vmax / acc;
					T4 = vmax / brk;
					T3 = -(((vmax * T4) + (vmax * T2) + (v0 * T1)) - (2 * s)) / (2 * vmax);
					Q = new float[] { brk, acc, 0, -brk };
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
	
	
	protected void calcVelPos(final float s0, final float v0)
	{
		BBTrajectoryPart first = parts[0];
		
		first.v0 = v0;
		first.s0 = s0;
		
		for (int i = 1; i < BANG_BANG_TRAJECTORY_1D_PARTS; i++)
		{
			BBTrajectoryPart cur = parts[i];
			BBTrajectoryPart prev = parts[i - 1];
			
			float tStart = 0;
			if (i > 1)
			{
				tStart = parts[i - 2].tEnd;
			}
			
			float dT = prev.tEnd - tStart;
			
			cur.v0 = prev.v0 + (prev.acc * dT);
			cur.s0 = prev.s0 + (prev.v0 * dT) + (0.5f * prev.acc * dT * dT);
		}
	}
	
	
	protected PosVelAcc getValuesAtTime(final float tt)
	{
		float trajTime = Math.max(0, tt);
		PosVelAcc result = new PosVelAcc();
		int i;
		float t;
		BBTrajectoryPart piece = parts[0];
		float tPieceStart = 0;
		
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
		}
		else
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
	public float getPosition(final float t)
	{
		return getValuesAtTime(t).pos;
	}
	
	
	@Override
	public float getVelocity(final float t)
	{
		return getValuesAtTime(t).vel;
	}
	
	
	@Override
	public float getAcceleration(final float t)
	{
		return getValuesAtTime(t).acc;
	}
	
	
	@Override
	public float getTotalTime()
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
