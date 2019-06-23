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
 * Similar to {@link Vector3}, but final/immutable!
 * 
 * @author Gero
 */
@Persistent
public class Vector3f extends AVector3
{
	private final double	x;
	private final double	y;
	private final double	z;
	
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 */
	public Vector3f(final double x, final double y, final double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * 
	 * @param xy
	 * @param z
	 */
	public Vector3f(final IVector2 xy, final double z)
	{
		x = xy.x();
		y = xy.y();
		this.z = z;
	}
	
	
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * 
	 * @param original
	 */
	public Vector3f(final IVector2 original)
	{
		this(original, 0);
	}
	
	
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * 
	 * @param original
	 */
	public Vector3f(final IVector3 original)
	{
		x = original.x();
		y = original.y();
		z = original.z();
	}
	
	
	/**
	 * Creates a final vector with (0,0,0)
	 */
	public Vector3f()
	{
		x = 0;
		y = 0;
		z = 0;
	}
	
	
	@Override
	public Vector3 getXYZVector()
	{
		return new Vector3(this);
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
	
	
	@Override
	public double z()
	{
		return z;
	}
}
