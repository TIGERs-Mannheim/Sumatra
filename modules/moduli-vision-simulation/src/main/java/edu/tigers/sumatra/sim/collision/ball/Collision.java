/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim.collision.ball;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 *
 */
public class Collision implements ICollision
{
	private final IVector2 pos;
	private final IVector2 normal;
	private final ICollisionObject object;


	public Collision(final IVector2 pos, final IVector2 normal, final ICollisionObject object)
	{
		super();
		this.pos = pos;
		this.normal = normal;
		this.object = object;
	}


	@Override
	public IVector2 getPos()
	{
		return pos;
	}


	@Override
	public IVector2 getNormal()
	{
		return normal;
	}


	@Override
	public IVector2 getObjectSurfaceVel()
	{
		return object.getSurfaceVel(getPos());
	}


	@Override
	public ICollisionObject getObject()
	{
		return object;
	}
}
