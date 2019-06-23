/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.03.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.HermiteSpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.HermiteSpline2D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;


/**
 * Generates trajectories with hermite spline interpolations.
 * 
 * @author AndreR
 */
public class SplineTrajectoryGenerator
{
	private static final Logger	log							= Logger.getLogger(SplineTrajectoryGenerator.class.getName());
	private float						maxVelocity					= 1.0f;
	private float						maxAcceleration			= 1.0f;
	private float						maxAngularVelocity		= 1.0f;
	private float						maxAngularAcceleration	= 1.0f;
	private float						reducePathScore			= 0.0f;
	
	
	/**
	 */
	public SplineTrajectoryGenerator()
	{
	}
	
	
	/**
	 * Create a smoothed path.
	 * 
	 * @param pathIn Path points to cross. Must include start and end point.
	 * @param initialVelocityIn Velocity at start point.
	 * @param finalVelocityIn Velocity at end point.
	 * @param initWIn initial orientation
	 * @param finalWIn final orientation
	 * @param initWVelIn initial angular velocity
	 * @param finalWVelIn final angular velocity
	 * @return Smoothed path.
	 */
	public SplinePair3D create(final List<IVector2> pathIn, final IVector2 initialVelocityIn,
			final IVector2 finalVelocityIn,
			final float initWIn, final float finalWIn, final float initWVelIn, final float finalWVelIn)
	{
		
		if (pathIn.size() > 2)
		{
			boolean doubleFound = false;
			do
			{
				doubleFound = false;
				for (int i = 0; i < (pathIn.size() - 2); i++)
				{
					if ((pathIn.get(i).subtractNew(pathIn.get(i + 1)).getLength2() < 0.001) ||
							(pathIn.get(i).subtractNew(pathIn.get(i + 2)).getLength2() < 0.001))
					{
						pathIn.remove(i);
						doubleFound = true;
						break;
					}
					else if (pathIn.get(i + 1).subtractNew(pathIn.get(i + 2)).getLength2() < 0.001)
					{
						pathIn.remove(i + 1);
						doubleFound = true;
						break;
					}
				}
			} while (doubleFound && (pathIn.size() > 2));
		}
		// if (pathIn.size() == 2)
		// {
		// if (pathIn.get(0).subtractNew(pathIn.get(1)).getLength2() < 0.001)
		// {
		// pathIn.remove(0);
		// }
		// }
		List<IVector2> path = pathIn;
		IVector2 initialVelocity = initialVelocityIn;
		IVector2 finalVelocity = finalVelocityIn;
		float initW = initWIn;
		float finalW = finalWIn;
		float initWVel = initWVelIn;
		float finalWVel = finalWVelIn;
		
		List<PathPointInfo> points = preProcess(path, initialVelocity, finalVelocity);
		
		
		// first point processing
		PathPointInfo info = points.get(0);
		info.tangent = (new Vector2(initialVelocity));
		
		// intermediate point processing
		for (int i = 1; i < (points.size() - 1); i++)
		{
			info = points.get(i);
			
			// find velocity
			info.tangent = tangent(path, i);
			if (info.tangent.getLength2() > maxVelocity)
			{
				info.tangent = info.tangent.scaleToNew(maxVelocity);
			}
		}
		
		// last point processing
		info = points.get(points.size() - 1);
		info.tangent = new Vector2(finalVelocity);
		
		// now create splines and trajectories
		List<HermiteSpline2D> parts = new ArrayList<HermiteSpline2D>();
		
		float totalTime = 0;
		
		for (int i = 0; i < (points.size() - 1); i++)
		{
			PathPointInfo A = points.get(i);
			PathPointInfo B = points.get(i + 1);
			
			HermiteSpline2D part = generateSpline(A.point, B.point, A.tangent, B.tangent);
			totalTime += part.getEndTime();
			
			parts.add(part);
		}
		
		// generate single spline for rotation
		HermiteSpline rotateSpline = generateSpline(initW, finalW, initWVel, finalWVel, totalTime, true);
		List<HermiteSpline> rotateParts = new ArrayList<HermiteSpline>();
		rotateParts.add(rotateSpline);
		
		SplinePair3D result = new SplinePair3D();
		result.setPositionTrajectory(new HermiteSplineTrajectory2D(parts));
		result.setRotationTrajectory(new HermiteSplineTrajectory1D(rotateParts));
		return result;
	}
	
	
	private float T(final List<IVector2> path, final int i)
	{
		if (i == 0)
		{
			return 0;
		}
		return (float) (Math.sqrt(path.get(i).subtractNew(path.get(i - 1)).getLength2()) + T(path, i - 1));
	}
	
	
	/*
	 * nonuniform Catmull-Rom spline
	 * C'(t1) = (P1 - P0) / (t1 - t0) - (P2 - P0) / (t2 - t0) + (P2 - P1) / (t2 - t1)
	 * not usable for i=0 and i=size(path) - 1
	 * from
	 * http://stackoverflow.com/questions/9489736/catmull-rom-curve-with-no-cusps-and-no-self-intersections/19283471#
	 * 19283471
	 */
	
