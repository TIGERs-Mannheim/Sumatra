/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.dynamics;

import edu.tigers.sumatra.math.vector.IVector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BallAction implements IAction
{
	private IVector3 accTorque;
	
	
	/**
	 * @param accTorque
	 */
	public BallAction(final IVector3 accTorque)
	{
		this.accTorque = accTorque;
	}
	
	
	@Override
	public IVector3 getAccTorque()
	{
		return accTorque;
	}
	
}
