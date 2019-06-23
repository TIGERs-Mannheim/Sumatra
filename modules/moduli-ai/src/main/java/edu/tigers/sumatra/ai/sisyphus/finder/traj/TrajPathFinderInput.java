/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 7, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.finder.traj;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles.IObstacle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.skillsystem.MovementCon;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TrajPathFinderInput
{
	private MovementCon				moveCon					= new MovementCon();
	private List<IDrawableShape>	debugShapes				= new ArrayList<>();
	private boolean					debug						= false;
	
	private double						collisionStepSize		= 0.125;
	private double						obstacleMargin			= 50;
	
	private IVector2					pos						= Vector2.ZERO_VECTOR;
	private IVector2					vel						= Vector2.ZERO_VECTOR;
	private double						orientation;
	private double						aVel;
	private IVector2					dest						= Vector2.ZERO_VECTOR;
	private List<IObstacle>			obstacles				= new ArrayList<>(0);
	private final long				timestamp;
	private double						targetAngle;
	private Random						rnd;
	private boolean					fastStop					= false;
	private double						minTrajTime				= 1;
	private double						collisionLookahead	= 2;
	
	
	/**
	 * @param timestamp
	 */
	public TrajPathFinderInput(final long timestamp)
	{
		this.timestamp = timestamp;
		rnd = new Random(timestamp);
	}
	
	
	/**
	 * @param input
	 * @param timestamp
	 */
	public TrajPathFinderInput(final TrajPathFinderInput input, final long timestamp)
	{
		this(timestamp);
		debug = input.debug;
		pos = input.pos;
		vel = input.vel;
		orientation = input.orientation;
		aVel = input.aVel;
		dest = input.dest;
		obstacles.addAll(input.obstacles);
		targetAngle = input.targetAngle;
		rnd = input.rnd;
		collisionStepSize = input.collisionStepSize;
		obstacleMargin = input.obstacleMargin;
		moveCon = input.getMoveCon();
		fastStop = input.isFastStop();
	}
	
	
	/**
	 * @param tBot
	 */
	public final void setTrackedBot(final ITrackedBot tBot)
	{
		pos = tBot.getPos();
		orientation = tBot.getAngle();
		vel = tBot.getVel();
		aVel = tBot.getaVel();
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
	 * @return the aVel
	 */
	public final double getaVel()
	{
		return aVel;
	}
	
	
	/**
	 * @param aVel the aVel to set
	 */
	public final void setaVel(final double aVel)
	{
		this.aVel = aVel;
	}
	
	
	/**
	 * @param debugShapes the debugShapes to set
	 */
	public final void setDebugShapes(final List<IDrawableShape> debugShapes)
	{
		this.debugShapes = debugShapes;
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
	
	
	/**
	 * @param collisionStepSize the collisionStepSize to set
	 */
	public void setCollisionStepSize(final double collisionStepSize)
	{
		this.collisionStepSize = collisionStepSize;
	}
	
	
	/**
	 * @return
	 */
	public double getObstacleMargin()
	{
		return obstacleMargin;
	}
	
	
	/**
	 * @param obstacleMargin the obstacleMargin to set
	 */
	public final void setObstacleMargin(final double obstacleMargin)
	{
		this.obstacleMargin = obstacleMargin;
	}
	
	
	/**
	 * @return the fastStop
	 */
	public boolean isFastStop()
	{
		return fastStop;
	}
	
	
	/**
	 * @param fastStop the fastStop to set
	 */
	public void setFastStop(final boolean fastStop)
	{
		this.fastStop = fastStop;
	}
	
	
	/**
	 * @return the moveCon
	 */
	public MovementCon getMoveCon()
	{
		return moveCon;
	}
	
	
	/**
	 * @param moveCon the moveCon to set
	 */
	public void setMoveCon(final MovementCon moveCon)
	{
		this.moveCon = moveCon;
	}
	
	
	/**
	 * @return the minTrajTime
	 */
	public double getMinTrajTime()
	{
		return minTrajTime;
	}
	
	
	/**
	 * @param minTrajTime the minTrajTime to set
	 */
	public void setMinTrajTime(final double minTrajTime)
	{
		this.minTrajTime = minTrajTime;
	}
	
	
	/**
	 * @return the collisionLookahead
	 */
	public double getCollisionLookahead()
	{
		return collisionLookahead;
	}
	
	
	/**
	 * @param collisionLookahead the collisionLookahead to set
	 */
	public void setCollisionLookahead(final double collisionLookahead)
	{
		this.collisionLookahead = collisionLookahead;
	}
}
