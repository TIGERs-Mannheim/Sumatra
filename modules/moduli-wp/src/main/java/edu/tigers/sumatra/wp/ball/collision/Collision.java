/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 5, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.ball.collision;

import edu.tigers.sumatra.math.IVector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class Collision implements ICollision
{
	private final IVector2	pos;
	private final IVector2	normal;
	private final IVector2	objectVel;
	
	
	/**
	 * @param pos
	 * @param normal
	 * @param objectVel
	 */
	public Collision(final IVector2 pos, final IVector2 normal, final IVector2 objectVel)
	{
		super();
		this.pos = pos;
		this.normal = normal;
		this.objectVel = objectVel;
	}
	
	
	/**
	 * @return the pos
	 */
	@Override
	public IVector2 getPos()
	{
		return pos;
	}
	
	
	/**
	 * @return the normal
	 */
	@Override
	public IVector2 getNormal()
	{
		return normal;
	}
	
	
	@Override
	public IVector2 getObjectVel()
	{
		return objectVel;
	}
}
