/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.testplays.util;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class Point
{
	
	@JsonProperty("fastPos")
	private boolean	fastPos;
	
	@JsonProperty("allowPenaltyAreaOur")
	private boolean	allowPenaltyAreaOur;
	
	@JsonProperty("allowPenaltyAreaTheir")
	private boolean	allowPenaltyAreaTheir;
	
	@JsonProperty("x")
	private double	x;
	
	@JsonProperty("y")
	private double	y;
	
	
	/**
	 * Creates a new point with the given coordinates.
	 * 
	 * @param x
	 * @param y
	 */
	public Point(double x, double y)
	{
		
		this.x = x;
		this.y = y;
	}
	
	
	/**
	 * Creates a new Point with x = y = 0
	 */
	public Point()
	{
		this(0, 0);
	}
	
	
	/**
	 * Creates a new Vector2 from this point.
	 * 
	 * @return A new Vector2
	 */
	public IVector2 createVector2()
	{
		
		return Vector2.fromXY(x, y);
	}
	
	
	public boolean isFastPos()
	{
		return fastPos;
	}
	
	
	public void setFastPos(final boolean fastPos)
	{
		this.fastPos = fastPos;
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
	
	
	public boolean isAllowPenaltyAreaOur()
	{
		return allowPenaltyAreaOur;
	}
	
	
	public void setAllowPenaltyAreaOur(final boolean allowPenaltyAreaOur)
	{
		this.allowPenaltyAreaOur = allowPenaltyAreaOur;
	}
	
	
	public boolean isAllowPenaltyAreaTheir()
	{
		return allowPenaltyAreaTheir;
	}
	
	
	public void setAllowPenaltyAreaTheir(final boolean allowPenaltyAreaTheir)
	{
		this.allowPenaltyAreaTheir = allowPenaltyAreaTheir;
	}

	@Override
	public String toString() {
		return "x: " + x + "; y: " + y;
	}
}
