/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects;

import java.io.Serializable;

import net.sf.oval.constraint.NotNull;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.AObjectID;
import edu.dhbw.mannheim.tigers.sumatra.util.units.DistanceUnit;


/**
 * Simple data holder describing objects that are recognized and tracked by the
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor}
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * 
 * @author Gero
 * 
 */
@Persistent
public abstract class ATrackedObject implements Serializable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= -5601755416618153247L;
	
	/** bot = assigned standardPatternId, ball = own specific Id */
	@NotNull
	protected AObjectID			id;
	
	/** 0-1, 1 = confident */
	public float					confidence;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@SuppressWarnings("unused")
	private ATrackedObject()
	{
	}
	
	
	/**
	 * @param id
	 * @param confidence
	 */
	public ATrackedObject(AObjectID id, float confidence)
	{
		this.id = id;
		this.confidence = confidence;
	}
	
	
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * @param o
	 */
	public ATrackedObject(ATrackedObject o)
	{
		this(o.id, o.confidence);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return id
	 */
	public AObjectID getId()
	{
		return id;
	}
	
	
	@Override
	public String toString()
	{
		return "[TrackedObject; pos = " + getPos() + "]";
	}
	
	
	/**
	 * Note: This implementation will only compare the id of this object! Be beware of this when you compare sth.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		
		if ((obj != null) && obj.getClass().equals(getClass()))
		{
			final ATrackedObject tObj = (ATrackedObject) obj;
			return id.equals(tObj.id);
		}
		
		return false;
	}
	
	
	@Override
	public int hashCode()
	{
		return id.hashCode();
	}
	
	
	/**
	 * @return the pos
	 */
	public abstract IVector2 getPos();
	
	
	/**
	 * @return the vel
	 */
	public abstract IVector2 getVel();
	
	
	/**
	 * @return velocity in mm
	 */
	public IVector2 getVelInMM()
	{
		return DistanceUnit.METERS.toMillimeters(getVel());
	}
	
	
	/**
	 * @return the acc
	 */
	public abstract IVector2 getAcc();
	
	
	/**
	 * @return the confidence
	 */
	public final float getConfidence()
	{
		return confidence;
	}
}
