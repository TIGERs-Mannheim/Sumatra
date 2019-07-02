/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.angle;

/**
 * An angle that is always normalized in the range [-pi..pi[.
 * If you need values greater than pi use {@link IRotation}
 * which is not normalized.
 * 
 * @author DominikE
 */
public interface IAngle extends IAngularMeasure
{
	@Override
	IAngle add(IAngularMeasure angle);
	
	
	@Override
	IAngle add(double angle);
	
	
	@Override
	IAngle subtract(IAngularMeasure angle);
	
	
	@Override
	IAngle subtract(double angle);
	
	
	@Override
	IAngle multiply(double factor);
}
