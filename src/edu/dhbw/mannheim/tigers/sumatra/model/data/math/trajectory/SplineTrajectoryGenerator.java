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

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions.Function1dPoly;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions.IFunction1D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.HermiteSpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.HermiteSpline2D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;


/**
 * Generates trajectories with hermite spline interpolations.
 * 
 * @author AndreR
 */
public class SplineTrajectoryGenerator
{
	private static class PathPointInfo
	{
		/** */
		public Vector2	point;
		/** */
		public Vector2	toPrevious;
		/** */
		public Vector2	toNext;
		
		
		/** */
		public Vector2	tangent;
		
		/** */
		public float	angle;
		/** */
		public float	normalAngle;
		
		/** */
		public float	distanceToNearest;
		/** */
		public float	distanceToPrevious;
		/** */
		public float	totalLength;
		
		/** to be filled later */
		public float	velocity;
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private IFunction1D	normalAngleToSpeed		= Function1dPoly.constant(1);
	private float			maxVelocity					= 1.0f;
	private float			maxAcceleration			= 1.0f;
	private float			maxAngularVelocity		= 1.0f;
	private float			maxAngularAcceleration	= 1.0f;
	private float			reducePathScore			= 0.0f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public SplineTrajectoryGenerator()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Set parameters for position trajectory generation.
	 * 
	 * @param maxVel Maximum velocity for trajectory.
	 * @param acc Acceleration for trajectory.
	 */
	public void setPositionTrajParams(final float maxVel, final float acc)
	{
		maxVelocity = maxVel;
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
	 * Set parameters for spline generation.
	 * 
	 * @param normalAngleToSpeedFunc This function maps the angle of the neighbor vectors at a path point to the speed at
	 *           this point.
	 */
	public void setSplineParams(final IFunction1D normalAngleToSpeedFunc)
	{
		normalAngleToSpeed = normalAngleToSpeedFunc;
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
			if (pathIn.get(0).subtractNew(pathIn.get(1)).getLength2() < 0.001)
			{
				pathIn.remove(0);
			}
		}
		List<IVector2> path = pathIn;
		IVector2 initialVelocity = initialVelocityIn;
		IVector2 finalVelocity = finalVelocityIn;
		float initW = initWIn;
		float finalW = finalWIn;
		float initWVel = initWVelIn;
		float finalWVel = finalWVelIn;
		
		List<PathPointInfo> points = preProcess(path, initialVelocity, finalVelocity);
		// for (int i = 0; i < points.size(); i++)
		// {
		// if (points.get(i).distanceToNearest < 0.02)
		// {
		// path.remove(i);
		// points = preProcess(path, initialVelocity, finalVelocity);
		// break;
		// }
		// }
		
		// first point processing
		PathPointInfo info = points.get(0);
		info.velocity = initialVelocity.getLength2();
		if (((info.distanceToNearest * maxAcceleration) * 0.8) < info.velocity)
		{
			info.velocity = (info.distanceToNearest * maxAcceleration) * 0.8f;
		}
		info.tangent = (new Vector2(initialVelocity)).scaleTo(info.velocity);
		
		// intermediate point processing
		for (int i = 1; i < (points.size() - 1); i++)
		{
			info = points.get(i);
			
			// find velocity
			info.velocity = normalAngleToSpeed.eval(info.normalAngle);
			
			// limit tangent to avoid spline circles
			if (((info.distanceToNearest * maxAcceleration) * 0.8) < info.velocity)
			{
				info.velocity = (info.distanceToNearest * maxAcceleration) * 0.8f;
			}
			// scale tangent
			info.tangent.scaleTo(info.velocity * Sisyphus.curveSpeed);
		}
		
		// last point processing
		info = points.get(points.size() - 1);
		info.velocity = finalVelocity.getLength2();
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
	
	
	private List<PathPointInfo> preProcess(final List<IVector2> path, final IVector2 initialVelocity,
			final IVector2 finalVelocity)
	{
		// List<IVector2> newPath = new LinkedList<IVector2>();
		// float stepSize = 100;
		// for (int i = 1; i < path.size(); i++)
		// {
		// IVector2 previous = path.get(i - 1);
		// newPath.add(previous);
		// IVector2 node = path.get(i);
		// IVector2 previousToNode = node.subtractNew(previous);
		// float dist = previousToNode.getLength2();
		// while (dist > (stepSize * 2))
		// {
		// IVector2 intermediate = previous.addNew(previousToNode.scaleToNew(stepSize));
		// newPath.add(intermediate);
		// previous = intermediate;
		// previousToNode = node.subtractNew(previous);
		// dist = previousToNode.getLength2();
		// }
		// }
		// newPath.add(path.get(path.size() - 1));
		// path = newPath;
		
		List<PathPointInfo> points = new ArrayList<PathPointInfo>();
		
		PathPointInfo info = generateInfo(AVector2.ZERO_VECTOR, path.get(0), path.get(1));
		info.tangent = new Vector2(initialVelocity);
		points.add(info);
		
		for (int i = 1; i < (path.size() - 1); i++)
		{
			IVector2 A = path.get(i - 1);
			IVector2 B = path.get(i);
			IVector2 C = path.get(i + 1);
			
			info = generateInfo(A, B, C);
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
		
		info = generateInfo(path.get(path.size() - 2), path.get(path.size() - 1), AVector2.ZERO_VECTOR);
		info.tangent = new Vector2(finalVelocity);
		points.add(info);
		
		// compute length of path
		points.get(0).totalLength = 0;
		for (int i = 1; i < points.size(); i++)
		{
			PathPointInfo p = points.get(i);
			
			p.totalLength = points.get(i - 1).totalLength + p.distanceToPrevious;
		}
		
		return points;
	}
	
	
	private PathPointInfo generateInfo(final IVector2 A, final IVector2 B, final IVector2 C)
	{
		PathPointInfo info = new PathPointInfo();
		
		info.point = new Vector2(B);
		info.toPrevious = A.subtractNew(B);
		info.toNext = C.subtractNew(B);
		info.distanceToPrevious = info.toPrevious.getLength2();
		
		info.distanceToNearest = info.toNext.getLength2();
		if (info.toPrevious.getLength2() < info.distanceToNearest)
		{
			info.distanceToNearest = info.toPrevious.getLength2();
		}
		
		info.toPrevious.normalize();
		info.toNext.normalize();
		
		info.angle = GeoMath.angleBetweenVectorAndVector(info.toPrevious, info.toNext);
		info.normalAngle = info.angle / AngleMath.PI;
		
		// get tangential vector
		info.tangent = info.toNext.subtractNew(info.toPrevious);
		
		return info;
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
	private HermiteSpline2D generateSpline(final IVector2 initialPos, final IVector2 finalPos,
			final IVector2 initialVelocity,
			final IVector2 finalVelocity)
	{
		// catch some invalid velocities
		if (initialVelocity.getLength2() > maxVelocity)
		{
			maxVelocity = initialVelocity.getLength2();
		}
		
		if (finalVelocity.getLength2() > maxVelocity)
		{
			maxVelocity = finalVelocity.getLength2();
		}
		
		// generate initial guess based on distance and max velocity
		float d = finalPos.subtractNew(initialPos).getLength2();
		// t will always be a too short time, that's good
		float t = d / maxVelocity;
		// and this will avoid nulls and NaNs in the following calculation
		t += 0.00001f;
		
		HermiteSpline2D spline = new HermiteSpline2D(initialPos, finalPos, initialVelocity, finalVelocity, t);
		
		float velDiff = maxVelocity - spline.getMaxFirstDerivative();
		
		// optimize for velocity first
		while (velDiff < 0)
		{
			t += 0.1f;
			
			spline = new HermiteSpline2D(initialPos, finalPos, initialVelocity, finalVelocity, t);
			
			velDiff = maxVelocity - spline.getMaxFirstDerivative();
		}
		
		// now optimize for acceleration
		float accDiff = maxAcceleration - spline.getMaxSecondDerivative();
		
		while (accDiff < 0)
		{
			t += 0.1f;
			
			spline = new HermiteSpline2D(initialPos, finalPos, initialVelocity, finalVelocity, t);
			
			accDiff = maxAcceleration - spline.getMaxSecondDerivative();
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
		while (velDiff < 0)
		{
			t += 0.1f;
			
			spline = new HermiteSpline(initialPos, finalPos, initialVelocity, finalVelocity, t);
			
			velDiff = maxAngularVelocity - spline.getMaxFirstDerivative();
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
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Path points where (1-normalAngle)*(distanceToNext+distanceToPrevious) is below this score will be removed.
	 * 
	 * @param s score boundary
	 */
	public void setReducePathScore(final float s)
	{
		reducePathScore = s;
	}
}
