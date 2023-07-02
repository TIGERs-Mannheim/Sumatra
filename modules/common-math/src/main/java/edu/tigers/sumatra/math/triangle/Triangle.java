/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.triangle;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.IBoundedPath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.List;

import static edu.tigers.sumatra.math.SumatraMath.sqrt;


/**
 * Immutable implementation of {@link ITriangle}.
 *
 * @author Malte
 */
@Persistent
public final class Triangle implements ITriangle
{
	private final IVector2 a;
	private final IVector2 b;
	private final IVector2 c;
	private final List<IVector2> corners;


	@SuppressWarnings("unused")
	private Triangle()
	{
		a = null;
		b = null;
		c = null;
		corners = null;
	}


	/**
	 * @param a
	 * @param b
	 * @param c
	 */
	private Triangle(final IVector2 a, final IVector2 b, final IVector2 c)
	{
		Validate.notNull(a);
		Validate.notNull(b);
		Validate.notNull(c);
		this.a = a;
		this.b = b;
		this.c = c;
		corners = new ArrayList<>(3);
		corners.add(a);
		corners.add(b);
		corners.add(c);
	}


	/**
	 * @param a
	 * @param b
	 * @param c
	 * @return new triangle
	 */
	public static Triangle fromCorners(final IVector2 a, final IVector2 b, final IVector2 c)
	{
		return new Triangle(a, b, c);
	}


	@Override
	public ITriangle withMargin(final double margin)
	{
		return TriangleMath.withMargin(this, margin);
	}


	@Override
	public ITriangle shortenToPoint(final IVector2 x)
	{
		ILine ab = Lines.lineFromPoints(a, b);
		ILine ac = Lines.lineFromPoints(a, c);
		ILine nb2nc = Lines.lineFromDirection(x, Vector2f.fromXY(x.y() - a.y(), a.x() - x.x()));

		var nb = ab.intersect(nb2nc).asOptional();
		var nc = ac.intersect(nb2nc).asOptional();

		if (nb.isPresent() && nc.isPresent())
		{
			return Triangle.fromCorners(a, nb.get(), nc.get());
		}
		throw new IllegalStateException();
	}


	@Override
	public IVector2 getA()
	{
		return a;
	}


	@Override
	public IVector2 getB()
	{
		return b;
	}


	@Override
	public IVector2 getC()
	{
		return c;
	}


	@Override
	public List<IVector2> getCorners()
	{
		return corners;
	}


	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		if (!(o instanceof ITriangle triangle))
			return false;
		if (a == null || b == null || c == null)
		{
			return false;
		}

		return a.equals(triangle.getA())
				&& b.equals(triangle.getB())
				&& c.equals(triangle.getC());

	}


	@Override
	public double area()
	{
		double lineA = a.distanceTo(b);
		double lineB = b.distanceTo(c);
		double lineC = c.distanceTo(a);
		double s = (lineA + lineB + lineC) / 2.;
		return sqrt(s * (s - lineA) * (s - lineB) * (s - lineC));
	}


	@Override
	public boolean isNeighbour(ITriangle triangle)
	{
		int counter = 0;
		for (IVector2 c1 : this.getCorners())
		{
			for (IVector2 c2 : triangle.getCorners())
			{
				if (c1.distanceTo(c2) <= 0)
				{
					counter++;
				}
			}
		}
		return counter == 2;
	}


	@Override
	public List<IBoundedPath> getPerimeterPath()
	{
		ILineSegment ab = Lines.segmentFromPoints(a, b);
		ILineSegment bc = Lines.segmentFromPoints(b, c);
		ILineSegment ca = Lines.segmentFromPoints(c, a);
		return List.of(ab, bc, ca);
	}


	@Override
	public boolean isPointInShape(final IVector2 point)
	{
		return TriangleMath.isPointInShape(this, point);
	}


	@Override
	public int hashCode()
	{
		int result = getA() != null ? getA().hashCode() : 0;
		result = 31 * result + (getB() != null ? getB().hashCode() : 0);
		result = 31 * result + (getC() != null ? getC().hashCode() : 0);
		return result;
	}


	@Override
	public String toString()
	{
		return "Triangle{" +
				"a=" + a +
				", b=" + b +
				", c=" + c +
				'}';
	}
}
