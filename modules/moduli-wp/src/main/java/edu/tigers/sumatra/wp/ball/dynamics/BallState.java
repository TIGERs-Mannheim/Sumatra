/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.dynamics;

import Jama.Matrix;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BallState implements IState
{
	private IVector3	pos	= Vector3.ZERO_VECTOR;
	private IVector3	vel	= Vector3.ZERO_VECTOR;
	private IVector3	acc	= Vector3.ZERO_VECTOR;
	
	
	/**
	 * @param pos
	 * @param vel
	 * @param acc
	 */
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
	
	
	/**
	 * @param state
	 */
	public BallState(final Matrix state)
	{
		this.pos = Vector3.fromXYZ(state.get(0, 0) * 1e3, state.get(1, 0) * 1e3, state.get(2, 0) * 1e3);
		this.vel = Vector3.fromXYZ(state.get(3, 0), state.get(4, 0), state.get(5, 0));
		this.acc = Vector3.fromXYZ(state.get(6, 0), state.get(7, 0), state.get(8, 0));
	}
	
	
	/**
	 * Zero state
	 */
	public BallState()
	{
		// already initialized
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
}
