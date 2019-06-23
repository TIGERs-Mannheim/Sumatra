/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 7, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.IObstacle;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TrajPathFinderInput
{
	private List<IDrawableShape>	debugShapes						= new ArrayList<>();
	private boolean					debug								= false;
	
	/** number of different times on trajectories to try per subPoint */
	private int							numPoints2TryOnTraj			= 10;
	/** number of different subNodes to try per iteration. They will be sampled randomly from available. */
	private int							numNodes2TryPerIteration	= 3;
	/** max number of points to use. Careful: exponential performance! */
	private int							maxSubPoints					= 3;
	/** If no path was found, force direct path after this time [s] */
	private float						forcePathAfter					= 0.2f;
	/** current path will be forced for this time. This is also the maximum processing time per call [s] */
	private float						trajOffset						= 0.0f;
	
	private float						maxProcessingTime				= 0.1f;
	
	private IVector2					pos								= Vector2.ZERO_VECTOR;
	private IVector2					vel								= Vector2.ZERO_VECTOR;
	private float						orientation;
	private float						aVel;
	private IVector2					dest								= Vector2.ZERO_VECTOR;
	private List<IObstacle>			obstacles						= new ArrayList<>(0);
	private long						timestamp						= System.nanoTime();
	private float						targetAngle;
	
	
	/**
	 * 
	 */
	public TrajPathFinderInput()
	{
	}
	
	
	/**
	 * @param input
	 */
	public TrajPathFinderInput(final TrajPathFinderInput input)
	{
		debugShapes.addAll(input.debugShapes);
		debug = input.debug;
		numPoints2TryOnTraj = input.numPoints2TryOnTraj;
		numNodes2TryPerIteration = input.numNodes2TryPerIteration;
		maxSubPoints = input.maxSubPoints;
		forcePathAfter = input.forcePathAfter;
		pos = input.pos;
		vel = input.vel;
		orientation = input.orientation;
		aVel = input.aVel;
		dest = input.dest;
		obstacles.addAll(input.obstacles);
		timestamp = input.timestamp;
		targetAngle = input.targetAngle;
	}
	
	
	/**
	 * Return the time past since bot pos/vel were last set.
	 * 
	 * @return [s]
	 */
	public float getAgeOfData()
	{
		return (System.nanoTime() - timestamp) / 1e9f;
	}
	
	
	/**
	 * @param tBot
	 */
	public final void setTrackedBot(final TrackedTigerBot tBot)
	{
		pos = tBot.getPos();
		orientation = tBot.getAngle();
		vel = tBot.getVel();
		aVel = tBot.getaVel();
		timestamp = System.nanoTime();
	}
	
	
	/**
	 * @return the numPoints2TryOnTraj
	 */
	public final int getNumPoints2TryOnTraj()
	{
		return numPoints2TryOnTraj;
	}
	
	
	/**
	 * @param numPoints2TryOnTraj the numPoints2TryOnTraj to set
	 */
	public final void setNumPoints2TryOnTraj(final int numPoints2TryOnTraj)
	{
		this.numPoints2TryOnTraj = numPoints2TryOnTraj;
	}
	
	
	/**
	 * @return the maxSubPoints
	 */
	public final int getMaxSubPoints()
	{
		return maxSubPoints;
	}
	
	
	/**
	 * @param maxSubPoints the maxSubPoints to set
	 */
	public final void setMaxSubPoints(final int maxSubPoints)
	{
		this.maxSubPoints = maxSubPoints;
	}
	
	
	/**
	 * @return the pos
	 */
	public final IVector2 getPos()
	{
		return pos;
	}
	
	
	/**
	 * @param pos the pos to set
	 */
	public final void setPos(final IVector2 pos)
	{
		this.pos = pos;
	}
	
	
	/**
	 * @return the vel
	 */
	public final IVector2 getVel()
	{
		return vel;
	}
	
	
	/**
	 * @param vel the vel to set
	 */
	public final void setVel(final IVector2 vel)
	{
		this.vel = vel;
	}
	
	
	/**
	 * @return the dest
	 */
	public final IVector2 getDest()
	{
		return dest;
	}
	
	
	/**
	 * @param dest the dest to set
	 */
	public final void setDest(final IVector2 dest)
	{
		this.dest = dest;
	}
	
	
	/**
	 * @return the obstacles
	 */
	public final List<IObstacle> getObstacles()
	{
		return obstacles;
	}
	
	
	/**
	 * @param obstacles the obstacles to set
	 */
	public final void setObstacles(final List<IObstacle> obstacles)
	{
		this.obstacles = obstacles;
	}
	
	
	/**
	 * @return the debug
	 */
	public final boolean isDebug()
	{
		return debug;
	}
	
	
	/**
	 * @param debug the debug to set
	 */
	public final void setDebug(final boolean debug)
	{
		this.debug = debug;
	}
	
	
	/**
	 * @return the debugShapes
	 */
	public final List<IDrawableShape> getDebugShapes()
	{
		return debugShapes;
	}
	
	
	/**
	 * @return the orientation
	 */
	public final float getTargetAngle()
	{
		return targetAngle;
	}
	
	
	/**
	 * @param orientation the orientation to set
	 */
	public final void setTargetAngle(final float orientation)
	{
		targetAngle = orientation;
	}
	
	
	/**
	 * @return the orientation
	 */
	public final float getOrientation()
	{
		return orientation;
	}
	
	
	/**
	 * @param orientation the orientation to set
	 */
	public final void setOrientation(final float orientation)
	{
		this.orientation = orientation;
	}
	
	
	/**
	 * @return the aVel
	 */
	public final float getaVel()
	{
		return aVel;
	}
	
	
	/**
	 * @param aVel the aVel to set
	 */
	public final void setaVel(final float aVel)
	{
		this.aVel = aVel;
	}
	
	
	/**
	 * @return the forcePathAfter
	 */
	public final float getForcePathAfter()
	{
		return forcePathAfter;
	}
	
	
	/**
	 * @param forcePathAfter the forcePathAfter to set
	 */
	public final void setForcePathAfter(final float forcePathAfter)
	{
		this.forcePathAfter = forcePathAfter;
	}
	
	
	/**
	 * @return the trajOffset
	 */
	public final float getTrajOffset()
	{
		return trajOffset;
	}
	
	
	/**
	 * @param trajOffset the trajOffset to set
	 */
	public final void setTrajOffset(final float trajOffset)
	{
		this.trajOffset = trajOffset;
	}
	
	
	/**
	 * @return the numNodes2TryPerIteration
	 */
	public final int getNumNodes2TryPerIteration()
	{
		return numNodes2TryPerIteration;
	}
	
	
	/**
	 * @param numNodes2TryPerIteration the numNodes2TryPerIteration to set
	 */
	public final void setNumNodes2TryPerIteration(final int numNodes2TryPerIteration)
	{
		this.numNodes2TryPerIteration = numNodes2TryPerIteration;
	}
	
	
	/**
	 * @param debugShapes the debugShapes to set
	 */
	public final void setDebugShapes(final List<IDrawableShape> debugShapes)
	{
		this.debugShapes = debugShapes;
	}
	
	
	/**
	 * @return the maxProcessingTime
	 */
	public final float getMaxProcessingTime()
	{
		return maxProcessingTime;
	}
	
	
	/**
	 * @param maxProcessingTime the maxProcessingTime to set
	 */
	public final void setMaxProcessingTime(final float maxProcessingTime)
	{
		this.maxProcessingTime = maxProcessingTime;
	}
	
}
