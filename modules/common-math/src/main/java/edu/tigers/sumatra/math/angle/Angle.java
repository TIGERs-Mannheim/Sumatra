/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.angle;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 * An angle that is always normalized in the range [-pi..pi[.
 * If you need values greater than pi use {@link IRotation}
 * which is not normalized.
 * <br>
 * Default implementation of {@link IAngle}.
 * 
 * @author DominikE
 */
public class Angle extends AAngularMeasure implements IAngle
{
	
	private static final Angle ZERO_ANGLE = new Angle();
	
	
	private Angle()
	{
		this(0);
	}
	
	
	private Angle(double rad)
	{
		super(normalizeAngle(rad));
	}
	
	
	public static Angle zero()
	{
		return ZERO_ANGLE;
	}
	
	
	public static Angle ofRad(double rad)
	{
		return new Angle(rad);
	}
	
	
	public static Angle ofDeg(double deg)
	{
		return new Angle(deg2rad(deg));
	}
	
	
	public static Angle ofRotation(double turns)
	{
		return new Angle(rot2rad(turns));
	}
	
	
	public static Angle ofVec(IVector2 vector)
	{
		return new Angle(vector.getAngle());
	}
	
	
	public static Angle ofAngularMeasure(IAngularMeasure angularMeasure)
	{
		return new Angle(angularMeasure.asRad());
	}
	
	
	@Override
	public IAngle add(final IAngularMeasure angle)
	{
		return add(angle.asRad());
	}
	
	
	@Override
	public IAngle add(final double angle)
	{
		return new Angle(this.angle + angle);
	}
	
	
	@Override
	public IAngle subtract(final IAngularMeasure angle)
	{
		return subtract(angle.asRad());
	}
	
	
	@Override
	public IAngle subtract(final double angle)
	{
		return new Angle(this.angle - angle);
	}
	
	
	@Override
	public IAngle multiply(final double factor)
	{
		return new Angle(this.angle * factor);
	}
	
	
	/**
	 * Normalize angle, to make sure angle is in [-pi/pi[ interval.
	 *
	 * @param angle some unnormalized angle
	 * @return angle in [-pi..pi]
	 */
	private static double normalizeAngle(final double angle)
	{
		return angle - (Math.round(angle / TWO_PI) * TWO_PI);
	}
}
