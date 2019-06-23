/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.04.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;


/**
 * Data class representing a mathematical {@link ILine Line} ("Gerade") in vector space. Mutable.
 * 
 * @author Malte
 * 
 */
public class Line extends ALine
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** ("Stützvektor") */
	private final Vector2			supportVector;
	
	/** ("Richtungsvektor" */
	private final Vector2			directionVector;
	
	private static final Logger	LOG	= Logger.getLogger(Line.class);
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * Creates a Line(InitVector, InitVector)
	  */
	public Line()
	{
		this.supportVector = new Vector2(AIConfig.INIT_VECTOR);
		this.directionVector = new Vector2(AIConfig.INIT_VECTOR);
	}
	

	public Line(ILine l)
	{
		this.supportVector = new Vector2(l.supportVector());
		this.directionVector = new Vector2(l.directionVector());
	}
	

	/**
	 * Defines a Line by its slope and y-intercept
	 */
	public Line(float slope, float yIntercept)
	{
		this.supportVector = new Vector2(0, yIntercept);
		this.directionVector = new Vector2(1, slope);
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
	 * @throws IllegalArgumentException
	 */
	public Line(IVector2 sV, IVector2 dV)
	{
		this.supportVector = new Vector2(sV);
		if (dV.isZeroVector())
		{
			throw new IllegalArgumentException("Directionvector may not be a Zero-Vector!");
		}
		this.directionVector = new Vector2(dV);
	}
	

	public static Line newLine(IVector2 p1, IVector2 p2)
	{
		Line line = new Line();
		line.setPoints(p1, p2);
		return line;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public void set(IVector2 sV, IVector2 dV)
	{
		setSupportVector(sV);
		setDirectionVector(sV);
	}
	

	public void set(Line line)
	{
		setSupportVector(line.supportVector());
		setDirectionVector(line.directionVector());
	}

	public void setSupportVector(IVector2 sV)
	{
		this.supportVector.x = sV.x();
		this.supportVector.y = sV.y();
	}
	

	public void setDirectionVector(IVector2 sV)
	{
		if (sV.isZeroVector())
		{
			throw new IllegalArgumentException("Directionvector may not be a Zero-Vector!");
		}
		this.directionVector.x = sV.x();
		this.directionVector.y = sV.y();
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
			LOG.warn("You tried to create a line with two equal points!");
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
		if (p1.equals(p2))
		{
			throw new IllegalArgumentException("Points may not be equal!");
		} else
		{
			setSupportVector(p1);
			setDirectionVector(p2.subtractNew(p1));
		}
		
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	

	@Override
	public Vector2 supportVector()
	{
		return supportVector;
	}
	

	@Override
	public Vector2 directionVector()
	{
		return directionVector;
	}



}
