/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.04.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;


/**
 * Immutable implementation of {@link Line}
 * 
 * @author Malte
 * 
 */
public class Linef extends ALine
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Vector2f	supportVector;
	private final Vector2f	directionVector;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param l
	 */
	public Linef(ALine l)
	{
		supportVector = new Vector2f(l.supportVector());
		directionVector = new Vector2f(l.directionVector());
	}
	
	
	/**
	 * @param slope
	 * @param yIntercept
	 */
	public Linef(float slope, float yIntercept)
	{
		supportVector = new Vector2f(0, yIntercept);
		directionVector = new Vector2f(1, slope);
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
	 * <code>Line l = Line.newLine(p1, p2)
	 * </code>
	 * </p>
	 * @param sV
	 * @param dV
	 * @throws IllegalArgumentException
	 */
	public Linef(IVector2 sV, IVector2 dV)
	{
		supportVector = new Vector2f(sV);
		if (dV.isZeroVector())
		{
			throw new IllegalArgumentException("Directionvector may not be a Zero-Vector!");
		}
		directionVector = new Vector2f(dV);
	}
	
	
	@Override
	public Vector2f supportVector()
	{
		return supportVector;
	}
	
	
	@Override
	public Vector2f directionVector()
	{
		return directionVector;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
