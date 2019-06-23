/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 13, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Try to solve a deadlock by pushing the ball
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PushBallDriver extends PositionDriver implements IKickPathDriver
{
	@SuppressWarnings("unused")
	private static final Logger	log				= Logger.getLogger(PushBallDriver.class.getName());
	
	@Configurable(comment = "Distance behind ball to set destination when pushing the ball")
	private static float				pushDistance	= 50;
	
	private final DynamicPosition	receiver;
	private IVector2					initBallPos		= null;
	private float						step				= 0;
	private long						lastTime			= System.nanoTime();
	private boolean					done				= false;
	
	
	/**
	 * @param receiver
	 */
	public PushBallDriver(final DynamicPosition receiver)
	{
		this.receiver = receiver;
	}
	
	
	@Override
	public void update(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		if (initBallPos == null)
		{
			initBallPos = wFrame.getBall().getPos();
		}
		
		IVector2 direction = receiver.subtractNew(wFrame.getBall().getPos());
		float relAngle = AngleMath.sin(step);
		Vector2 turnDir = direction.turnNew(relAngle * AngleMath.PI_QUART);
		turnDir.scaleTo(200 + pushDistance);
		IVector2 preDest = wFrame.getBall().getPos().addNew(direction.scaleToNew(-200));
		IVector2 dest = preDest.addNew(turnDir);
		setDestination(dest);
		setOrientation(direction.getAngle());
		
		if ((System.nanoTime() - lastTime) > 50e6)
		{
			step = step + 0.1f;
			lastTime = System.nanoTime();
		}
		
		IVector2 ball = wFrame.getBall().getPos();
		float dist = GeoMath.distancePP(initBallPos, ball);
		if (dist > 50)
		{
			done = true;
		}
	}
	
	
	@Override
	public boolean isDone()
	{
		return done;
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.PUSH_BALL;
	}
	
	
	@Override
	public boolean isReceiving()
	{
		return false;
	}
	
	
	@Override
	public void setPenAreaAllowed(final boolean allowed)
	{
	}
	
	
	@Override
	public boolean armKicker()
	{
		return false;
	}
}
