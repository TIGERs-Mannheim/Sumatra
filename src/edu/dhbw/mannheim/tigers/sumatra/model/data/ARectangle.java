/*
 * *********************************************************
 * Copyright (c) 2010 DHBW Mannheim - Tigers Mannheim
 * Project: tigers - field raster generator
 * Date: 11.08.2010
 * Authors:
 * Oliver Steinbrecher
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import java.io.Serializable;
import java.util.Random;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.AIRectangle;


/**
 * 
 * This abstract class represents a rectangle. It is used i. e. to describe a part-rectangle of the field.
 * Implementing {@link IRectangle}.
 * Superclass for {@link Rectangle} and {@link Rectanglef}.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>, MalteM
 */

public abstract class ARectangle implements Serializable, IRectangle
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -2289190109482320362L;
	
	
	// --------------------------------------------------------------------------
	// --- getInstance/constructor(s) -------------------------------------------
	// --------------------------------------------------------------------------
	public ARectangle()
	{
	}
	

	// --------------------------------------------------------------------------
	// --- public-method(s) -----------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append("(" + xExtend());
		builder.append("/");
		builder.append(+yExtend());
		builder.append(")");
		builder.append("ref" + topLeft().toString());
		return builder.toString();
	}
	

	/**
	 * 
	 * Function is used to check if two rectangles are equal. First compare the length and width and then the content of
	 * idSet.
	 * @param rectangle
	 * @return true when equal.
	 */
	public boolean equals(AIRectangle rectangle)
	{
		if (xExtend() == rectangle.xExtend() && yExtend() == rectangle.yExtend() && topLeft().equals(rectangle.topLeft()))
		{
			return true;
		}
		return false;
	}
	

	@Override
	public float getArea()
	{
		return xExtend() * yExtend();
	}
	

	/**
	 * checks if the point is inside the rectangle
	 * @param point
	 * @return true if inside (corners included!!)
	 * @author DanielW
	 */
	@Override
	public boolean isPointInShape(IVector2 point)
	{
		return (point.x() >= topLeft().x() && point.x() <= topLeft().x() + xExtend() && point.y() <= topLeft().y() && point
				.y() >= topLeft().y() - yExtend());
	}
	

	/**
	 * <pre>
	 * Checks if the line is intersecting the rectangle.
	 * 
	 * _ _ /_ _
	 * |  /   |
	 * | /    |
	 * |/_ _ _| 
	 * /
	 * </pre>
	 * 
	 * Funktioniert zur Zeit noch nicht..
	 * @param line
	 * @return true if intersecting
	 * @author MalteM
	 *         
	 */
	@Override
	@Deprecated
	public boolean isLineIntersectingShape(ILine line) 
	{
		Vector2 s  = new Vector2(line.supportVector());
		Vector2 n = line.directionVector().normalizeNew();
		boolean d1 = AIMath.isPositive(this.topLeft().subtractNew(s).scalarProduct(n));
		boolean d2 = AIMath.isPositive(this.topRight().subtractNew(s).scalarProduct(n)); 
		boolean d3 = AIMath.isPositive(this.bottomLeft().subtractNew(s).scalarProduct(n)); 
		boolean d4 = AIMath.isPositive(this.bottomRight().subtractNew(s).scalarProduct(n)); 
		
//		System.out.println(""+d1+d2+d3+d4);
		
		return !AIMath.allTheSame(d1,d2,d3,d4);
	}
	

	/**
	 * returns the nearest point outside this rectangle:
	 * - the point itself if outside
	 * - a point on the outer circumference if inside
	 * @param point
	 * @return a point guaranteed to be outside of the rectangle
	 * @author DionH
	 */
	@Override
	public Vector2 nearestPointOutside(IVector2 point)
	{
		// if point is inside
		if (point.x() > topLeft().x() && point.x() < topRight().x() && point.y() > topLeft().y()
				&& point.y() < bottomLeft().y())
		{
			Vector2 nearestPoint;
			float distance;
			
			// left
			distance = point.x() - topLeft().x();
			nearestPoint = new Vector2(topLeft().x(), point.y());
			
			// right
			if (distance > topRight().x() - point.x())
			{
				distance = topRight().x() - point.x();
				nearestPoint = new Vector2(topRight().x(), point.y());
			}
			
			// top
			if (distance > point.y() - topLeft().y())
			{
				distance = point.y() - topLeft().y();
				nearestPoint = new Vector2(point.x(), topLeft().y());
			}
			
			// bottom
			if (distance > bottomLeft().y() - point.y())
			{
				distance = bottomLeft().y() - point.y();
				nearestPoint = new Vector2(point.x(), bottomLeft().y());
			}
			
			return nearestPoint;
		}
		
		// else return point
		return new Vector2(point);
	}
	

	/**
	 * returns the nearest point inside this rectangle:
	 * - the point itself if inside
	 * - a point on the outer circumference if outside
	 * @param point
	 * @return a point guaranteed to be within rectangle
	 * @author DanielW
	 */
	public Vector2 nearestPointInside(IVector2 point)
	{
		Vector2 inside = new Vector2(0, 0);
		// setx
		if (point.x() < topLeft().x())
			inside.setX(topLeft().x());
		else if (point.x() > topLeft().x() + xExtend())
			inside.setX(topLeft().x() + xExtend());
		else
			inside.setX(point.x());
		
		// sety
		if (point.y() > topLeft().y())
			inside.setY(topLeft().y());
		else if (point.y() < topLeft().y() - yExtend())
			inside.setY(topLeft().y() - yExtend());
		else
			inside.setY(point.y());
		
		return inside;
	}
	

	/**
	 * 
	 * Generates a random point within the shape.
	 * 
	 * @return a random point within the shape
	 * @author Oliver Steinbrecher
	 */
	@Override
	public Vector2 getRandomPointInShape()
	{
		Random randomGenerator = new Random();
		
		float x;
		float y;
		
		if (AIMath.hasDigitsAfterDecimalPoint(this.xExtend()) || AIMath.hasDigitsAfterDecimalPoint(this.yExtend()))
		{
			// handle very small rectangles
			x = topLeft().x() + randomGenerator.nextFloat() * this.xExtend();
			y = topLeft().y() - randomGenerator.nextFloat() * this.yExtend();
			
			x = (float) (x - 1);
			
			if (y < 0)
				y = (float) (y + 1);
			else
				y = (float) (y - 1);
			
		} else
		{
			/*
			 * handle normal rectangles
			 * 
			 * Especially when the length and width have no position after decimal point
			 * the result vector should not have one.
			 */

			x = topLeft().x() + randomGenerator.nextInt((int) this.xExtend());
			y = topLeft().y() - randomGenerator.nextInt((int) this.yExtend());
		}
		
		return new Vector2(x, y);
	}
}
