/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 6, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.sim;

import edu.tigers.sumatra.wp.ball.BallCollisionModel;
import edu.tigers.sumatra.wp.ball.collision.CollisionHandler;
import edu.tigers.sumatra.wp.data.MotionContext;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BallCollisionSimModel extends BallCollisionModel
{
	
	/**
	 * 
	 */
	public BallCollisionSimModel()
	{
		super(true);
	}
	
	
	@Override
	protected void addCollisionObjects(final CollisionHandler ch, final MotionContext context)
	{
		super.addCollisionObjects(ch, context);
	}
	
	
	@Override
	protected void addImpulseObjects(final CollisionHandler ch, final MotionContext context)
	{
		super.addImpulseObjects(ch, context);
	}
	
}
