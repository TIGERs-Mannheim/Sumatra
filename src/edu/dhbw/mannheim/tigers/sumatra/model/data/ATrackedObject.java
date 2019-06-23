/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import java.io.Serializable;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;


/**
 * Simple data holder describing objects that are recognized and tracked by the {@link AWorldPredictor}
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * 
 * @author Gero
 * 
 */
public abstract class ATrackedObject implements Serializable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= -5601755416618153247L;
	
	/** bot = assigned standardPatternId, ball = own specific Id */
	public final int				id;
	
	/** mm, z-value currently only used by ball */
	public final Vector2f		pos;
	
	/** m/s, z-value currently only used by ball */
	public final Vector2f		vel;
	
	/** m/s^2, z-value currently only used by ball */
	public final Vector2f		acc;
	
	/** 0-1, 1 = confident */
	public final float			confidence;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param id
	 * @param xPos
	 * @param xVel
	 * @param xAcc
	 * @param yPos
	 * @param yVel
	 * @param yAcc
	 * @param confidence
	 */
	public ATrackedObject(int id, float xPos, float xVel, float xAcc, float yPos, float yVel, float yAcc,
			float confidence)
	{
		this.id = id;
		this.pos = new Vector2f(xPos, yPos);
		this.vel = new Vector2f(xVel, yVel);
		this.acc = new Vector2f(xAcc, yAcc);
		this.confidence = confidence;
	}
	

	/**
	 * @param id
	 * @param pos
	 * @param vel
	 * @param acc
	 * @param confidence
	 */
	public ATrackedObject(int id, IVector2 pos, IVector2 vel, IVector2 acc, float confidence)
	{
		this(id, pos.x(), vel.x(), acc.x(), pos.y(), vel.y(), acc.y(), confidence);
	}
	

	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * @param original
	 */
	public ATrackedObject(ATrackedObject o)
	{
		this(o.id, o.pos, o.vel, o.acc, o.confidence);
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public String toString()
	{
		return "[TrackedObject; pos = " + this.pos + "]";
	}
	

	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && obj.getClass().equals(getClass()))
		{
			ATrackedObject tObj = (ATrackedObject) obj;
			return this.id == tObj.id;
		}
		
		return false;
	}
	

	@Override
	public int hashCode()
	{
		return this.id;
	}
}
