/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim.dynamics.ball;

import edu.tigers.sumatra.math.vector.IVector3;


/**
 */
public class BallState implements IState
{
	private IVector3 pos;
	private IVector3 vel;
	private IVector3 acc;
	
	
	public BallState(final IVector3 pos, final IVector3 vel, final IVector3 acc)
	{
		super();
		this.pos = pos;
		this.vel = vel;
		this.acc = acc;
	}
	
	
	/**
	 * @param state
	 */
	public BallState(final IState state)
	{
		this(state.getPos(), state.getVel(), state.getAcc());
	}
	
	
	@Override
	public IVector3 getPos()
	{
		return pos;
	}
	
	
	@Override
	public IVector3 getVel()
	{
		return vel;
	}
	
	
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
}
