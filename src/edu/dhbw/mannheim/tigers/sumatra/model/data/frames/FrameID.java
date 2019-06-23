/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 27.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.frames;

import java.io.Serializable;


/**
 * Identifier for {@link WorldFrame}s
 * 
 * @author Gero
 */
public class FrameID implements Serializable, Comparable<FrameID>
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= -3973100081292390909L;
	
	/** Matches the WorldFrame to a cam */
	private final int				cam;
	
	/** Identifies a frame unique <strong>per cam</strong> */
	private final long			frameNumber;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// -------------------------------------------------------------------------
	/**
	 * @param cam
	 * @param frameNumber
	 */
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
		cam = original.cam;
		frameNumber = original.frameNumber;
	}
	
	
	@Override
	public int hashCode()
	{
		int hc = 17;
		final int hashMultiplier = 59;
		
		hc = (hc * hashMultiplier) + cam;
		// bit-wise xor
		hc = (hc * hashMultiplier) + (int) (frameNumber ^ (frameNumber >>> 32));
		
		return hc;
	}
	
	
	@Override
	public boolean equals(Object obj)
	{
		if ((obj != null) && obj.getClass().equals(getClass()))
		{
			final FrameID id = (FrameID) obj;
			return (cam == id.cam) && (frameNumber == id.frameNumber);
		}
		
		return false;
	}
	
	
	@Override
	public String toString()
	{
		return "[" + cam + "/" + frameNumber + "]";
	}
	
	
	/**
	 * @return the cam
	 */
	public final int getCam()
	{
		return cam;
	}
	
	
	/**
	 * @return the frameNumber
	 */
	public final long getFrameNumber()
	{
		return frameNumber;
	}
	
	
	@Override
	public int compareTo(FrameID o)
	{
		int cmpFrameNumber = Long.valueOf(frameNumber).compareTo(o.frameNumber);
		if (cmpFrameNumber != 0)
		{
			return cmpFrameNumber;
		}
		return Integer.valueOf(cam).compareTo(o.cam);
	}
}
