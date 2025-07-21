/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.angle;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 * An angle that is not normalized so it can be used for counting rotations.
 * Use {@link IAngle} if you want an angle that is always normalized in the
 * range [-pi..pi[ and {@link IAngularMeasure} if you don't care.
 * <br>
 * Default implementation of {@link IRotation}
 * 
 * @author DominikE
 */
public class Rotation extends AAngularMeasure implements IRotation
{
	
	private static final Rotation ZERO_ROTATION = new Rotation();
	
	
	private Rotation()
	{
		this(0);
	}
	
	
	private Rotation(double rad)
	{
		super(rad);
	}
	
	
	public static Rotation zero()
	{
		return ZERO_ROTATION;
	}
	
	
	public static Rotation ofRad(double rad)
	{
		return new Rotation(rad);
	}
	
	
	public static Rotation ofDeg(double deg)
	{
		return new Rotation(deg2rad(deg));
	}
	
	
	public static Rotation ofRotation(double turns)
	{
		return new Rotation(rot2rad(turns));
	}
	
	
	public static Rotation ofVec(IVector2 vector)
	{
		return new Rotation(vector.getAngle());
	}
	
	
	public static Rotation ofAngularMeasure(IAngularMeasure angularMeasure)
	{
		return new Rotation(angularMeasure.asRad());
	}
	
	
	@Override
	public IRotation add(final IAngularMeasure angle)
	{
		return add(angle.asRad());
	}
	
	
	@Override
	public IRotation add(final double angle)
	{
		return new Rotation(this.angle + angle);
	}
	
	
	@Override
	public IRotation subtract(final IAngularMeasure angle)
	{
		return subtract(angle.asRad());
	}
	
	
	@Override
	public IRotation subtract(final double angle)
	{
		return new Rotation(this.angle - angle);
	}
	
	
	@Override
	public IRotation multiply(final double factor)
	{
		return new Rotation(this.angle * factor);
	}
}
