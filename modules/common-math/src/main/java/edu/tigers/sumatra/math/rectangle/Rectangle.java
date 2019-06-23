/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.rectangle;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Immutable implementation of {@link IRectangle}.
 * 
 * @author Malte
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class Rectangle extends ARectangle
{
	private final IVector2 center;
	private final double xExtent;
	private final double yExtent;
	
	
	@SuppressWarnings("unused")
	protected Rectangle()
	{
		center = Vector2.zero();
		xExtent = 0;
		yExtent = 0;
	}
	
	
	private Rectangle(final IVector2 center, final double xExtent, final double yExtent)
	{
		Validate.isTrue(xExtent >= 0, "xExtent must be >= 0: " + xExtent);
		Validate.isTrue(yExtent >= 0, "yExtent must be >= 0" + yExtent);
		this.center = Vector2.copy(center);
		this.xExtent = xExtent;
		this.yExtent = yExtent;
	}
	
	
	/**
	 * @param rec
	 */
	protected Rectangle(final IRectangle rec)
	{
		center = Vector2.copy(rec.center());
		xExtent = rec.xExtent();
		yExtent = rec.yExtent();
	}
	
	
	/**
	 * Span the rectangle around given line.
	 *
	 * @param p0 of the line
	 * @param p1 of the line
	 * @param radius distance from line to rectangle border in orthogonal and parallel direction
	 * @return
	 */
	public static Rectangle aroundLine(final IVector2 p0, final IVector2 p1, final double radius)
	{
		IVector2 dir;
		if (p0.equals(p1))
		{
			dir = AVector2.X_AXIS;
		} else
		{
			dir = p0.subtractNew(p1);
		}
		IVector2 orthDir = dir.turnNew(AngleMath.PI_HALF).scaleTo(radius);
		IVector2 p2 = p1.addNew(dir.scaleToNew(-radius));
		IVector2 p3 = p0.addNew(dir.scaleToNew(radius));
		return fromPoints(p3.addNew(orthDir), p2.addNew(orthDir.turnNew(AngleMath.PI)));
	}
	
	
	/**
	 * Create a rectangle based on a line segment
	 *
	 * @param lineSegment start defines start and end point for this rectangle
	 * @return new rectangle based on line segment
	 */
	public static Rectangle fromLineSegment(final ILine lineSegment)
	{
		return fromPoints(lineSegment.getStart(), lineSegment.getEnd());
	}
	
	
	/**
	 * Creates new Rectangle from two points. Have to be counter side corners.
	 *
	 * @param p1 first point
	 * @param p2 second point
	 * @return new rectangle
	 */
	public static Rectangle fromPoints(final IVector2 p1, final IVector2 p2)
	{
		double xExtent = Math.abs(p1.x() - p2.x());
		double yExtent = Math.abs(p1.y() - p2.y());
		IVector2 center = Vector2.fromXY(
				Math.min(p1.x(), p2.x()) + xExtent / 2.0,
				Math.min(p1.y(), p2.y()) + yExtent / 2.0);
		return new Rectangle(center, xExtent, yExtent);
	}
	
	
	/**
	 * Creates a new Rectangle. xExtent and yExtent must be positive (may be zero).
	 *
	 * @param center which is used to define the position of the rectangle on the field
	 * @param xExtent the width on the x-axis
	 * @param yExtent the width on the y-axis
	 * @return new rectangle
	 */
	public static Rectangle fromCenter(final IVector2 center, final double xExtent, final double yExtent)
	{
		return new Rectangle(center, xExtent, yExtent);
	}
	
	
	@Override
	public IRectangle withMargin(final double margin)
	{
		return withMarginXy(margin, margin);
	}
	
	
	@Override
	public IRectangle withMarginXy(double xMargin, double yMargin)
	{
		return fromCenter(center(),
				Math.max(0, xExtent + 2 * xMargin),
				Math.max(0, yExtent + 2 * yMargin));
	}
	
	
	@Override
	public IRectangle mirror()
	{
		return fromCenter(center.multiplyNew(-1), xExtent, yExtent);
	}
	
	
	@Override
	public double yExtent()
	{
		return yExtent;
	}
	
	
	@Override
	public double xExtent()
	{
		return xExtent;
	}
	
	
	@Override
	public IVector2 center()
	{
		return center;
	}
	
	
	@Override
	public final boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}
		
		if (o == null)
		{
			return false;
		}
		
		if (!(o instanceof ARectangle))
		{
			return false;
		}
		
		final ARectangle rectangle = (ARectangle) o;
		
		return SumatraMath.isEqual(xExtent, rectangle.xExtent())
				&& SumatraMath.isEqual(yExtent, rectangle.yExtent())
				&& center.equals(rectangle.center());
	}
	
	
	@Override
	public final int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.append(center)
				.append(xExtent)
				.append(yExtent)
				.toHashCode();
	}
	
	
	@Override
	public String toString()
	{
		return "Rectangle{" +
				"center=" + center +
				", xExtent=" + xExtent +
				", yExtent=" + yExtent +
				'}';
	}
}
