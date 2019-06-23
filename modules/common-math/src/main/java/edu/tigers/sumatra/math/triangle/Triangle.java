/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.triangle;

import static edu.tigers.sumatra.math.SumatraMath.sqrt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * Immutable implementation of {@link ITriangle}.
 * 
 * @author Malte
 */
@Persistent
public final class Triangle extends ATriangle
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
	protected Triangle(final IVector2 a, final IVector2 b, final IVector2 c)
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
	 * @param triangle
	 */
	protected Triangle(final Triangle triangle)
	{
		a = triangle.a;
		b = triangle.b;
		c = triangle.c;
		corners = triangle.corners;
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
		ILine ab = Line.fromPoints(a, b);
		ILine ac = Line.fromPoints(a, c);
		ILine nbnc = Line.fromDirection(x, Vector2f.fromXY(x.y() - a.y(), a.x() - x.x()));
		
		Optional<IVector2> nb = LineMath.intersectionPoint(ab, nbnc);
		Optional<IVector2> nc = LineMath.intersectionPoint(ac, nbnc);
		
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
	public final boolean equals(final Object o)
	{
		if (this == o)
			return true;
		if (!(o instanceof ITriangle))
			return false;
		if (a == null || b == null || c == null)
		{
			return false;
		}
		
		final ITriangle triangle = (ITriangle) o;
		
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
	public List<IVector2> lineIntersections(final ILine line)
	{
		ILineSegment ab = Lines.segmentFromPoints(a, b);
		ILineSegment bc = Lines.segmentFromPoints(b, c);
		ILineSegment ac = Lines.segmentFromPoints(a, c);
		ILineSegment[] lines = new ILineSegment[] { ab, ac, bc };
		List<IVector2> intersections = new ArrayList<>();
		for (ILineSegment segment : lines)
		{
			Optional<IVector2> intersection = segment.intersectLine(Lines.lineFromLegacyLine(line));
			intersection.ifPresent(intersections::add);
		}
		return intersections;
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
