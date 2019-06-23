/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.11.2010
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;


/**
 * Abstract {@link ILine Line}
 * 
 * @author Malte
 * 
 */
@Persistent
public abstract class ALine implements ILine
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Only for code-reuse, not meant to be used by anyone anymore! =)
	 */
	ALine()
	{
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public String toString()
	{
		return "Line: (" + supportVector().x() + "," + supportVector().y() + ") + v * (" + directionVector().x() + ","
				+ supportVector().y() + ")";
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public float getSlope() throws MathException
	{
		if (isVertical())
		{
			// Line is parallel to y-axis
			throw new MathException("Can't calculate the slope of a vertical line!");
		}
		return directionVector().y() / directionVector().x();
	}
	
	
	@Override
	public float getYIntercept() throws MathException
	{
		if (isVertical())
		{
			// Line is parallel to y-axis
			throw new MathException("Can't calculate y-intercept of a vertical line!");
		}
		float factor = (-supportVector().x()) / directionVector().x();
		float yIntercept = (factor * directionVector().y()) + supportVector().y();
		return yIntercept;
	}
	
	
	@Override
	public float getYValue(float x) throws MathException
	{
		return (x * getSlope()) + getYIntercept();
	}
	
	
	@Override
	public float getXValue(float y) throws MathException
	{
		if (isVertical())
		{
			return supportVector().x();
		} else if (isHorizontal())
		{
			throw new MathException("Can't calculate x-value of a horizontal line!");
		} else
		{
			return (y - getYIntercept()) / getSlope();
		}
	}
	
	
	@Override
	public ILine getOrthogonalLine()
	{
		return new Line(supportVector(), directionVector().getNormalVector());
	}
	
	
	@Override
	public boolean isVertical()
	{
		return directionVector().isVertical();
	}
	
	
	@Override
	public boolean isHorizontal()
	{
		return directionVector().isHorizontal();
	}
	
	
	@Override
	public boolean isPointInFront(IVector2 point)
	{
		Vector2 b = point.subtractNew(supportVector());
		
		// angle above 90deg
		if (directionVector().normalizeNew().scalarProduct(b.normalize()) < 0)
		{
			return false;
		}
		
		return true;
	}
	
	
	@Override
	public boolean isPointOnTheLeft(IVector2 point)
	{
		return false;
	}
}
