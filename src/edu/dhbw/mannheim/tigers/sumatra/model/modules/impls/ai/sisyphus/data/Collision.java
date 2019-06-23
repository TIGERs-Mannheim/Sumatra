/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 22, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


/**
 * data holder for a collision happening on the spline
 * Deprecated, but needed for Berkeley DB!
 * 
 * @deprecated
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 */
@Persistent
@Deprecated
public class Collision
{
	private float		time;
	private IVector2	position;
	
	
	@SuppressWarnings("unused")
	private Collision()
	{
	}
	
	
	/**
	 * @param time
	 * @param position
	 */
	public Collision(final float time, final IVector2 position)
	{
		super();
		this.time = time;
		this.position = position;
	}
	
	
	/**
	 * @return the time
	 */
	public float getTime()
	{
		return time;
	}
	
	
	/**
	 * @param time the time to set
	 */
	public void setTime(final float time)
	{
		this.time = time;
	}
	
	
	/**
	 * @return the position
	 */
	public IVector2 getPosition()
	{
		return position;
	}
	
	
	/**
	 * @param position the position to set
	 */
	public void setPosition(final IVector2 position)
	{
		this.position = position;
	}
	
}
