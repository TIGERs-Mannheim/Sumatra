/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.06.2015
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.trajectory;

import java.util.Arrays;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.trajectory.BangBangTrajectory1D.BBTrajectoryPart;


/**
 * Bang Bang Trajectory for orientation.
 * This trajectory consumes the rest of voltage after a XY trajectory has already been computed.
 * 
 * @author AndreR
 */
@Persistent
public class BangBangTrajectoryOrient implements ITrajectory<Double>
{
	/** */
	public static final int			BANG_BANG_TRAJECTORY_ORIENT_PARTS	= 12;
																							
	private static final double	COMBINE_THRESHOLD							= 0.01;
																							
	@Persistent
	private static class LimitPart
	{
		public double	tEnd;
		public double	v0;
		public double	a0;
		public double	vt0;
	}
	
	@Persistent
	private static class PosVelAcc
	{
		public double	pos;
		public double	vel;
		public double	acc;
	}
	
	@Persistent
	private static class LimitPair
	{
		public LimitPart[]	parts;
		public int				vLimUsed;
	}
	
	private BBTrajectoryPart[]				parts;
													
	private final double						initialOrient;
	private final double						finalOrient;
	private final double						initialVel;
	private final double						maxAccRot;
	private final double						maxBrkRot;
	private final double						maxVelRot;
	private final BangBangTrajectory2D	trajXY;
	private final double						maxVelXY;
													
													
	/**
	 * Create uninitialized trajectory. Use create... methods!
	 */
	private BangBangTrajectoryOrient()
	{
		initialOrient = 0;
		finalOrient = 0;
		initialVel = 0;
		maxAccRot = 0;
		maxBrkRot = 0;
		maxVelRot = 0;
		trajXY = null;
		maxVelXY = 0;
	}
	
	
	/**
	 * @param initialOrient
	 * @param finalOrient
	 * @param initialVel
	 * @param maxAccRot
	 * @param maxBrkRot
	 * @param maxVelRot
	 * @param trajXY
	 * @param maxVelXY
	 * @return
	 */
	public static BangBangTrajectoryOrient direct(final double initialOrient, final double finalOrient,
			final double initialVel,
			final double maxAccRot, final double maxBrkRot,
			final double maxVelRot, final BangBangTrajectory2D trajXY, final double maxVelXY)
	{
		BangBangTrajectoryOrient traj = new BangBangTrajectoryOrient(initialOrient, finalOrient, initialVel, maxAccRot,
				maxBrkRot, maxVelRot, trajXY, maxVelXY);
		traj.createDirect();
		return traj;
	}
	
	
	/**
	 * @param initialOrient
	 * @param finalOrient
	 * @param initialVel
	 * @param maxAccRot
	 * @param maxBrkRot
	 * @param maxVelRot
	 * @param trajXY
	 * @param maxVelXY
	 * @return
	 */
	public static BangBangTrajectoryOrient circleAware(final double initialOrient, final double finalOrient,
			final double initialVel,
			final double maxAccRot, final double maxBrkRot,
			final double maxVelRot, final BangBangTrajectory2D trajXY, final double maxVelXY)
	{
		BangBangTrajectoryOrient traj = new BangBangTrajectoryOrient(initialOrient, finalOrient, initialVel, maxAccRot,
				maxBrkRot, maxVelRot, trajXY, maxVelXY);
		traj.createCircleAware();
		return traj;
	}
	
	
	/**
	 * Create a circle aware orientation trajectory with limits from XY trajectory.
	 * This variant knows about the +/- PI jumps of a circle and always choses the fastest trajectory to reach the target
	 * orientation.
	 * 
	 * @param initialOrient
	 * @param finalOrient
	 * @param initialVel
	 * @param maxAccRot
	 * @param maxBrkRot
	 * @param maxVelRot
	 * @param trajXY
	 * @param maxVelXY
	 */
	private BangBangTrajectoryOrient(final double initialOrient, final double finalOrient, final double initialVel,
			final double maxAccRot, final double maxBrkRot,
			final double maxVelRot, final BangBangTrajectory2D trajXY, final double maxVelXY)
	{
		this.initialOrient = initialOrient;
		this.finalOrient = finalOrient;
		this.initialVel = initialVel;
		this.maxAccRot = maxAccRot;
		this.maxBrkRot = maxBrkRot;
		this.maxVelRot = maxVelRot;
		this.trajXY = trajXY;
		this.maxVelXY = maxVelXY;
	}
	
	
	/**
	 * Create a direct trajectory with limits from XY trajectory.
	 * This method goes directly from initialPos to finalPos and is unaware of any circle problems.
	 */
	private void createDirect()
	{
		LimitPair limits = generateLimits(trajXY, maxAccRot, maxBrkRot, maxVelRot, maxVelXY);
		
		// printLimit(limits.parts);
		
		parts = generateTrajectoryOrient(limits, maxAccRot, maxBrkRot, initialVel, finalOrient - initialOrient);
		
		calcOrient(initialOrient);
	}
	
	
	/**
	 * Create a circle aware orientation trajectory with limits from XY trajectory.
	 * This variant knows about the +/- PI jumps of a circle and always choses the fastest trajectory to reach the target
	 * orientation.
	 */
	private void createCircleAware()
	{
		LimitPair limits = generateLimits(trajXY, maxAccRot, maxBrkRot, maxVelRot, maxVelXY);
		
		double sDiffShort = finalOrient - initialOrient;
		sDiffShort = AngleMath.normalizeAngle(sDiffShort);
		double sDiffLong;
		if (sDiffShort < 0)
		{
			sDiffLong = sDiffShort + (2 * SumatraMath.PI);
		} else if (sDiffShort > 0)
		{
			sDiffLong = sDiffShort - (2 * SumatraMath.PI);
		} else
		{
			sDiffLong = 0;
		}
		
		BBTrajectoryPart[] partsShort = generateTrajectoryOrient(limits, maxAccRot, maxBrkRot, initialVel, sDiffShort);
		BBTrajectoryPart[] partsLong = generateTrajectoryOrient(limits, maxAccRot, maxBrkRot, initialVel, sDiffLong);
		
		if (partsLong[partsLong.length - 1].tEnd < partsShort[partsShort.length - 1].tEnd)
		{
			parts = partsLong;
		} else
		{
			parts = partsShort;
		}
		
		calcOrient(initialOrient);
	}
	
	
	private PosVelAcc getValuesAtTime(final double tt)
	{
		double trajTime = Math.max(0, tt);
		PosVelAcc result = new PosVelAcc();
		int i;
		double t;
		BBTrajectoryPart piece = parts[0];
		double tPieceStart = 0;
		
		for (i = 0; i < parts.length; i++)
		{
			piece = parts[i];
			if (trajTime < piece.tEnd)
			{
				break;
			}
		}
		
		if (i == parts.length)
		{
			t = piece.tEnd - parts[parts.length - 2].tEnd; // trajectory complete, use end time
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
	
	
	private LimitPair generateLimits(final BangBangTrajectory2D pTrajXY, final double accW, final double brkW,
			final double wMax, double xyMax)
	{
		LimitPart[] limit = new LimitPart[BANG_BANG_TRAJECTORY_ORIENT_PARTS];
		LimitPart[] brkLimit = new LimitPart[BANG_BANG_TRAJECTORY_ORIENT_PARTS];
		
		for (int i = 0; i < BANG_BANG_TRAJECTORY_ORIENT_PARTS; i++)
		{
			limit[i] = new LimitPart();
			brkLimit[i] = new LimitPart();
		}
		
		xyMax += 0.1;
		
		double xyToW = wMax / xyMax;
		
		BangBangTrajectory1D pX = pTrajXY.getX();
		BangBangTrajectory1D pY = pTrajXY.getY();
		
		// first element is reserved in case the first velocity is above maximum
		
		// process first element
		double vBegin = SumatraMath.sqrt((pX.getPart(0).v0 * pX.getPart(0).v0) + (pY.getPart(0).v0 * pY.getPart(0).v0));
		limit[1].v0 = (xyMax - vBegin) * xyToW;
		if (limit[1].v0 < 1e-3f)
		{
			limit[1].v0 = 0;
		}
		
		// calculate limits from 2D trajectory
		int vLimUsed = 2;
		int x = 0;
		int y = 0;
		double t1 = -1;
		for (int i = 2; i < 10; i++)
		{
			double tNow;
			double vx;
			double vy;
			
			if (x == 4)
			{
				tNow = pY.getPart(y).tEnd;
				++y;
			} else if (y == 4)
			{
				tNow = pX.getPart(x).tEnd;
				++x;
			} else
			{
				if (pX.getPart(x).tEnd < pY.getPart(y).tEnd)
				{
					tNow = pX.getPart(x).tEnd;
					++x;
				} else
				{
					tNow = pY.getPart(y).tEnd;
					++y;
				}
			}
			
			if ((tNow - t1) > COMBINE_THRESHOLD)
			{
				t1 = tNow;
			} else
			{
				if ((tNow < COMBINE_THRESHOLD) && (pTrajXY.getTotalTime() > COMBINE_THRESHOLD))
				{
					continue;
				}
				
				--vLimUsed;
			}
			
			// System.out.println("x: " + x + ", y: " + y + ", vLimUsed: " + vLimUsed + "tNow: " + tNow + ", t1: " + t1);
			
			vx = pX.getVelocity(tNow);
			vy = pY.getVelocity(tNow);
			
			double wNow = (xyMax - SumatraMath.sqrt((vx * vx) + (vy * vy))) * xyToW;
			if (wNow < 1e-3f)
			{
				wNow = 0;
			}
			double wLast = limit[vLimUsed - 1].v0;
			double tLast = limit[vLimUsed - 2].tEnd;
			double aNow = (wNow - wLast) / (tNow - tLast);
			if ((tNow - tLast) == 0.0)
			{
				aNow = 0;
			}
			
			if (Math.abs(aNow) < 1e-3f)
			{
				wNow = wLast;
				aNow = 0;
			}
			
			double vt0 = wLast - (aNow * tLast);
			
			limit[vLimUsed - 1].tEnd = tNow;
			limit[vLimUsed].v0 = wNow;
			limit[vLimUsed - 1].a0 = aNow;
			limit[vLimUsed - 1].vt0 = vt0;
			
			++vLimUsed;
		}
		
		
		// check if xy limit is exceeded, prepend a zero element in vLimit
		if (limit[1].v0 < 0)
		{
			double tZero = -limit[1].vt0 / limit[1].a0;
			
			limit[0].tEnd = tZero;
			limit[1].v0 = 0;
		}
		
		limit[vLimUsed - 1].tEnd = limit[vLimUsed - 2].tEnd + 1e6f;
		limit[vLimUsed - 1].v0 = wMax;
		limit[vLimUsed - 1].vt0 = wMax;
		
		// printLimit(limit);
		
		// vLimUsed-1 is the last element (the extended wMax one)
		
		brkLimit[BANG_BANG_TRAJECTORY_ORIENT_PARTS - 1].tEnd = limit[vLimUsed - 1].tEnd;
		brkLimit[BANG_BANG_TRAJECTORY_ORIENT_PARTS - 1].v0 = limit[vLimUsed - 1].v0;
		brkLimit[BANG_BANG_TRAJECTORY_ORIENT_PARTS - 1].a0 = limit[vLimUsed - 1].a0;
		brkLimit[BANG_BANG_TRAJECTORY_ORIENT_PARTS - 1].vt0 = limit[vLimUsed - 1].vt0;
		
		int brkLimFree = BANG_BANG_TRAJECTORY_ORIENT_PARTS - 2;
		
		// we start with vLimUsed-2, it is the first one that can violate brkW
		for (int i = vLimUsed - 2; i >= 0; i--)
		{
			if (brkLimit[brkLimFree].tEnd == 0)
			{
				brkLimit[brkLimFree].tEnd = limit[i].tEnd;
			}
			
			double a1 = limit[i].a0;
			if (a1 >= -brkW)
			{
				brkLimit[brkLimFree].v0 = limit[i].v0;
				brkLimit[brkLimFree].a0 = limit[i].a0;
				brkLimit[brkLimFree].vt0 = limit[i].vt0;
				--brkLimFree;
				continue;
			}
			
			double v1 = limit[i + 1].v0;
			t1 = limit[i].tEnd;
			double vz1 = v1 + (brkW * t1);
			
			double vInter = 0;
			double tInter = 0;
			int j;
			for (j = i - 1; j > 0; j--)
			{
				double a0 = limit[j].a0;
				double vz0 = limit[j].vt0;
				
				tInter = -(vz0 - vz1) / (a0 + brkW);
				
				if ((tInter >= limit[j - 1].tEnd) && (tInter <= limit[j].tEnd))
				{
					// intersection found
					vInter = (a0 * tInter) + vz0;
					break;
				}
			}
			
			if (j > 0)
			{
				brkLimit[brkLimFree - 1].tEnd = tInter;
				brkLimit[brkLimFree].v0 = vInter;
				brkLimit[brkLimFree].a0 = -brkW;
				brkLimit[brkLimFree].vt0 = vz1;
				--brkLimFree;
				i = j + 1;
			} else
			{
				// no intersecting element, use t=0
				brkLimit[brkLimFree - 1].tEnd = 0;
				brkLimit[brkLimFree].v0 = vz1;
				brkLimit[brkLimFree].a0 = -brkW;
				brkLimit[brkLimFree].vt0 = vz1;
				// --brkLimFree;
				brkLimFree -= 2;
				break;
			}
		}
		
		// printLimit(brkLimit);
		
		// zero out limit
		limit = new LimitPart[BANG_BANG_TRAJECTORY_ORIENT_PARTS];
		vLimUsed = 0;
		for (int i = 0; i < BANG_BANG_TRAJECTORY_ORIENT_PARTS; i++)
		{
			limit[i] = new LimitPart();
		}
		
		for (int i = brkLimFree + 1; i < BANG_BANG_TRAJECTORY_ORIENT_PARTS; i++)
		{
			if (limit[vLimUsed].v0 == 0)
			{
				limit[vLimUsed].v0 = brkLimit[i].v0;
			}
			
			double a1 = brkLimit[i].a0;
			if (a1 <= accW)
			{
				limit[vLimUsed].tEnd = brkLimit[i].tEnd;
				limit[vLimUsed].a0 = brkLimit[i].a0;
				limit[vLimUsed].vt0 = brkLimit[i].vt0;
				++vLimUsed;
				continue;
			}
			
			t1 = 0;
			if (i > 0)
			{
				t1 = brkLimit[i - 1].tEnd;
			}
			
			double v1 = brkLimit[i].v0;
			double vz1 = v1 - (accW * t1);
			
			double vInter = 0.0;
			double tInter = 0.0;
			int j;
			for (j = i + 1; j < BANG_BANG_TRAJECTORY_ORIENT_PARTS; j++)
			{
				double a0 = brkLimit[j].a0;
				double vz0 = brkLimit[j].vt0;
				
				tInter = -(vz0 - vz1) / (a0 - accW);
				
				if ((tInter >= (brkLimit[j - 1].tEnd - 1e-4f)) && (tInter <= brkLimit[j].tEnd))
				{
					// intersection found
					vInter = (a0 * tInter) + vz0;
					break;
				}
			}
			
			if (j == BANG_BANG_TRAJECTORY_ORIENT_PARTS)
			{
				// no intersection found => Plan B
				limit[vLimUsed].tEnd = brkLimit[i].tEnd;
				limit[vLimUsed].a0 = brkLimit[i].a0;
				limit[vLimUsed].vt0 = brkLimit[i].vt0;
			} else
			{
				limit[vLimUsed].tEnd = tInter;
				limit[vLimUsed + 1].v0 = vInter;
				limit[vLimUsed].a0 = accW;
				limit[vLimUsed].vt0 = vz1;
				i = j - 1;
			}
			
			++vLimUsed;
		}
		
		LimitPair pair = new LimitPair();
		pair.parts = limit;
		pair.vLimUsed = vLimUsed;
		return pair;
	}
	
	
	private BBTrajectoryPart[] generateTrajectoryOrient(final LimitPair limitPair, final double accW, final double brkW,
			double w0, double s)
	{
		BBTrajectoryPart[] parts = new BBTrajectoryPart[BANG_BANG_TRAJECTORY_ORIENT_PARTS];
		for (int i = 0; i < parts.length; i++)
		{
			parts[i] = new BBTrajectoryPart();
		}
		
		LimitPart[] limit = limitPair.parts;
		int vLimUsed = limitPair.vLimUsed;
		double t = 0;
		
		int trajUsed = 0;
		parts[0].v0 = w0;
		
		if (Math.abs(s) < 1e-4f)
		{
			return parts;
		}
		
		double sDriven = 0;
		double sDrivenLast = 0;
		
		double tBrk = w0 / brkW;
		double sBrk = 0.5 * w0 * tBrk;
		
		boolean intersectionFound = false;
		if ((s >= 0) && (w0 <= 0))
		{
			// A
			s += sBrk;
			w0 = 0;
			t = -tBrk;
			parts[0].acc = brkW;
			parts[0].tEnd = t;
			parts[1].v0 = 0;
			++trajUsed;
		} else if ((s < 0) && (w0 > 0))
		{
			// B
			s -= sBrk;
			w0 = 0;
			t = tBrk;
			parts[0].acc = -brkW;
			parts[0].tEnd = t;
			parts[1].v0 = 0;
			++trajUsed;
		} else if (sBrk >= Math.abs(s))
		{
			if (w0 > 0)
			{
				// C1
				parts[0].acc = -brkW;
				s -= sBrk;
			} else
			{
				// C2
				parts[0].acc = brkW;
				s += sBrk;
			}
			w0 = 0;
			t = Math.abs(tBrk);
			parts[0].tEnd = t;
			parts[1].v0 = 0;
			++trajUsed;
		} else
		{
			if (w0 > 0)
			{
				// D1
				parts[0].acc = -brkW;
			} else
			{
				// D2
				parts[0].acc = brkW;
			}
			
			// check for backward intersection: t=0, v0=w0, a0=+-brk
			double a0 = parts[0].acc;
			double v0 = w0;
			double vInter = 0;
			double tInter = 0;
			int i;
			for (i = vLimUsed - 1; i >= 1; i--)
			{
				// calculate intersection point
				double a1 = limit[i].a0;
				double vz1 = limit[i].vt0;
				
				if (a0 > 0)
				{
					a1 *= -1;
					vz1 *= -1;
				}
				
				if (a0 == a1)
				{
					// there is no intersection points if the lines have the same slope
					continue;
				}
				
				tInter = -(v0 - vz1) / (a0 - a1);
				
				double tLeft = 0;
				if (i > 0)
				{
					tLeft = limit[i - 1].tEnd;
				}
				
				if ((i == 1) && (v0 == vz1))
				{
					vInter = v0;
					intersectionFound = true;
					break;
				}
				
				if ((tInter >= (tLeft - 2e-4f)) && (tInter <= limit[i].tEnd) && (tInter > 0))
				{
					vInter = (a0 * tInter) + v0;
					intersectionFound = true;
					break;
				}
			}
			
			if (intersectionFound)
			{
				// intersection found
				// E
				double sTmp = 0.5 * (w0 + vInter) * tInter;
				sDriven += sTmp;
				w0 = vInter;
				t = tInter;
				parts[0].tEnd = t;
				parts[1].v0 = w0;
				++trajUsed;
			}
		}
		
		if (!intersectionFound)
		{
			if ((s - sDriven) > 0)
			{
				// F1
				parts[trajUsed].acc = accW;
			} else
			{
				// F2
				parts[trajUsed].acc = -accW;
			}
			
			// check for foward intersection: t=t, v0=w0, a0=+-acc
			double a0 = parts[trajUsed].acc;
			double v0 = w0;
			double tInter = 0.0;
			double vInter = 0.0;
			for (int i = 1; i < vLimUsed; i++)
			{
				double a1 = limit[i].a0;
				double vz1 = limit[i].vt0;
				
				if (a0 < 0)
				{
					a1 *= -1;
					vz1 *= -1;
				}
				
				if (a0 == a1)
				{
					continue;
				}
				
				double vz0 = v0 - (a0 * t);
				tInter = -(vz0 - vz1) / (a0 - a1);
				
				double tLeft = 0;
				if (i > 0)
				{
					tLeft = limit[i - 1].tEnd;
				}
				
				if ((tInter >= tLeft) && (tInter <= (limit[i].tEnd + 1e-4f)) && (tInter > (t - 1e-4f)))
				{
					if (tInter < t)
					{
						tInter = t;
					}
					vInter = (a0 * tInter) + vz0;
					break;
				}
			}
			
			double tLeft = 0;
			if (trajUsed > 0)
			{
				tLeft = parts[trajUsed - 1].tEnd;
			}
			
			double sTmp = 0.5 * (w0 + vInter) * (tInter - tLeft);
			sDrivenLast = sDriven;
			sDriven += sTmp;
			w0 = vInter;
			t = tInter;
			parts[trajUsed].tEnd = t;
			parts[trajUsed + 1].v0 = w0;
			++trajUsed;
		}
		
		int i;
		for (i = 0; i < (vLimUsed - 1); i++)
		{
			double tLeft = 0;
			if (i > 0)
			{
				tLeft = limit[i - 1].tEnd;
			}
			
			if ((parts[trajUsed - 1].tEnd >= tLeft) && (parts[trajUsed - 1].tEnd <= limit[i].tEnd))
			{
				if (s < 0)
				{
					parts[trajUsed].acc = -limit[i].a0;
				} else
				{
					parts[trajUsed].acc = limit[i].a0;
				}
				
				break;
			}
		}
		
		// printParts(parts);
		
		for (; i <= vLimUsed; i++)
		{
			// printParts(parts);
			
			// central point
			double tc = parts[trajUsed - 1].tEnd;
			double vc = parts[trajUsed].v0;
			
			double tBrk1 = Math.abs(vc) / brkW;
			double sBrk1 = 0.5 * vc * tBrk1;
			
			double sBrkNow = sDriven + sBrk1;
			
			// System.out.println("tc: " + tc + ", vc: " + vc + ", tBrk1: " + tBrk1 + "sBrk1: " + sBrk1 + ", sBrkNow: "
			// + sBrkNow + ", sDriven: " + sDriven);
			
			if (((s >= 0) && (sBrkNow > s)) || ((s <= 0) && (sBrkNow < s)))
			{
				double a1 = parts[trajUsed - 1].acc;
				double v0 = parts[trajUsed - 1].v0;
				double t0 = 0;
				if (trajUsed > 1)
				{
					t0 = parts[trajUsed - 2].tEnd;
				}
				
				double a2;
				if (s < 0)
				{
					a2 = brkW;
				} else
				{
					a2 = -brkW;
				}
				
				double d = s - sDrivenLast;
				double t1;
				
				if (Math.abs(a1) < 5e-4f)
				{
					if (Math.abs(v0) < 1e-3f)
					{
						t1 = 0;
					} else
					{
						t1 = ((v0 * v0) + (2 * a2 * d)) / (2.0 * a2 * v0);
					}
				} else
				{
					double C = a2 - a1;
					double sqr = a2 * C * ((v0 * v0) + (2 * a1 * d));
					if (sqr > 0)
					{
						t1 = -(SumatraMath.sqrt(sqr) + (C * v0)) / (a1 * C);
						t1 = Math.abs(t1);
					} else
					{
						t1 = Math.abs(v0 / a1);
					}
				}
				
				double v1 = (t1 * a1) + v0;
				double s1 = (v0 + v1) * 0.5 * t1;
				
				// System.out.println("sDrivenLast: " + sDrivenLast + ", s:" + s + ", t0: " + t0 + ", v0: " + v0 + ", a1: "
				// + a1 + ", t1: " + t1 + ", s1: " + s1 + ", v1: " + v1);
				sDrivenLast = sDriven;
				sDriven += s1;
				
				parts[trajUsed - 1].tEnd = t0 + t1;
				parts[trajUsed].v0 = v1;
				parts[trajUsed].acc = a2;
				
				double t2 = -v1 / a2;
				
				parts[trajUsed].tEnd = t0 + t1 + t2;
				
				++trajUsed;
				
				break;
			}
			
			// right point
			double tr = limit[i].tEnd;
			double vr;
			double ar;
			
			if (i < (vLimUsed - 1))
			{
				vr = limit[i + 1].v0;
				ar = limit[i + 1].a0;
			} else
			{
				vr = limit[i].v0;
				ar = 0;
			}
			
			if (s < 0)
			{
				vr *= -1;
				ar *= -1;
			}
			
			double t2 = tr - tc;
			double s2 = 0.5 * (vr + vc) * t2;
			sDrivenLast = sDriven;
			sDriven += s2;
			
			parts[trajUsed].tEnd = tr;
			if (trajUsed < (BANG_BANG_TRAJECTORY_ORIENT_PARTS - 1))
			{
				parts[trajUsed + 1].v0 = vr;
				parts[trajUsed + 1].acc = ar;
			} else
			{
				break;
			}
			
			++trajUsed;
		}
		
		// printParts(parts);
		
		return Arrays.copyOf(parts, trajUsed);
	}
	
	
	private void calcOrient(final double s0)
	{
		BBTrajectoryPart first = parts[0];
		
		first.s0 = s0;
		
		for (int i = 1; i < parts.length; i++)
		{
			BBTrajectoryPart cur = parts[i];
			BBTrajectoryPart prev = parts[i - 1];
			
			double tStart = 0;
			if (i > 1)
			{
				tStart = parts[i - 2].tEnd;
			}
			
			double dT = prev.tEnd - tStart;
			
			cur.s0 = prev.s0 + (prev.v0 * dT) + (0.5f * prev.acc * dT * dT);
		}
	}
	
	
	/**
	 * @param i
	 * @return
	 */
	public BBTrajectoryPart getPart(final int i)
	{
		return parts[i];
	}
	
	
	/**
	 * @return
	 */
	public int getNumParts()
	{
		return parts.length;
	}
	
	
	@Override
	public Double getPositionMM(final double t)
	{
		return AngleMath.normalizeAngle(getValuesAtTime(t).pos);
	}
	
	
	@Override
	public Double getPosition(final double t)
	{
		return AngleMath.normalizeAngle(getValuesAtTime(t).pos);
	}
	
	
	/**
	 * @param t
	 * @return
	 */
	public Double getPositionDirect(final double t)
	{
		return getValuesAtTime(t).pos;
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
		return parts[parts.length - 1].tEnd;
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("Input: initialOrient finalOrient initialVel maxAccRot maxBrkRot maxVelRot maxVelXY:\n");
		sb.append(String.format("%03.15f %03.15f %03.15f %03.15f %03.15f %03.15f %03.15f%n%nParts:%n", initialOrient,
				finalOrient,
				initialVel, maxAccRot, maxBrkRot, maxVelRot, maxVelXY));
		for (BBTrajectoryPart part : parts)
		{
			sb.append(String.format("%03.15f %03.15f %03.15f %03.15f%n", part.s0, part.v0, part.acc, part.tEnd));
		}
		sb.append(trajXY.toString());
		
		return sb.toString();
	}
	
	
	/**
	 * @return
	 */
	public String getInitParams()
	{
		return String.format("%03.20f %03.20f %03.20f %03.20f %03.20f %03.20f %03.20f", initialOrient,
				finalOrient,
				initialVel, maxAccRot, maxBrkRot, maxVelRot, maxVelXY);
	}
	
	
	// private void printParts(final BBTrajectoryPart[] parts)
	// {
	// StringBuilder sb = new StringBuilder();
	//
	// for (BBTrajectoryPart part : parts)
	// {
	// sb.append(String.format("%03.8f %03.8f %03.8f %03.8f\n", part.s0, part.v0, part.acc, part.tEnd));
	// }
	//
	// System.out.println(sb.toString());
	//
	// }
	
	
	/**
	 * @param limits
	 */
	public void printLimit(final LimitPart[] limits)
	{
		StringBuilder sb = new StringBuilder();
		
		for (LimitPart part : limits)
		{
			sb.append(String.format("%03.8f %03.8f %03.8f %03.8f%n", part.tEnd, part.v0, part.a0, part.vt0));
		}
		
		System.out.println(sb.toString());
	}
}
