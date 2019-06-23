/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 4, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.drawable;

import edu.tigers.sumatra.math.AngleMath;


/**
 */
public enum EFieldTurn
{
	/**  */
	NORMAL(0),
	/**  */
	T90(AngleMath.PI_HALF),
	/**  */
	T180(AngleMath.PI),
	/**  */
	T270(AngleMath.PI + AngleMath.PI_HALF);
	
	private final double	angle;
	
	
	private EFieldTurn(final double angle)
	{
		this.angle = angle;
	}
	
	
	/**
	 * @return the angle
	 */
	public final double getAngle()
	{
		return angle;
	}
	
	
	/**
	 * Returns the angular opposite of the current value
	 * 
	 * @return
	 */
	public EFieldTurn getOpposite()
	{
		switch (this)
		{
			case NORMAL:
				return T180;
			case T180:
				return NORMAL;
			case T270:
				return T90;
			case T90:
				return T270;
			default:
				throw new IllegalArgumentException("Please add the new value to this switch case: " + this);
		}
	}
}
