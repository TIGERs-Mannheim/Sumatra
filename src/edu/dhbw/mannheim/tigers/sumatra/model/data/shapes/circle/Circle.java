/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.04.2011
 * Author(s): Malte
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;


/**
 * Geometrical representation of a circle.
 * 
 * @author Malte
 */
@Persistent(version = 1)
public class Circle extends ACircle
{
	/** Center of the circle! */
	protected IVector2	center;
	
	/** Radius of the circle. Mustn't be negative! */
	protected float		radius;
	
	
	@SuppressWarnings("unused")
	protected Circle()
	{
		this(AVector2.ZERO_VECTOR, 1);
	}
	
	
	/**
	 * Defines a circle by a radius and a center.
	 * Radius must not be negative or zero!
	 * 
	 * @param center
	 * @param radius
	 * @throws IllegalArgumentException
	 */
	public Circle(final IVector2 center, final float radius)
	{
		if (radius <= 0)
		{
			throw new IllegalArgumentException("Radius of a circle must not be smaller than zero!");
		}
		this.center = new Vector2(center);
		this.radius = radius;
	}
	
	
	/**
	 * @see #Circle(Vector2, float)
	 * @param c
	 */
	public Circle(final ICircle c)
	{
		this(c.center(), c.radius());
	}
	
	
	/**
	 * @param center
	 * @param radius
	 * @return
	 */
	public static Circle getNewCircle(final IVector2 center, final float radius)
	{
		return new Circle(center, radius);
	}
	
	
	@Override
	public float radius()
	{
		return radius;
	}
	
	
	@Override
	public IVector2 center()
	{
		return center;
	}
	
	
	@Override
	public String toString()
	{
		return "Center = " + center().toString() + "\nRadius = " + radius();
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IFieldPanel fieldPanel, final boolean invert)
	{
		// --- from SSLVision-mm to java2d-coordinates ---
		final IVector2 center = fieldPanel.transformToGuiCoordinates(center(), invert);
		final float radius = fieldPanel.scaleXLength(radius());
		
		g.setColor(Color.RED);
		g.setStroke(new BasicStroke(1));
		g.drawOval((int) (center.x() - radius), (int) (center.y() - radius), (int) radius * 2, (int) radius * 2);
	}
}
