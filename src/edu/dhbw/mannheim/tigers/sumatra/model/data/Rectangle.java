/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.04.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;


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
	private Vector2				topLeft;
	
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
	 * 
	 * @param referencePoint which is used to define the position of the rectangle on the field.
	 *           Is always left-top.
	 * @param rectangleLength the length (x-axis)
	 * @param rectangleWidth the width (y-axis)
	 * @throws IllegalArgumentException when length or width is negative or zero.
	 */
	public Rectangle(IVector2 topLeft, float rectangleLength, float rectangleWidth)
	{
		this.topLeft = new Vector2(topLeft);
		if (rectangleLength <= 0 || rectangleWidth <= 0)
		{
			throw new IllegalArgumentException("Lenght or width cannot be negative or zero.");
		} else
		{
			this.yExtend = rectangleWidth;
			this.xExtend = rectangleLength;
		}
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
		this.topLeft = new Vector2(AIMath.min(p1.x(), p2.x()), AIMath.min(p1.y(), p2.y()));
		this.xExtend = Math.abs(p1.x() - p2.x());
		this.yExtend = Math.abs(p1.y() - p2.y());
		
		if (this.xExtend == 0 || this.yExtend == 0)
			throw new IllegalArgumentException("Lenght or width cannot be negative.");
	}
	

	public Rectangle(IRectangle rec)
	{
		this.topLeft = new Vector2(rec.topLeft());
		this.xExtend = rec.xExtend();
		this.yExtend = rec.yExtend();
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
		} else
		{
			this.yExtend = yExtend;
		}
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
		} else
		{
			this.xExtend = xExtend;
		}
	}
	

	public void setTopLeft(Vector2 referencePoint)
	{
		this.topLeft = referencePoint;
	}
	

	/**
	 * 
	 * Tries to combine two rectangles when they are nearby each other.
	 * 
	 * @param combineRec which should be used for combining.
	 * @throws IllegalArgumentException when rectangles are equal, length and/or width are not
	 *            equitable or they are not close-by.
	 * @author Olli
	 */
	public Rectangle combine(Rectangle combineRec)
	{
		if (equals(combineRec))
		{
			throw new IllegalArgumentException("Rectangles are equal and cannot be combined.");
		}
		
		// TODO Olli: Really float-Equality? (Gero)
		if (xExtend() != combineRec.xExtend() || yExtend != combineRec.yExtend())
		{
			throw new IllegalArgumentException("Length and/or width of the rectangles are not equal.");
		} else
		{
			
			if (topLeft.x + xExtend() == combineRec.topLeft().x() && topLeft.y == combineRec.topLeft().y())
			{
				// --- position of combineRec is right of this ---
				xExtend += xExtend();
				
				return this;
			} else if (topLeft.x - xExtend() == combineRec.topLeft().x() && topLeft.y == combineRec.topLeft().y())
			{
				// --- position of combineRec is left of this ---
				xExtend += xExtend();
				topLeft = new Vector2(combineRec.topLeft());
				
				return this;
			} else if (topLeft.y + yExtend == combineRec.topLeft().y() && topLeft.x == combineRec.topLeft().x())
			{
				// --- position of combineRec is over this ---
				yExtend += yExtend;
				topLeft = new Vector2(combineRec.topLeft());
				
				return this;
			} else if (topLeft.y - yExtend == combineRec.topLeft().y() && topLeft.x == combineRec.topLeft().x())
			{
				// --- position of combineRec is under this ---
				yExtend += yExtend;
				return this;
			} else
			{
				throw new IllegalArgumentException("Rectangles are not close-by and thus cannot be combined.");
			}
		}
	}
	

	public Rectangle shrink(float width, float height)
	{
		topLeft.x += width;
		topLeft.y -= height;
		
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
		return new Vector2(topLeft.x + (xExtend / 2), topLeft.y - (yExtend / 2));
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
