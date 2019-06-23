/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.circle;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class Arc extends AArc
{
	private final double startAngle;
	private final double rotation;
	private final Vector2f center;
	private final double radius;
	
	
	@SuppressWarnings("unused")
	protected Arc()
	{
		center = Vector2f.ZERO_VECTOR;
		radius = 1;
		startAngle = 0;
		rotation = 1;
	}
	
	
	/**
	 * @param center
	 * @param radius
	 * @param startAngle
	 * @param rotation
	 */
	protected Arc(final IVector2 center, final double radius, final double startAngle, final double rotation)
	{
		this.center = Vector2f.copy(center);
		this.radius = radius;
		this.startAngle = AngleMath.normalizeAngle(startAngle);
		this.rotation = rotation;
	}
	
	
	/**
	 * @param arc
	 */
	protected Arc(final IArc arc)
	{
		center = Vector2f.copy(arc.center());
		radius = arc.radius();
		startAngle = arc.getStartAngle();
		rotation = arc.getRotation();
	}
	
	
	/**
	 * Create a new arc
	 * 
	 * @param center
	 * @param radius
	 * @param startAngle
	 * @param rotation
	 * @return
	 */
	public static IArc createArc(final IVector2 center, final double radius, final double startAngle,
			final double rotation)
	{
		return new Arc(center, radius, startAngle, rotation);
	}
	
	
	/**
	 * @param arc
	 * @return
	 */
	public static IArc copyArc(final IArc arc)
	{
		return new Arc(arc);
	}
	
	
	@Override
	public IArc mirror()
	{
		return createArc(center().multiplyNew(-1), radius, startAngle + AngleMath.PI, rotation);
	}
	
	
	@Override
	public double radius()
	{
		return radius;
	}
	
	
	@Override
	public IVector2 center()
	{
		return center;
	}
	
	
	@Override
	public final double getStartAngle()
	{
		return startAngle;
	}
	
	
	@Override
	public final double getRotation()
	{
		return rotation;
	}
	
	
	@Override
	public final boolean equals(final Object o)
	{
		if (this == o)
			return true;
		if (!(o instanceof IArc))
			return false;
		
		final IArc arc = (IArc) o;
		
		return center.equals(arc.center())
				&& SumatraMath.isEqual(radius, arc.radius())
				&& SumatraMath.isEqual(startAngle, arc.getStartAngle())
				&& SumatraMath.isEqual(rotation, arc.getRotation());
	}
	
	
	@Override
	public final int hashCode()
	{
		int result;
		long temp;
		temp = Double.doubleToLongBits(getStartAngle());
		result = (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getRotation());
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		result = 31 * result + center.hashCode();
		temp = Double.doubleToLongBits(radius);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	
	
	@Override
	public String toString()
	{
		return "Arc{" +
				"startAngle=" + startAngle +
				", rotation=" + rotation +
				", center=" + center +
				", radius=" + radius +
				'}';
	}
}
