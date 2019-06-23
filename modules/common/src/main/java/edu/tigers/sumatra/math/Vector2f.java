/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.08.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.math;

import com.sleepycat.persist.model.Persistent;


/**
 * Similar to {@link Vector2}, but final/immutable!
 * 
 * @author Gero
 */
@Persistent
public class Vector2f extends AVector2
{
	private final double	x;
	private final double	y;
	
	
	/**
	 * @param x
	 * @param y
	 */
	public Vector2f(final double x, final double y)
	{
		this.x = x;
		this.y = y;
	}
	
	
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * 
	 * @param xy
	 */
	public Vector2f(final IVector2 xy)
	{
		x = xy.x();
		y = xy.y();
	}
	
	
	/**
	 * Creates a final vector with (0, 0)
	 */
	public Vector2f()
	{
		x = 0;
		y = 0;
	}
	
	
	@Override
	public Vector2 getXYVector()
	{
		return new Vector2(this);
	}
	
	
	@Override
	public double x()
	{
		return x;
	}
	
	
	@Override
	public double y()
	{
		return y;
	}
}
