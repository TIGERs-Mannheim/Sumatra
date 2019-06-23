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

import java.io.Serializable;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;


/**
 * Data class representing a mathematical {@link ILine Line} ("Gerade") in vector space. Mutable.
 * 
 * @author Malte
 * 
 */
public class Line extends ALine implements Serializable
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log					= Logger.getLogger(Line.class.getName());
	
	/**  */
	private static final long		serialVersionUID	= 1921865867557739325L;
	
	/** ("Stuetzvektor") */
	private Vector2					supportVector;
	
	/** ("Richtungsvektor" */
	private Vector2					directionVector;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Creates a Line(InitVector, InitVector)
	 */
	public Line()
	{
		supportVector = new Vector2(GeoMath.INIT_VECTOR);
		directionVector = new Vector2(GeoMath.INIT_VECTOR);
	}
	
	
	/**
	 * 
	 * @param l
	 */
	public Line(ILine l)
	{
		supportVector = new Vector2(l.supportVector());
		directionVector = new Vector2(l.directionVector());
	}
	
	
	/**
	 * Defines a Line by its slope and y-intercept
	 * @param slope
	 * @param yIntercept
	 */
	public Line(float slope, float yIntercept)
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
	 * @param sV
	 * @param dV
	 */
	public Line(IVector2 sV, IVector2 dV)
	{
		supportVector = new Vector2(sV);
		if (dV.isZeroVector())
		{
			log.error("Tried to create a line with a zero-direction vector.", new Exception());
			directionVector = new Vector2(1, 0);
		} else
		{
			directionVector = new Vector2(dV);
		}
	}
	
	
	/**
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static Line newLine(IVector2 p1, IVector2 p2)
	{
		final Line line = new Line();
		line.setPoints(p1, p2);
		return line;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param sV
	 * @param dV
	 */
	public void set(IVector2 sV, IVector2 dV)
	{
		setSupportVector(sV);
		setDirectionVector(sV);
	}
	
	
	/**
	 * @param line
	 */
	public void set(Line line)
	{
		setSupportVector(line.supportVector());
		setDirectionVector(line.directionVector());
	}
	
	
	/**
	 * @param sV
	 */
	public void setSupportVector(IVector2 sV)
	{
		supportVector.x = sV.x();
		supportVector.y = sV.y();
	}
	
	
	/**
	 * @param sV
	 */
	public void setDirectionVector(IVector2 sV)
	{
		if (sV.isZeroVector())
		{
			throw new IllegalArgumentException("Directionvector may not be a Zero-Vector!");
		}
		directionVector.x = sV.x();
		directionVector.y = sV.y();
	}
	
	
	/**
	 * The Line is now defined by 2 points: the given one (p) and the support vector.<b>
	 * The directionvector of the line
	 * leads from supportVector to p.<br>
	 * If both points are equal: p.x = p.x + 0.001; p.y = p.y + 0.001;
	 * 
	 * @param p
	 */
	public void setPoint(IVector2 p)
	{
		if (p.equals(supportVector()))
		{
			log.warn("You tried to create a line with two equal points!");
		} else
		{
			setDirectionVector(p.subtractNew(supportVector()));
		}
		
	}
	
	
	/**
	 * Re-Defines 'this'. The two Points are element of the line.<br>
	 * Can be used as a constructor. The directionvector of the new line
	 * leads from p1 to p2.<br>
	 * If both points are equal: <code>line = new Line(initVector, initVector);</code>
	 * 
	 * @param p1
	 * @param p2
	 */
	public void setPoints(IVector2 p1, IVector2 p2)
	{
		setSupportVector(p1);
		if (p1.equals(p2))
		{
			log.warn("Points may not be equal: " + p1, new IllegalArgumentException());
			setDirectionVector(new Vector2(1, 0).subtract(p1));
		} else
		{
			setDirectionVector(p2.subtractNew(p1));
		}
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public Vector2 supportVector()
	{
		if (supportVector == null)
		{
			supportVector = new Vector2();
		}
		return supportVector;
	}
	
	
	@Override
	public Vector2 directionVector()
	{
		if (directionVector == null)
		{
			directionVector = new Vector2();
		}
		return directionVector;
	}
	
	
}
