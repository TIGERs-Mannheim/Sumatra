package edu.tigers.sumatra.referee.control;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Data type for locations from game controller
 */
public class EventLocation
{
	private double x;
	private double y;
	
	
	public EventLocation()
	{
	}
	
	
	public EventLocation(IVector2 location)
	{
		this.x = location.x() / 1000;
		this.y = location.y() / 1000;
	}
	
	
	public double getX()
	{
		return x;
	}
	
	
	public void setX(final double x)
	{
		this.x = x;
	}
	
	
	public double getY()
	{
		return y;
	}
	
	
	public void setY(final double y)
	{
		this.y = y;
	}
}
