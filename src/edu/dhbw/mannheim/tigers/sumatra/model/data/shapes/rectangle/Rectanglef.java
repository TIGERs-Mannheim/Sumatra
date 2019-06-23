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

import javax.persistence.Entity;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;


/**
 * Immutable implementation of IRectangle.
 * 
 * @author Malte
 * 
 */
@Entity
public class Rectanglef extends ARectangle
{
	/**  */
	private static final long	serialVersionUID	= 1L;
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** is always in the top left corner of the rectangle */
	private final Vector2f		topLeft;
	/** x */
	private final float			xExtend;
	/** y */
	private final float			yExtend;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * Creates a new Rectangle. Length and width must be positive
	 * and greater then zero.
	 * 
	 * @param leftTop which is used to define the position of the rectangle on the field.
	 *           Is always left-top.
	 * @param xExtend the length (x-axis)
	 * @param yExtend the width (y-axis)
	 * @throws IllegalArgumentException when length or width is negative or zero.
	 */
	public Rectanglef(IVector2 leftTop, float xExtend, float yExtend)
	{
		topLeft = new Vector2f(leftTop);
		if ((xExtend <= 0) || (yExtend <= 0))
		{
			throw new IllegalArgumentException("Lenght or width cannot be negative or zero.");
		}
		this.yExtend = yExtend;
		this.xExtend = xExtend;
	}
	
	
	/**
	 * @param rec
	 */
	public Rectanglef(IRectangle rec)
	{
		topLeft = new Vector2f(rec.topLeft());
		xExtend = rec.xExtend();
		yExtend = rec.yExtend();
	}
	
	
	/**
	 * Creates new Rectanglef from two points. Have to be counter side corners.
	 * @param p1
	 * @param p2
	 * @throws IllegalArgumentException when length or width is zero.
	 * @author DionH
	 */
	public Rectanglef(IVector2 p1, IVector2 p2)
	{
		topLeft = new Vector2f(SumatraMath.min(p1.x(), p2.x()), SumatraMath.max(p1.y(), p2.y()));
		xExtend = Math.abs(p1.x() - p2.x());
		yExtend = Math.abs(p1.y() - p2.y());
		
		if ((xExtend == 0) || (yExtend == 0))
		{
			throw new IllegalArgumentException("Lenght or width cannot be negative.");
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
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
		return new Vector2f(topLeft.x() + xExtend, topLeft.y());
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
