/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.angle;

/**
 * An angle that is not normalized so it can be used for counting rotations.
 * Use {@link IAngle} if you want an angle that is always normalized in the
 * range [-pi..pi[ and {@link IAngularMeasure} if you don't care.
 * 
 * @author DominikE
 */
public interface IRotation extends IAngularMeasure
{
	@Override
	IRotation add(IAngularMeasure angle);
	
	
	@Override
	IRotation add(double angle);
	
	
	@Override
	IRotation subtract(IAngularMeasure angle);
	
	
	@Override
	IRotation subtract(double angle);
	
	
	@Override
	IRotation multiply(double factor);
}
