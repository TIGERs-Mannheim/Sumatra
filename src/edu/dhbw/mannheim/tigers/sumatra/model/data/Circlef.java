/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.04.2011
 * Author(s): Malte
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

/**
 * Immutable version of {@link Circle}
 * 
 * @author Malte
 * 
 */
public class Circlef extends ACircle
{


	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final float radius;
	private final Vector2f center;
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Defines a circle by a radius and a center.
	 * Radius must not be negative or zero!
	 * @throws IllegalArgumentException
	 * 
	 * @param center
	 * @param radius
	 */
	public Circlef(IVector2 center, float radius)
	{
		if (radius <= 0)
		{
			throw new IllegalArgumentException("Radius of a circle must not be smaller than zero!");
		}
		this.center = new Vector2f(center);
		this.radius = radius;
	}


	/**
	 *
	 * @see #Circle(IVector2, float)
	 * @param c
	 */
	public Circlef(ICircle c)
	{
		this.center = new Vector2f(c.center());
		this.radius = c.radius();
	}


	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public float radius()
	{
		return radius;
	}

	@Override
	public Vector2f center()
	{
		return center;
	}

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
