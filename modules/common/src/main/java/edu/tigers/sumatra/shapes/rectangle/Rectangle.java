/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.04.2011
 * Author(s): Malte
 * *********************************************************
 */
package edu.tigers.sumatra.shapes.rectangle;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector2f;


/**
 * Mutable implementation of {@link IRectangle}.
 * 
 * @author Malte
 */
@Persistent
public class Rectangle extends ARectangle
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** is always in the top left corner of the rectangle */
	private final IVector2	topLeft;
									
	/** x */
	private final double		xExtend;
	/** y */
	private final double		yExtend;
									
									
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@SuppressWarnings("unused")
	protected Rectangle()
	{
		this(AVector2.ZERO_VECTOR, new Vector2(1, 1));
	}
	
	
	/**
	 * Creates a new Rectangle. Length and width must be positive
	 * and greater then zero.
	 * 
	 * @param topLeft which is used to define the position of the rectangle on the field.
	 *           Is always left-top.
	 * @param rectangleLength the length (x-axis)
	 * @param rectangleWidth the width (y-axis)
	 * @throws IllegalArgumentException when length or width is negative or zero.
	 */
	public Rectangle(final IVector2 topLeft, final double rectangleLength, final double rectangleWidth)
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
	 * 
	 * @param p1
	 * @param p2
	 * @throws IllegalArgumentException when length or width is zero.
	 * @author DionH
	 */
	public Rectangle(final IVector2 p1, final IVector2 p2)
	{
		topLeft = new Vector2(SumatraMath.min(p1.x(), p2.x()), SumatraMath.max(p1.y(), p2.y()));
		double xExtend = Math.abs(p1.x() - p2.x());
		double yExtend = Math.abs(p1.y() - p2.y());
		
		if ((xExtend == 0))
		{
			xExtend = 1e-6f;
		}
		if ((yExtend == 0))
		{
			yExtend = 1e-6f;
		}
		this.xExtend = xExtend;
		this.yExtend = yExtend;
	}
	
	
	/**
	 * @param p0
	 * @param p1
	 * @param radius
	 * @return
	 */
	public static Rectangle aroundLine(final IVector2 p0, final IVector2 p1, final double radius)
	{
		IVector2 dir;
		if (p0.equals(p1))
		{
			dir = AVector2.X_AXIS;
		} else
		{
			dir = p0.subtractNew(p1);
		}
		IVector2 orthDir = dir.turnNew(AngleMath.PI_HALF).scaleTo(radius);
		IVector2 p2 = p1.addNew(dir.scaleToNew(-radius));
		IVector2 p3 = p0.addNew(dir.scaleToNew(radius));
		return new Rectangle(p3.addNew(orthDir), p2.addNew(orthDir.turnNew(AngleMath.PI)));
	}
	
	
	/**
	 * @param rec
	 */
	public Rectangle(final IRectangle rec)
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
	
	
	@Override
	public double yExtend()
	{
		return yExtend;
	}
	
	
	@Override
	public double xExtend()
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
		return new Vector2(topLeft.x() + (xExtend / 2.0), topLeft.y() - (yExtend / 2.0));
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
	
	
	/**
	 * @return the topLeft
	 */
	public final IVector2 getTopLeft()
	{
		return topLeft;
	}
	
	
	/**
	 * @return the xExtend
	 */
	public final double getxExtend()
	{
		return xExtend;
	}
	
	
	/**
	 * @return the yExtend
	 */
	public final double getyExtend()
	{
		return yExtend;
	}
}
