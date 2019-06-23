/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Data holder for pathplanning information
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TrajPathFinderInput
{
	private MoveConstraints	moveConstraints	= null;
	
	private double				collisionStepSize	= 0.125;
	
	private IVector2			pos					= Vector2.ZERO_VECTOR;
	private IVector2			vel					= Vector2.ZERO_VECTOR;
	private double				orientation;
	private IVector2			dest					= Vector2.ZERO_VECTOR;
	
	private List<IObstacle>	obstacles			= new ArrayList<>(0);
	private final long		timestamp;
	private double				targetAngle;
	private Random				rnd;
	
	
	/**
	 * @param timestamp current time [ns]
	 */
	public TrajPathFinderInput(final long timestamp)
	{
		this.timestamp = timestamp;
		rnd = new Random(timestamp);
	}
	
	
	/**
	 * @param input to be copied
	 * @param timestamp current time [ns]
	 */
	public TrajPathFinderInput(final TrajPathFinderInput input, final long timestamp)
	{
		this(timestamp);
		pos = input.pos;
		vel = input.vel;
		orientation = input.orientation;
		dest = input.dest;
		obstacles.addAll(input.obstacles);
		targetAngle = input.targetAngle;
		rnd = input.rnd;
		collisionStepSize = input.collisionStepSize;
		moveConstraints = input.getMoveConstraints();
	}
	
	
	/**
	 * @param tBot
	 */
	public final void setTrackedBot(final ITrackedBot tBot)
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
