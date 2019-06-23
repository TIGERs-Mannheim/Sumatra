/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 20, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.ObstacleGenerator;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CatchBallInput
{
	private TrackedTigerBot					bot				= null;
	private TrackedBall						ball				= null;
	private DynamicPosition					receiver			= null;
	private WorldFrame						wFrame			= null;
	private final TrajPathFinderInput	finderInput		= new TrajPathFinderInput();
	private final ObstacleGenerator		obsGen			= new ObstacleGenerator();
	private List<IDrawableShape>			shapes			= new ArrayList<>();
	private Random								rnd				= new Random();
	private IVector2							lastDest;
	private boolean							debug				= false;
	private float								lastDist2Dest	= Float.MAX_VALUE;
	private TrajPath							lastPath			= null;
	
	
	private float								maxAngle			= AngleMath.PI_HALF - 0.2f;
	
	
	/**
	 * @param bot
	 * @param ball
	 * @param receiver
	 */
	public CatchBallInput(final TrackedTigerBot bot, final TrackedBall ball, final DynamicPosition receiver)
	{
		super();
		this.bot = bot;
		this.ball = ball;
		this.receiver = receiver;
		lastDest = ball.getPosByVel(0);
	}
	
	
	/**
	 * @return the bot
	 */
	public final TrackedTigerBot getBot()
	{
		return bot;
	}
	
	
	/**
	 * @param bot the bot to set
	 */
	public final void setBot(final TrackedTigerBot bot)
	{
		this.bot = bot;
	}
	
	
	/**
	 * @return the ball
	 */
	public final TrackedBall getBall()
	{
		return ball;
	}
	
	
	/**
	 * @param ball the ball to set
	 */
	public final void setBall(final TrackedBall ball)
	{
		this.ball = ball;
	}
	
	
	/**
	 * @return the receiver
	 */
	public final DynamicPosition getReceiver()
	{
		return receiver;
	}
	
	
	/**
	 * @param receiver the receiver to set
	 */
	public final void setReceiver(final DynamicPosition receiver)
	{
		this.receiver = receiver;
	}
	
	
	/**
	 * @return the shapes
	 */
	public final List<IDrawableShape> getShapes()
	{
		return shapes;
	}
	
	
	/**
	 * @param shapes the shapes to set
	 */
	public final void setShapes(final List<IDrawableShape> shapes)
	{
		this.shapes = shapes;
	}
	
	
	/**
	 * @return the lastDest
	 */
	public final IVector2 getLastDest()
	{
		if ((lastDest == null) && (ball != null))
		{
			lastDest = ball.getPosByVel(0);
		}
		return lastDest;
	}
	
	
	/**
	 * @param lastDest the lastDest to set
	 */
	public final void setLastDest(final IVector2 lastDest)
	{
		this.lastDest = lastDest;
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
	 * @return the obsGen
	 */
	public final ObstacleGenerator getObsGen()
	{
		return obsGen;
	}
	
	
	/**
	 * @return the rnd
	 */
	public final Random getRnd()
	{
		return rnd;
	}
	
	
	/**
	 * @param rnd the rnd to set
	 */
	public final void setRnd(final Random rnd)
	{
		this.rnd = rnd;
	}
	
	
	/**
	 * @return the lastDist2Dest
	 */
	public final float getLastDist2Dest()
	{
		return lastDist2Dest;
	}
	
	
	/**
	 * @param lastDist2Dest the lastDist2Dest to set
	 */
	public final void setLastDist2Dest(final float lastDist2Dest)
	{
		this.lastDist2Dest = lastDist2Dest;
	}
	
	
	/**
	 * @return the lastPath
	 */
	public final TrajPath getLastPath()
	{
		return lastPath;
	}
	
	
	/**
	 * @param lastPath the lastPath to set
	 */
	public final void setLastPath(final TrajPath lastPath)
	{
		this.lastPath = lastPath;
	}
	
	
	/**
	 * @return the maxAngle
	 */
	public final float getMaxAngle()
	{
		return maxAngle;
	}
	
	
	/**
	 * @param maxAngle the maxAngle to set
	 */
	public final void setMaxAngle(final float maxAngle)
	{
		this.maxAngle = maxAngle;
	}
	
	
	/**
	 * @return the finderInput
	 */
	public final TrajPathFinderInput getFinderInput()
	{
		return finderInput;
	}
	
	
	/**
	 * @return the wFrame
	 */
	public final WorldFrame getwFrame()
	{
		return wFrame;
	}
	
	
	/**
	 * @param wFrame the wFrame to set
	 */
	public final void setwFrame(final WorldFrame wFrame)
	{
		this.wFrame = wFrame;
	}
}
