/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 13, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.driver;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


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
	private static double			pushDistance	= 50;
	
	private final DynamicPosition	receiver;
	private IVector2					initBallPos		= null;
	private double						step				= 0;
	private long						lastTime			= 0;
	private boolean					done				= false;
	
	
	static
	{
		ConfigRegistration.registerClass("skills", PushBallDriver.class);
	}
	
	
	/**
	 * @param receiver
	 */
	public PushBallDriver(final DynamicPosition receiver)
	{
		this.receiver = receiver;
	}
	
	
	@Override
	public void update(final ITrackedBot bot, final ABot aBot, final WorldFrame wFrame)
	{
		if (initBallPos == null)
		{
			initBallPos = wFrame.getBall().getPos();
		}
		
		IVector2 direction = receiver.subtractNew(wFrame.getBall().getPos());
		double relAngle = Math.sin(step);
		Vector2 turnDir = direction.turnNew(relAngle * AngleMath.PI_QUART);
		turnDir.scaleTo(200 + pushDistance);
		IVector2 preDest = wFrame.getBall().getPos().addNew(direction.scaleToNew(-200));
		IVector2 dest = preDest.addNew(turnDir);
		setDestination(dest);
		setOrientation(direction.getAngle());
		
		if ((wFrame.getTimestamp() - lastTime) > 50e6)
		{
			step = step + 0.1;
			lastTime = wFrame.getTimestamp();
		}
		
		IVector2 ball = wFrame.getBall().getPos();
		double dist = GeoMath.distancePP(initBallPos, ball);
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
	public boolean isEnableDribbler()
	{
		return false;
	}
	
	
	@Override
	public boolean armKicker()
	{
		return false;
	}
}
