/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 6, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.ball;

import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BallState implements IState
{
	private IVector3	pos			= Vector3.ZERO_VECTOR, vel = Vector3.ZERO_VECTOR, acc = Vector3.ZERO_VECTOR;
	private IVector3	accTorque	= Vector3.ZERO_VECTOR;
	
	
	/**
	 * @param pos
	 * @param vel
	 * @param acc
	 * @param accTorque
	 */
	public BallState(final IVector3 pos, final IVector3 vel, final IVector3 acc, final IVector3 accTorque)
	{
		super();
		this.pos = pos;
		this.vel = vel;
		this.acc = acc;
		this.accTorque = accTorque;
	}
	
	
	/**
	 * @param state
	 */
	public BallState(final IState state)
	{
		this(state.getPos(), state.getVel(), state.getAcc(), state.getAccFromTorque());
	}
	
	
	/**
	 * 
	 */
	public BallState()
	{
	}
	
	
	/**
	 * @return the pos
	 */
	@Override
	public IVector3 getPos()
	{
		return pos;
	}
	
	
	/**
	 * @return the vel
	 */
	@Override
	public IVector3 getVel()
	{
		return vel;
	}
	
	
	/**
	 * @return the acc
	 */
	@Override
	public IVector3 getAcc()
	{
		return acc;
	}
	
	
	/**
	 * @param pos the pos to set
	 */
	public void setPos(final IVector3 pos)
	{
		this.pos = pos;
	}
	
	
	/**
	 * @param vel the vel to set
	 */
	public void setVel(final IVector3 vel)
	{
		this.vel = vel;
	}
	
	
	/**
	 * @param acc the acc to set
	 */
	public void setAcc(final IVector3 acc)
	{
		this.acc = acc;
	}
	
	
	/**
	 * @param accTorque the accTorque to set
	 */
	public void setAccTorque(final IVector3 accTorque)
	{
		this.accTorque = accTorque;
	}
	
	
	@Override
	public IVector3 getAccFromTorque()
	{
		return accTorque;
	}
	
	
}
