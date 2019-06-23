/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.04.2011
 * Author(s): Malte
 * *********************************************************
 */
package edu.tigers.sumatra.shapes.circle;

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

import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.MathException;
import edu.tigers.sumatra.math.Vector2;


/**
 * Geometrical representation of a circle.
 * 
 * @author Malte
 */
@Persistent(version = 1)
public class Circle extends ACircle
{
	/** Center of the circle! */
	private IVector2	center;
	
	/** Radius of the circle. Mustn't be negative! */
	private double		radius;
	
	
	protected Circle()
	{
		this(AVector2.ZERO_VECTOR, 1);
	}
	
	
	/**
	 * Defines a circle by a radius and a center.
	 * Radius must not be negative or zero!
	 * 
	 * @param center
	 * @param radius
	 * @throws IllegalArgumentException
	 */
	public Circle(final IVector2 center, final double radius)
	{
		if (radius <= 0)
		{
			throw new IllegalArgumentException("Radius of a circle must not be smaller than zero!");
		}
		this.center = new Vector2(center);
		this.radius = radius;
	}
	
	
	/**
	 * @see #Circle(Vector2, double)
	 * @param c
	 */
	public Circle(final ICircle c)
	{
		this(c.center(), c.radius());
	}
	
	
	/**
	 * Create a circle from 3 points on the arc (no center given).
	 * 
	 * @author AndreR
	 * @param P1 First point
	 * @param P2 Second point
	 * @param P3 Third point
	 * @return The unique circle going through all given points.
	 * @throws MathException If all points are on a line, no finite solution exists!
	 */
	public static Circle circleFrom3Points(final IVector2 P1, final IVector2 P2, final IVector2 P3)
			throws MathException
	{
		RealMatrix A = new Array2DRowRealMatrix(new double[][] { { 1, P1.x(), P1.y() }, { 1, P2.x(), P2.y() },
				{ 1, P3.x(), P3.y() } }, false);
		
		DecompositionSolver solver = new LUDecomposition(A).getSolver();
		
		RealVector B = new ArrayRealVector(new double[] { (P1.x() * P1.x()) + (P1.y() * P1.y()),
				(P2.x() * P2.x()) + (P2.y() * P2.y()), (P3.x() * P3.x()) + (P3.y() * P3.y()) }, false);
		
		RealVector solution;
		try
		{
			solution = solver.solve(B);
		} catch (SingularMatrixException err)
		{
			throw new MathException("Infinite circle => line");
		}
		
		RealVector center = solution.getSubVector(1, 2).mapMultiplyToSelf(0.5);
		
		double sq = center.ebeMultiply(center).getL1Norm() + solution.getEntry(0);
		double radius = Math.sqrt(sq);
		
		return new Circle(new Vector2(center), radius);
	}
	
	
	/**
	 * Create a circle from 3 points on the arc (no center given).
	 * 
	 * @author AndreR
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param P
	 * @return The unique circle going through all given points.
	 * @throws MathException If all points are on a line, no finite solution exists!
	 */
	public static Circle circleFromNPoints(final List<IVector2> P)
			throws MathException
	{
		RealMatrix A = new Array2DRowRealMatrix(P.size(), 3);
		RealVector B = new ArrayRealVector(P.size());
		for (int i = 0; i < P.size(); i++)
		{
			IVector2 p = P.get(i);
			A.setEntry(i, 0, 1);
			A.setEntry(i, 1, p.x());
			A.setEntry(i, 2, p.y());
			B.setEntry(i, (p.x() * p.x()) + (p.y() * p.y()));
		}
		
		DecompositionSolver solver = new QRDecomposition(A).getSolver();
		
		RealVector solution;
		try
		{
			solution = solver.solve(B);
		} catch (SingularMatrixException err)
		{
			throw new MathException("Infinite circle => line");
		}
		
		RealVector center = solution.getSubVector(1, 2).mapMultiplyToSelf(0.5);
		
		double sq = center.ebeMultiply(center).getL1Norm() + solution.getEntry(0);
		double radius = Math.sqrt(sq);
		
		return new Circle(new Vector2(center), radius);
	}
	
	
	/**
	 * @param center
	 * @param radius
	 * @return
	 */
	public static Circle getNewCircle(final IVector2 center, final double radius)
	{
		return new Circle(center, radius);
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
	public String toString()
	{
		return "Center = " + center().toString() + "\nRadius = " + radius();
	}
}
