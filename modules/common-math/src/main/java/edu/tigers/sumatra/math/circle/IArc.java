/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.circle;

/**
 * An arc is a pizza peace of a circle.
 * 
 * @author nicolai.ommer
 */
public interface IArc extends ICircular
{
	/**
	 * @return the startAngle
	 */
	double getStartAngle();
	
	
	/**
	 * @return the angle
	 */
	double getRotation();
	
	
	@Override
	IArc mirror();
}
