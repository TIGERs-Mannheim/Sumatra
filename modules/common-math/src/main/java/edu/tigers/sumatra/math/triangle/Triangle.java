/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.triangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Immutable implementation of {@link ITriangle}.
 * 
 * @author Malte
 */
@Persistent
public class Triangle extends ATriangle
{
	private final IVector2	a;
	private final IVector2	b;
	private final IVector2	c;
	
	
	@SuppressWarnings("unused")
	private Triangle()
	{
		a = null;
		b = null;
		c = null;
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
	}
	
	
	/**
	 * @param triangle
	 */
	protected Triangle(final Triangle triangle)
	{
		a = triangle.a;
		b = triangle.b;
		c = triangle.c;
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
		ILine nbnc = Line.fromDirection(x, Vector2.fromXY(x.y() - a.y(), a.x() - x.x()));
		
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
		List<IVector2> corners = new ArrayList<>(3);
		corners.add(a);
		corners.add(b);
		corners.add(c);
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
