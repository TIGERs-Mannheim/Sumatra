/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 27.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import java.io.Serializable;


/**
 * Identifier for {@link WorldFrame}s
 * 
 * @author Gero
 */
public class FrameID implements Serializable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= -3973100081292390909L;
	
	/** Matches the WorldFrame to a cam */
	public final int				cam;
	
	/** Identifies a frame unique <strong>per cam</strong> */
	public final long				frameNumber;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// -------------------------------------------------------------------------
	public FrameID(int cam, long frameNumber)
	{
		this.cam = cam;
		this.frameNumber = frameNumber;
	}
	

	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * @param original
	 */
	public FrameID(FrameID original)
	{
		this.cam = original.cam;
		this.frameNumber = original.frameNumber;
	}
	

	@Override
	public int hashCode()
	{
		int hc = 17;
		int hashMultiplier = 59;
		
		hc = hc * hashMultiplier + cam;
		hc = hc * hashMultiplier + (int) (frameNumber ^ (frameNumber >>> 32)); // bit-wise xor
		
		return hc;
	}
	

	@Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		
		if (obj != null && obj.getClass().equals(getClass()))
		{
			FrameID id = (FrameID) obj;
			result = equals(id);
		}
		
		return result;
	}
	

	/**
	 * @param id
	 * @return this.cam == id.cam <strong>&&</strong> this.frameNumber == id.frameNumber
	 */
	public boolean equals(FrameID id)
	{
		return this.cam == id.cam && this.frameNumber == id.frameNumber;
	}
	

	@Override
	public String toString()
	{
		return "[" + cam + "/" + frameNumber + "]";
	}
}
