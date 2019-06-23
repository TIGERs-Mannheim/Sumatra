/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.04.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;


/**
 * Mutable implementation of {@link IRectangle}.
 * 
 * @author Malte
 * 
 */
public class Rectangle extends ARectangle
{
	/**  */
	private static final long	serialVersionUID	= 1L;
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** is always in the top left corner of the rectangle */
	private IVector2				topLeft;
	
	/** x */
	private float					xExtend;
	/** y */
	private float					yExtend;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * 
	 * Creates a new Rectangle. Length and width must be positive
	 * and greater then zero.
	 * @param topLeft which is used to define the position of the rectangle on the field.
	 *           Is always left-top.
	 * @param rectangleLength the length (x-axis)
	 * @param rectangleWidth the width (y-axis)
	 * @throws IllegalArgumentException when length or width is negative or zero.
	 */
	public Rectangle(IVector2 topLeft, float rectangleLength, float rectangleWidth)
	{
		this.topLeft = new Vector2(topLeft);
		if ((rectangleLength <= 0) || (rectangleWidth <= 0))
		{
			throw new IllegalArgumentException("Lenght or width cannot be negative or zero.");
		}
		yExtend = rectangleWidth;
		xExtend = rectangleLength;
	}
	
	
	/**
	 * Creates new Rectangle from two points. Have to be counter side corners.
	 * @param p1
	 * @param p2
	 * @throws IllegalArgumentException when length or width is zero.
	 * @author DionH
	 */
	public Rectangle(IVector2 p1, IVector2 p2)
	{
		topLeft = new Vector2(SumatraMath.min(p1.x(), p2.x()), SumatraMath.max(p1.y(), p2.y()));
		xExtend = Math.abs(p1.x() - p2.x());
		yExtend = Math.abs(p1.y() - p2.y());
		
		if ((xExtend == 0) || (yExtend == 0))
		{
			throw new IllegalArgumentException("Lenght or width cannot be negative.");
		}
	}
	
	
	/**
	 * @param rec
	 */
	public Rectangle(IRectangle rec)
	{
		topLeft = new Vector2(rec.topLeft());
		xExtend = rec.xExtend();
		yExtend = rec.yExtend();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- setter/getter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * Set new rectangle width.
	 * 
	 * @param yExtend
	 * @throws IllegalArgumentException when length or width is negative or zero.
	 */
	public void setYExtend(float yExtend)
	{
		if (yExtend <= 0)
		{
			throw new IllegalArgumentException("Width cannot be negative or zero.");
		}
		this.yExtend = yExtend;
	}
	
	
	/**
	 * 
	 * Set new rectangle length.
	 * 
	 * @param xExtend
	 * @throws IllegalArgumentException when length or width is negative or zero.
	 */
	public void setxExtend(float xExtend)
	{
		if (xExtend <= 0)
		{
			throw new IllegalArgumentException("Length cannot be negative or zero.");
		}
		this.xExtend = xExtend;
	}
	
	
	/**
	 * @param referencePoint
	 */
	public void setTopLeft(Vector2 referencePoint)
	{
		topLeft = referencePoint;
	}
	
	
	/**
	 * @param width
	 * @param height
	 * @return
	 */
	public Rectangle shrink(float width, float height)
	{
		topLeft = new Vector2(topLeft.x() + width, topLeft.y() - height);
		
		xExtend -= 2 * width;
		yExtend -= 2 * height;
		
		return this;
	}
	
	
	@Override
	public float yExtend()
	{
		return yExtend;
	}
	
	
	@Override
	public float xExtend()
	{
		return xExtend;
	}
	
	
	@Override
	public IVector2 topLeft()
	{
		return topLeft;
	}
	
	
	@Override
	public IVector2 topRight()
	{
		return new Vector2(topLeft.x() + xExtend, topLeft.y());
	}
	
	
	@Override
	public IVector2 bottomLeft()
	{
		return new Vector2f(topLeft.x(), topLeft.y() - yExtend);
	}
	
	
	@Override
	public IVector2 bottomRight()
	{
		return new Vector2f(topLeft.x() + xExtend, topLeft.y() - yExtend);
	}
	
	
	@Override
	public IVector2 getMidPoint()
	{
		return new Vector2(topLeft.x() + (xExtend / 2), topLeft.y() - (yExtend / 2));
	}
	
	
	@Override
	public List<IVector2> getCorners()
	{
		List<IVector2> corners = new ArrayList<IVector2>();
		
		corners.add(new Vector2(topLeft));
		corners.add(bottomLeft());
		corners.add(bottomRight());
		corners.add(topRight());
		
		return corners;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
