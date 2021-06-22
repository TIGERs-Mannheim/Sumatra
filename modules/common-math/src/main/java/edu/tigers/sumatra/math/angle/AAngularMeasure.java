/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.angle;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.SumatraMath;


/**
 * @author DominikE
 */
@Persistent
public abstract class AAngularMeasure implements IAngularMeasure
{
	private static final double PI = Math.PI;
	protected static final double TWO_PI = Math.PI * 2.0;
	private static final double DEG_RAD_FACTOR = 180;
	private static final double DEG_TO_RAD = PI / DEG_RAD_FACTOR;
	private static final double RAD_TO_DEG = DEG_RAD_FACTOR / PI;

	protected final double angle;


	public AAngularMeasure(final double angle)
	{
		this.angle = angle;
	}


	protected static double rad2deg(double rad)
	{
		return rad * RAD_TO_DEG;
	}


	protected static double deg2rad(double deg)
	{
		return deg * DEG_TO_RAD;
	}


	protected static double rad2rot(double rad)
	{
		return rad / TWO_PI;
	}


	protected static double rot2rad(double rotation)
	{
		return rotation * TWO_PI;
	}


	@Override
	public double asRad()
	{
		return angle;
	}


	@Override
	public double asDeg()
	{
		return rad2deg(angle);
	}


	@Override
	public double asRotation()
	{
		return rad2rot(angle);
	}


	@Override
	public double sin()
	{
		return SumatraMath.sin(angle);
	}


	@Override
	public double cos()
	{
		return SumatraMath.cos(angle);
	}


	@Override
	public double tan()
	{
		return SumatraMath.tan(angle);
	}


	@Override
	public String toString()
	{
		return String.format("%.2f (%.2f°)", angle, rad2deg(angle));
	}


	@Override
	public boolean equals(final Object obj)
	{
		if (obj == null)
			return false;
		if (obj instanceof IAngularMeasure)
		{
			return Math.abs(this.subtract((IAngularMeasure) obj).asRad()) < 1e-3;
		}
		return false;
	}


	@Override
	public int hashCode()
	{
		return Double.hashCode(angle);
	}
}
