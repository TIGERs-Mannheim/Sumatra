/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Data holder for pathplanning information
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TrajPathFinderInput
{
	private final long timestamp;
	private MoveConstraints moveConstraints = null;
	
	private IVector2 pos = Vector2f.ZERO_VECTOR;
	private IVector2 vel = Vector2f.ZERO_VECTOR;
	private double collisionStepSize = 0.125;
	private IVector2 dest = Vector2f.ZERO_VECTOR;
	private double orientation;
	private List<IObstacle> obstacles = new ArrayList<>(0);
	private double targetAngle;
	private Random rnd;
	
	
	/**
	 * @param timestamp current time [ns]
	 */
	public TrajPathFinderInput(final long timestamp)
	{
		this.timestamp = timestamp;
		rnd = new Random(timestamp);
	}
	
	
	/**
	 * Create the input from a tracked bot and the current trajectory
	 * 
	 * @param timestamp
	 * @param tBot
	 * @return
	 */
	public static TrajPathFinderInput fromBotOrTrajectory(long timestamp, ITrackedBot tBot)
	{
		TrajPathFinderInput input = new TrajPathFinderInput(timestamp);
		input.setTrackedBot(tBot);
		return input;
	}
	
	
	/**
	 * @param tBot
	 */
	private void setTrackedBot(final ITrackedBot tBot)
	{
		pos = tBot.getPos();
		orientation = tBot.getOrientation();
		vel = tBot.getVel();
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
	
	
	public void setCollisionStepSize(final double collisionStepSize)
	{
		this.collisionStepSize = collisionStepSize;
	}
	
	
	/**
	 * @return the collisionStepSize
	 */
	public double getCollisionStepSize()
	{
		return collisionStepSize;
	}
	
	
	public MoveConstraints getMoveConstraints()
	{
		return moveConstraints;
	}
	
	
	public void setMoveConstraints(final MoveConstraints moveConstraints)
	{
		this.moveConstraints = moveConstraints;
	}
}
