/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line;

import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Data class representing a mathematical {@link ILine Line} in vector space.
 * The line is represented by support and direction vector.
 * The direction vector can be used for line segment, too.
 * 
 * @author Malte
 */
@Persistent(version = 1)
public class Line extends ALine
{
	/** ("Stuetzvektor") */
	private final IVector2	supportVector;
	
	/** ("Richtungsvektor" */
	private final IVector2	directionVector;
	
	
	@SuppressWarnings("unused")
	private Line()
	{
		this(AVector2.ZERO_VECTOR, AVector2.ZERO_VECTOR);
	}
	
	
	private Line(final IVector2 sV, final IVector2 dV)
	{
		supportVector = Vector2.copy(sV);
		directionVector = Vector2.copy(dV);
	}
	
	
	protected Line(final ILine line)
	{
		this(line.supportVector(), line.directionVector());
	}
	
	
	/**
	 * @param pFrom first point on line
	 * @param pTo second point on line
	 * @return a new line that points from pFrom to pTo
	 */
	public static Line fromPoints(final IVector2 pFrom, final IVector2 pTo)
	{
		IVector2 supportVector = Vector2.copy(pFrom);
		IVector2 directionVector = pTo.subtractNew(pFrom);
		return new Line(supportVector, directionVector);
	}
	
	
	/**
	 * Defines a line by a support- and a directionVector.
	 *
	 * @param supportVector of the line
	 * @param directionVector of the line
	 * @return a new line
	 */
	public static Line fromDirection(final IVector2 supportVector, final IVector2 directionVector)
	{
		return new Line(supportVector, directionVector);
	}
	
	
	/**
	 * Calculates a regression line through all points.
	 * 
	 * @param points
	 * @return Optional Line.
	 */
	@SuppressWarnings("squid:S1166")
	public static Optional<Line> fromPointsList(final List<IVector2> points)
	{
		int numPoints = points.size();
		
		if (numPoints < 2)
		{
			return Optional.empty();
		}
		
		RealMatrix matA = new Array2DRowRealMatrix(numPoints, 2);
		RealVector b = new ArrayRealVector(numPoints);
		
		for (int i = 0; i < numPoints; i++)
		{
			matA.setEntry(i, 0, points.get(i).x());
			matA.setEntry(i, 1, 1.0);
			
			b.setEntry(i, points.get(i).y());
		}
		
		DecompositionSolver solver = new QRDecomposition(matA).getSolver();
		RealVector x;
		try
		{
			x = solver.solve(b);
		} catch (SingularMatrixException e)
		{
			return Optional.empty();
		}
		
		double slope = x.getEntry(0);
		double offset = x.getEntry(1);
		
		double x1 = points.get(0).x();
		double x2 = points.get(points.size() - 1).x();
		
		Vector2 p1 = Vector2.fromXY(x1, (x1 * slope) + offset);
		Vector2 p2 = Vector2.fromXY(x2, (x2 * slope) + offset);
		
		return Optional.of(Line.fromPoints(p1, p2));
	}
	
	
	/**
	 * Copy a line.
	 * 
	 * @param line to be copied
	 * @return deep copy of given line
	 */
	public static Line copyOf(final ILine line)
	{
		return Line.fromDirection(line.supportVector(), line.directionVector());
	}
	
	
	@Override
	public IVector2 supportVector()
	{
		return supportVector;
	}
	
	
	@Override
	public IVector2 directionVector()
	{
		return directionVector;
	}
	
	
	@Override
	public final boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof ILine))
		{
			return false;
		}
		
		final ILine line = (ILine) o;
		
		return supportVector.equals(line.supportVector())
				&& directionVector.equals(line.directionVector());
	}
	
	
	@Override
	public final int hashCode()
	{
		int result = supportVector.hashCode();
		result = (31 * result) + directionVector.hashCode();
		return result;
	}
	
	
	@Override
	public String toString()
	{
		return "Line: (" + supportVector().x() + "," + supportVector().y() + ") + v * (" + directionVector().x() + ","
				+ directionVector().y() + ")";
	}
}
