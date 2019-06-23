/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 22, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data;

import javax.persistence.Embeddable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


/**
 * data holder for a collision happening on the spline
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
@Embeddable
public class Collision
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private float		time;
	private IVector2	position;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param time
	 * @param position
	 */
	public Collision(float time, IVector2 position)
	{
		super();
		this.time = time;
		this.position = position;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
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
	public void setTime(float time)
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
	public void setPosition(IVector2 position)
	{
		this.position = position;
	}
	
}
