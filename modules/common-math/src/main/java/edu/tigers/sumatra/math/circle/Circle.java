/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.circle;

import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.MathException;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Geometrical representation of a circle.
 * 
 * @author Malte
 */
@Persistent(version = 1)
public class Circle extends ACircle
{
	private final IVector2 center;
	private final double radius;
	
	
	protected Circle()
	{
		this(AVector2.ZERO_VECTOR, 1);
	}
	
	
	/**
	 * @param circle a circle
	 */
	protected Circle(final ICircle circle)
	{
		this(circle.center(), circle.radius());
	}
	
	
	/**
	 * Defines a circle by a radius and a center.
	 * Radius must not be negative or zero!
	 * 
	 * @param center
	 * @param radius
	 * @throws IllegalArgumentException if the radius is not real positive
	 */
	protected Circle(final IVector2 center, final double radius)
	{
		if (radius <= 0)
		{
			throw new IllegalArgumentException("Radius of a circle must be larger than zero!");
		}
		this.center = Vector2.copy(center);
		this.radius = radius;
	}
	
	
	/**
	 * Create a circle from 3 points on the arc (no center given).
	 *
	 * @param p1 First point
	 * @param p2 Second point
	 * @param p3 Third point
	 * @return The unique circle going through all given points.
	 * @throws MathException If all points are on a line, no finite solution exists!
	 */
	public static ICircle from3Points(final IVector2 p1, final IVector2 p2, final IVector2 p3)
			throws MathException
	{
		RealMatrix luA = new Array2DRowRealMatrix(new double[][] { { 1, p1.x(), p1.y() }, { 1, p2.x(), p2.y() },
				{ 1, p3.x(), p3.y() } }, false);
		
		DecompositionSolver solver = new LUDecomposition(luA).getSolver();
		
		RealVector luB = new ArrayRealVector(new double[] { (p1.x() * p1.x()) + (p1.y() * p1.y()),
				(p2.x() * p2.x()) + (p2.y() * p2.y()), (p3.x() * p3.x()) + (p3.y() * p3.y()) }, false);
		
		RealVector solution;
		try
		{
			solution = solver.solve(luB);
		} catch (SingularMatrixException err)
		{
			throw new MathException("Infinite circle => line", err);
		}
		
		RealVector center = solution.getSubVector(1, 2).mapMultiplyToSelf(0.5);
		
		double sq = center.ebeMultiply(center).getL1Norm() + solution.getEntry(0);
		double radius = Math.sqrt(sq);
		
		return createCircle(Vector2.fromReal(center), radius);
	}
	
	
	/**
	 * Create a circle from 3 points on the arc (no center given).
	 *
	 * @param points
	 * @return The unique circle going through all given points.
	 * @throws MathException If all points are on a line, no finite solution exists!
	 */
	public static ICircle fromNPoints(final List<IVector2> points)
			throws MathException
	{
		RealMatrix qrA = new Array2DRowRealMatrix(points.size(), 3);
		RealVector qrB = new ArrayRealVector(points.size());
		for (int i = 0; i < points.size(); i++)
		{
			IVector2 p = points.get(i);
			qrA.setEntry(i, 0, 1);
			qrA.setEntry(i, 1, p.x());
			qrA.setEntry(i, 2, p.y());
			qrB.setEntry(i, (p.x() * p.x()) + (p.y() * p.y()));
		}
		
		DecompositionSolver solver = new QRDecomposition(qrA).getSolver();
		
		RealVector solution;
		try
		{
			solution = solver.solve(qrB);
		} catch (SingularMatrixException err)
		{
			throw new MathException("Infinite circle => line", err);
		}
		
		RealVector center = solution.getSubVector(1, 2).mapMultiplyToSelf(0.5);
		
		double sq = center.ebeMultiply(center).getL1Norm() + solution.getEntry(0);
		double radius = Math.sqrt(sq);
		
		return createCircle(Vector2.fromReal(center), radius);
	}
	
	
	/**
	 * @param center
	 * @param radius
	 * @return a new circle
	 */
	public static ICircle createCircle(final IVector2 center, final double radius)
	{
		return new Circle(center, radius);
	}
	
	
	@Override
	public ICircle withMargin(double margin)
	{
		return new Circle(center(), radius() + margin);
	}
	
	
	@Override
	public ICircle mirror()
	{
		return new Circle(center.multiplyNew(-1), radius);
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
	public final boolean equals(final Object o)
	{
		if (this == o)
			return true;
		if (!(o instanceof ICircle))
			return false;
		
		final ICircle circle = (ICircle) o;
		
		return center.equals(circle.center())
				&& SumatraMath.isEqual(radius, circle.radius());
	}
	
	
	@Override
	public final int hashCode()
	{
		int result;
		long temp;
		result = center.hashCode();
		temp = Double.doubleToLongBits(radius);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	
	
	@Override
	public String toString()
	{
		return "Circle{" +
				"center=" + center +
				", radius=" + radius +
				'}';
	}
}