	private IVector2 tangent(final List<IVector2> path, final int i)
	{
		// if (path.get(i - 1).equals(path.get(i)) || path.get(i).equals(path.get(i + 1))
		// || path.get(i - 1).equals(path.get(i + 1)))
		// {
		// log.error("Two points are double in the spline!!!");
		// }
		IVector2 p1 = ((path.get(i).subtractNew(path.get(i - 1))).multiplyNew(multiplicatorChecker(path, i, i - 1)));
		IVector2 p2 = ((path.get(i + 1).subtractNew(path.get(i - 1)))
				.multiplyNew(multiplicatorChecker(path, i + 1, i - 1)));
		IVector2 p3 = ((path.get(i + 1).subtractNew(path.get(i))).multiplyNew(multiplicatorChecker(path, i + 1, i)));
		return p1.subtractNew(p2).addNew(p3);
	}
	
	
	private float multiplicatorChecker(final List<IVector2> path, final int i1, final int i2)
	{
		Float multiplicator = (1 / (T(path, i1) - T(path, i2)));
		if (multiplicator.isNaN())
		{
			log.error("Invalid spline");
			multiplicator = 0f;
		}
		return multiplicator;
	}
	
	
	private List<PathPointInfo> preProcess(final List<IVector2> path, final IVector2 initialVelocity,
			final IVector2 finalVelocity)
	{
		List<PathPointInfo> points = new ArrayList<PathPointInfo>();
		
		// Start Point
		PathPointInfo infoStart = new PathPointInfo(AVector2.ZERO_VECTOR, path.get(0), path.get(1));
		points.add(infoStart);
		
		// Intermediate Points
		for (int i = 1; i < (path.size() - 1); i++)
		{
			IVector2 A = path.get(i - 1);
			IVector2 B = path.get(i);
			IVector2 C = path.get(i + 1);
			
			PathPointInfo info = new PathPointInfo(A, B, C);
			float distance = B.subtractNew(A).getLength2() + B.subtractNew(C).getLength2();
			float score = (1.0f - info.normalAngle) * distance;
			
			if (score < reducePathScore)
			{
				// log.debug("Kicked point " + i + ", score = " + score);
				path.remove(i);
				i--;
			} else
			{
				points.add(info);
			}
		}
		
		// End Point
		PathPointInfo infoEnd = new PathPointInfo(path.get(path.size() - 2), path.get(path.size() - 1),
				AVector2.ZERO_VECTOR);
		points.add(infoEnd);
		
		// compute length of path
		points.get(0).totalLength = 0;
		for (int i = 1; i < points.size(); i++)
		{
			PathPointInfo p = points.get(i);
			
			p.totalLength = points.get(i - 1).totalLength + p.distanceToPrevious;
		}
		
		return points;
	}
	
	
	/**
	 * Generates a spline with maxVelocity and maxAcceleration using a simple iterative approach.
	 * 
	 * @param initialPos
	 * @param finalPos
	 * @param initialVelocity
	 * @param finalVelocity
	 * @return
	 */
	public HermiteSpline2D generateSpline(final IVector2 initialPos, final IVector2 finalPos,
			final IVector2 initialVelocity,
			final IVector2 finalVelocity)
	{
		IVector2 initVel = initialVelocity;
		IVector2 finalVel = finalVelocity;
		
		// catch some invalid velocities
		if (initVel.getLength2() > maxVelocity)
		{
			// need to tackle numeric issues or below while loop with become an endless loop!
			initVel = initVel.scaleToNew(maxVelocity - 0.1f);
		}
		
		if (finalVel.getLength2() > maxVelocity)
		{
			// need to tackle numeric issues or below while loop with become an endless loop!
			finalVel = finalVel.scaleToNew(maxVelocity - 0.1f);
		}
		
		assert maxVelocity > 0;
		
		// generate initial guess based on distance and max velocity
		float d = finalPos.subtractNew(initialPos).getLength2();
		// t will always be a too short time, that's good
		float t = d / maxVelocity;
		// and this will avoid nulls and NaNs in the following calculation
		t += 0.00001f;
		
		HermiteSpline2D spline = new HermiteSpline2D(initialPos, finalPos, initVel, finalVel, t);
		
		float velDiff = maxVelocity - spline.getMaxFirstDerivative();
		
		// optimize for velocity first
		while (velDiff < 0)
		{
			t += 0.1f;
			
			spline = new HermiteSpline2D(initialPos, finalPos, initVel, finalVel, t);
			
			velDiff = maxVelocity - spline.getMaxFirstDerivative();
			
			if (t > 60)
			{
				log.warn("Endless loop detected Velocity!!! " + spline.getEndTime() + ", initialPos: " + initialPos
						+ ", finalPos: "
						+ finalPos + ", initialVelocity: " + initialVelocity + ", finalVelocity: " + finalVelocity);
				break;
			}
		}
		
		// now optimize for acceleration
		float accDiff = maxAcceleration - spline.getMaxSecondDerivative();
		
		while (accDiff < 0)
		{
			t += 0.1f;
			
			spline = new HermiteSpline2D(initialPos, finalPos, initVel, finalVel, t);
			
			accDiff = maxAcceleration - spline.getMaxSecondDerivative();
			
			if (t > 60)
			{
				log.warn("Endless loop detected Acceleration!!! " + spline.getEndTime() + ", initialPos: " + initialPos
						+ ", finalPos: "
						+ finalPos + ", initialVelocity: " + initialVelocity + ", finalVelocity: " + finalVelocity);
				break;
			}
		}
		
		return spline;
	}
	
	
	/**
	 * Generates a spline with maxVelocity and maxAcceleration using a simple iterative approach.
	 * 
	 * @param initialPos
	 * @param finalPos
	 * @param initialVelocity
	 * @param finalVelocity
	 * @param t initial guess for time
	 * @param isAngle
	 * @return
	 */
	public HermiteSpline generateSpline(final float initialPos, float finalPos, final float initialVelocity,
			final float finalVelocity,
			float t, final boolean isAngle)
	{
		// catch some invalid velocities
		if (Math.abs(initialVelocity) > maxAngularVelocity)
		{
			maxAngularVelocity = Math.abs(initialVelocity);
		}
		
		if (Math.abs(finalVelocity) > maxAngularVelocity)
		{
			maxAngularVelocity = Math.abs(finalVelocity);
		}
		
		if (isAngle)
		{
			finalPos = initialPos + AngleMath.getShortestRotation(initialPos, finalPos);
		}
		
		HermiteSpline spline = new HermiteSpline(initialPos, finalPos, initialVelocity, finalVelocity, t);
		
		float velDiff = maxAngularVelocity - spline.getMaxFirstDerivative();
		
		// optimize for velocity first
		int timeout = 100;
		while ((velDiff < 0) && (timeout > 0))
		{
			t += 0.1f;
			
			spline = new HermiteSpline(initialPos, finalPos, initialVelocity, finalVelocity, t);
			
			velDiff = maxAngularVelocity - spline.getMaxFirstDerivative();
			
			timeout--;
		}
		
		// now optimize for acceleration
		float accDiff = maxAngularAcceleration - spline.getMaxSecondDerivative();
		
		while (accDiff < 0)
		{
			t += 0.1f;
			
			spline = new HermiteSpline(initialPos, finalPos, initialVelocity, finalVelocity, t);
			
			accDiff = maxAngularAcceleration - spline.getMaxSecondDerivative();
		}
		
		return spline;
	}
	
	
	/**
	 * Set parameters for position trajectory generation.
	 * 
	 * @param maxVel Maximum velocity for trajectory.
	 * @param acc Acceleration for trajectory.
	 */
	public void setPositionTrajParams(final float maxVel, final float acc)
	{
		setMaxVelocity(maxVel);
		maxAcceleration = acc;
	}
	
	
	/**
	 * Set parameters for rotation trajectory generation.
	 * 
	 * @param maxVel Maximum velocity for trajectory.
	 * @param acc Acceleration for trajectory.
	 */
	public void setRotationTrajParams(final float maxVel, final float acc)
	{
		maxAngularVelocity = maxVel;
		maxAngularAcceleration = acc;
	}
	
	
	/**
	 * Path points where (1-normalAngle)*(distanceToNext+distanceToPrevious) is below this score will be removed.
	 * 
	 * @param s score boundary
	 */
	public void setReducePathScore(final float s)
	{
		reducePathScore = s;
	}
	
	
	private static class PathPointInfo
	{
		
		
		protected PathPointInfo(final IVector2 A, final IVector2 B, final IVector2 C)
		{
			point = new Vector2(B);
			toPrevious = A.subtractNew(B);
			toNext = C.subtractNew(B);
			distanceToPrevious = toPrevious.getLength2();
			
			toPrevious.normalize();
			toNext.normalize();
			
			angle = GeoMath.angleBetweenVectorAndVector(toPrevious, toNext);
			normalAngle = angle / AngleMath.PI;
		}
		
		/** */
		public Vector2		point;
		/** */
		public Vector2		toPrevious;
		/** */
		public Vector2		toNext;
		
		
		/** */
		public IVector2	tangent;
		
		/** */
		public float		angle;
		/** */
		public float		normalAngle;
		
		/** */
		public float		distanceToPrevious;
		/** */
		public float		totalLength;
	}
	
	
	/**
	 * @param maxVelocity the maxVelocity to set
	 */
	public final void setMaxVelocity(final float maxVelocity)
	{
		this.maxVelocity = maxVelocity;
	}
}
