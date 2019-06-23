/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 5, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.ball.collision;

import edu.tigers.sumatra.math.AVector3;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.wp.ball.IState;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotKickImpuls implements IImpulseObject
{
	private final IVector3	kickVel;
	private final ILine		kickerLine;
	
	
	/**
	 * @param pos
	 * @param center2DribblerDist
	 * @param kickVel
	 */
	public BotKickImpuls(final IVector3 pos, final double center2DribblerDist, final IVector3 kickVel)
	{
		super();
		kickerLine = BotCollision.getKickerFrontLine(pos, center2DribblerDist);
		this.kickVel = kickVel;
	}
	
	
	@Override
	public IVector3 getImpulse(final IVector3 pos)
	{
		if ((pos.z() < 170) && kickerLine.isPointOnLine(pos.getXYVector(), 20))
		{
			return kickVel;
		}
		
		return AVector3.ZERO_VECTOR;
	}
	
	
	@Override
	public IVector3 getTorqueAcc(final IState state)
	{
		return AVector3.ZERO_VECTOR;
	}
	
}
