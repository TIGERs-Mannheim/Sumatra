/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim.collision.ball;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;


/**
 * A collision object that can have a velocity and can be sticky (for dribbling).
 */
public class KickerFrontLineCollisionObject extends LineSegmentCollisionObject
{
	private final IVector3 vel;
	private final BotID botID;
	private IVector3 impulse = Vector3f.ZERO_VECTOR;
	private IVector3 acc = Vector3f.ZERO_VECTOR;
	private boolean sticky;
	private double dampFactor;


	public KickerFrontLineCollisionObject(final ILine obstacleLine, final IVector3 vel, final IVector2 normal,
			final BotID botID)
	{
		super(Lines.segmentFromPoints(obstacleLine.getStart(), obstacleLine.getEnd()), normal);
		this.vel = vel;
		this.botID = botID;
	}


	@Override
	public IVector3 getImpulse(final IVector3 prePos)
	{
		if (prePos.z() < 0.001)
		{
			return impulse;
		}
		return Vector3.from2d(impulse.getXYVector(), 0);
	}


	@Override
	public IVector2 stick(IVector2 pos)
	{
		return obstacleLine.getCenter();
	}


	public void setImpulse(final IVector3 impulse)
	{
		this.impulse = impulse;
	}


	@Override
	public boolean isSticky()
	{
		return sticky;
	}


	@Override
	public double getDampFactor()
	{
		return dampFactor;
	}


	public void setSticky(final boolean sticky)
	{
		this.sticky = sticky;
	}


	public void setDampFactor(final double dampFactor)
	{
		this.dampFactor = dampFactor;
	}


	@Override
	public IVector3 getAcc()
	{
		return acc;
	}


	public void setAcc(final IVector3 acc)
	{
		this.acc = acc;
	}


	@Override
	public IVector3 getVel()
	{
		return vel;
	}


	@Override
	public BotID getBotID()
	{
		return botID;
	}
}
