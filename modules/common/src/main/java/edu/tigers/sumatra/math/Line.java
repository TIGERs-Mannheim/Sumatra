/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.04.2011
 * Author(s): Malte
 * *********************************************************
 */
package edu.tigers.sumatra.math;

import com.sleepycat.persist.model.Persistent;


/**
 * Data class representing a mathematical {@link ILine Line} ("Gerade") in vector space. Mutable.
 * 
 * @author Malte
 */
@Persistent(version = 1)
public class Line extends ALine
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/** ("Stuetzvektor") */
	private final IVector2	supportVector;
	
	/** ("Richtungsvektor" */
	private final IVector2	directionVector;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Creates a Line(InitVector, InitVector)
	 */
	protected Line()
	{
		supportVector = new Vector2();
		directionVector = new Vector2();
	}
	
	
	/**
	 * @param l
	 */
	public Line(final ILine l)
	{
		supportVector = new Vector2(l.supportVector());
		directionVector = new Vector2(l.directionVector());
	}
	
	
	/**
	 * Defines a Line by its slope and y-intercept
	 * 
	 * @param slope
	 * @param yIntercept
	 */
	public Line(final double slope, final double yIntercept)
	{
		supportVector = new Vector2(0, yIntercept);
		directionVector = new Vector2(1, slope);
	}
	
	
	/**
	 * Defines a line by a support- and a directionVector.
	 * DirectionVector must not be a zero-vector!<br>
	 * <b>Note:</b> If you want to define a line by two points on that line use:
	 * <p>
	 * <code>Line l = new Line();<br>
	 * l.setPoints(p1, p2);
	 * </code>
	 * </p>
	 * or
	 * <p>
	 * <code>Line l = Line.newLine(p1, p2);
	 * </code>
	 * </p>
	 * 
	 * @param sV
	 * @param dV
	 */
	public Line(final IVector2 sV, final IVector2 dV)
	{
		supportVector = new Vector2(sV);
		// if (dV.isZeroVector())
		// {
		// throw new IllegalArgumentException("Tried to create a line with a zero-direction vector.");
		// }
		directionVector = new Vector2(dV);
	}
	
	
	/**
	 * @param p1
	 * @param p2
	 * @return
	 * @throws IllegalArgumentException if points are equal
	 */
	public static Line newLine(final IVector2 p1, final IVector2 p2)
	{
		IVector2 supportVector = new Vector2f(p1);
		// if (p1.equals(p2))
		// {
		// throw new IllegalArgumentException("Points may not be equal: " + p1);
		// }
		IVector2 directionVector = p2.subtractNew(p1);
		return new Line(supportVector, directionVector);
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
}
