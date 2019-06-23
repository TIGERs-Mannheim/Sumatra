/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.11.2010
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;



/**
 * Abstract {@link ILine Line} 
 * 
 * @author Malte
 * 
 */
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
		if (this.isVertical()) 
		{
			//Line is parallel to y-axis
			throw new MathException("Can't calculate the slope of a vertical line!");
		}
		return directionVector().y()/directionVector().x();
	}

	
	@Override
	public float getYIntercept() throws MathException
	{
		if (this.isVertical()) 
		{
			//Line is parallel to y-axis
			throw new MathException("Can't calculate y-intercept of a vertical line!");
		}
		float factor = (-this.supportVector().x()) / this.directionVector().x();
		float yIntercept = factor * this.directionVector().y() + this.supportVector().y(); 
		return yIntercept;
	}
	
	@Override
	public float getYValue(float x) throws MathException
	{
		return x * this.getSlope() + this.getYIntercept();
	}
	
	@Override
	public float getXValue(float y) throws MathException
	{
		if (this.isVertical())
		{
			return supportVector().x();
		}
		else if (this.isHorizontal())
		{
			throw new MathException("Can't calculate x-value of a horizontal line!");
		}
		else 
		{
			return (y - this.getYIntercept()) / this.getSlope();
		}
	}
	
	@Override
	public ILine getOrthogonalLine() {
		return new Line(this.supportVector(), this.directionVector().getNormalVector());
	};
	
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
	public boolean isPointOnTheLeft(IVector2 point)
	{
		// TODO Auto-generated method stub
		return false;
	}
}
