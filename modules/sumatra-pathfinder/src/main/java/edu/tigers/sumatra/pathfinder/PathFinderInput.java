/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder;

import edu.tigers.sumatra.bot.IMoveConstraints;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import edu.tigers.sumatra.trajectory.ITrajectory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Data holder for path planning information
 */
public class PathFinderInput
{
	private static final double COLLISION_STEP_SIZE = 0.125;

	private final long timestamp;
	private IMoveConstraints moveConstraints = null;

	private IVector2 pos = Vector2f.ZERO_VECTOR;
	private IVector2 vel = Vector2f.ZERO_VECTOR;
	private IVector2 dest = Vector2f.ZERO_VECTOR;
	private double orientation;
	private List<IObstacle> obstacles = new ArrayList<>(0);
	private double targetAngle;
	private Random rnd;
	private boolean debug = false;


	/**
	 * @param timestamp current time [ns]
	 */
	private PathFinderInput(final long timestamp)
	{
		this.timestamp = timestamp;
		rnd = new Random(timestamp);
	}


	/**
	 * Create the input from a tracked bot
	 *
	 * @param timestamp
	 * @param state
	 * @return
	 */
	public static PathFinderInput fromBot(long timestamp, State state)
	{
		PathFinderInput input = new PathFinderInput(timestamp);
		input.setPos(state.getPos());
		input.setOrientation(state.getOrientation());
		input.setVel(state.getVel2());
		return input;
	}


	/**
	 * Create the input from the current trajectory
	 *
	 * @param timestamp
	 * @param trajectory
	 * @return
	 */
	public static PathFinderInput fromTrajectory(long timestamp, ITrajectory<IVector3> trajectory)
	{
		PathFinderInput input = new PathFinderInput(timestamp);
		IVector3 pos3 = trajectory.getPositionMM(0);
		input.setPos(pos3.getXYVector());
		input.setOrientation(pos3.z());
		input.setVel(trajectory.getVelocity(0).getXYVector());
		return input;
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
	 * @return the orientation
	 */
	public final double getTargetAngle()
	{
		return targetAngle;
	}


	/**
	 * @param orientation the orientation to set
	 */
	public final void setTargetAngle(final double orientation)
	{
		targetAngle = orientation;
	}


	/**
	 * @return the orientation
	 */
	public final double getOrientation()
	{
		return orientation;
	}


	/**
	 * @param orientation the orientation to set
	 */
	public final void setOrientation(final double orientation)
	{
		this.orientation = orientation;
	}


	/**
	 * @return the timestamp
	 */
	public long getTimestamp()
	{
		return timestamp;
	}


	/**
	 * @return
	 */
	public Random getRnd()
	{
		return rnd;
	}


	/**
	 * @param rnd the rnd to set
	 */
	public void setRnd(final Random rnd)
	{
		this.rnd = rnd;
	}


	/**
	 * @return the collisionStepSize
	 */
	public double getCollisionStepSize()
	{
		return COLLISION_STEP_SIZE;
	}


	public IMoveConstraints getMoveConstraints()
	{
		return moveConstraints;
	}


	public void setMoveConstraints(final IMoveConstraints mc)
	{
		this.moveConstraints = mc;
	}


	public boolean isDebug()
	{
		return debug;
	}


	public void setDebug(final boolean debug)
	{
		this.debug = debug;
	}
}
