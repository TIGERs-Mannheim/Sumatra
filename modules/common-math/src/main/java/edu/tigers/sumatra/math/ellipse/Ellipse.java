/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.ellipse;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * Default implementation of an ellipse
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class Ellipse extends AEllipse
{
	
	private final IVector2	center;
	private final double		radiusX;
	private final double		radiusY;
	private final double		turnAngle;
	
	
	@SuppressWarnings("unused")
	protected Ellipse()
	{
		this(Vector2f.ZERO_VECTOR, 1, 1, 0);
	}
	
	
	/**
	 * @param center
	 * @param radiusX
	 * @param radiusY
	 * @param turnAngle
	 */
	protected Ellipse(final IVector2 center, final double radiusX, final double radiusY, final double turnAngle)
	{
		if ((radiusX <= 0) || (radiusY <= 0))
		{
			throw new IllegalArgumentException("radius may not be equal or smaller than zero");
		}
		if (center == null)
		{
			throw new IllegalArgumentException("center may not be null");
		}
		this.center = center;
		this.radiusX = radiusX;
		this.radiusY = radiusY;
		this.turnAngle = AngleMath.normalizeAngle(turnAngle);
	}
	
	
	/**
	 * @param center
	 * @param radiusX
	 * @param radiusY
	 */
	protected Ellipse(final IVector2 center, final double radiusX, final double radiusY)
	{
		this(center, radiusX, radiusY, 0);
	}
	
	
	/**
	 * Copy constructor
	 * 
	 * @param ellipse
	 */
	protected Ellipse(final IEllipse ellipse)
	{
		this(ellipse.center(), ellipse.getRadiusX(), ellipse.getRadiusY(), ellipse.getTurnAngle());
	}
	
	
	/**
	 * @param center
	 * @param radiusX
	 * @param radiusY
	 * @param turnAngle
	 * @return new ellipse
	 */
	public static Ellipse createTurned(final IVector2 center, final double radiusX, final double radiusY,
			final double turnAngle)
	{
		return new Ellipse(center, radiusX, radiusY, turnAngle);
	}
	
	
	/**
	 * @param center
	 * @param radiusX
	 * @param radiusY
	 * @return new ellipse
	 */
	public static Ellipse createEllipse(final IVector2 center, final double radiusX, final double radiusY)
	{
		return new Ellipse(center, radiusX, radiusY);
	}
	
	
	@Override
	public double getTurnAngle()
	{
		return turnAngle;
	}
	
	
	@Override
	public IVector2 center()
	{
		return center;
	}
	
	
	@Override
	public double getRadiusX()
	{
		return radiusX;
	}
	
	
	@Override
	public double getRadiusY()
	{
		return radiusY;
	}
	
	
	@Override
	public final boolean equals(final Object o)
	{
		if (this == o)
			return true;
		if (!(o instanceof IEllipse))
			return false;
		
		final IEllipse ellipse = (IEllipse) o;
		
		return SumatraMath.isEqual(getRadiusX(), ellipse.getRadiusX())
				&& SumatraMath.isEqual(getRadiusY(), ellipse.getRadiusY())
				&& SumatraMath.isEqual(getTurnAngle(), ellipse.getTurnAngle())
				&& center().equals(ellipse.center());
	}
	
	
	@Override
	public final int hashCode()
	{
		int result;
		long temp;
		result = center.hashCode();
		temp = Double.doubleToLongBits(getRadiusX());
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getRadiusY());
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getTurnAngle());
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	
	
	@Override
	public String toString()
	{
		return "Ellipse{" +
				"center=" + center +
				", radiusX=" + radiusX +
				", radiusY=" + radiusY +
				", turnAngle=" + turnAngle +
				'}';
	}
}
