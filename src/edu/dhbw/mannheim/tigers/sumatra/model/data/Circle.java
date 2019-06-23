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
 * Geometrical representation of a circle.
 * 
 * @author Malte
 * 
 */
public class Circle extends ACircle 
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** Center of the circle! */
	private final Vector2 center;
	
	/** Radius of the circle. Mustn't be negative! */
	private float radius;

	
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
	public Circle(IVector2 center, float radius)
	{
		if (radius <= 0)
		{
			throw new IllegalArgumentException("Radius of a circle must not be smaller than zero!");
		}
		else
		{
			this.center = new Vector2(center);
			this.radius = radius;
		}
		
	}


	/**
	 *
	 * @see #Circle(Vector2, float)
	 * @param c
	 */
	public Circle(ICircle c)
	{
		this.center = new Vector2();
		set(c);
	}
	
	
	/**
	 * @param c
	 */
	public void set(ICircle c)
	{
		this.center.set(c.center());
		this.radius = c.radius();
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public void setCenter(IVector2 center)
	{
		this.center.set(center);
	}

	public void setRadius(float radius)
	{
		this.radius = radius;
	}


	@Override
	public float radius()
	{
		return radius;
	}


	@Override
	public AVector2 center()
	{
		return center;
	}
}


