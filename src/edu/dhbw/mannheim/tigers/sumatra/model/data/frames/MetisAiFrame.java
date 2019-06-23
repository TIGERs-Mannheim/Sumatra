/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 11, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.frames;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ITacticalField;


/**
 * This frame extends the {@link BaseAiFrame} with information gathered from Metis module.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class MetisAiFrame extends BaseAiFrame
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/** stores all tactical information added by metis' calculators. */
	private final ITacticalField	tacticalField;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param baseAiFrame
	 * @param tacticalInfo
	 */
	public MetisAiFrame(BaseAiFrame baseAiFrame, ITacticalField tacticalInfo)
	{
		super(baseAiFrame);
		tacticalField = tacticalInfo;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the tacticalInfo
	 */
	public ITacticalField getTacticalField()
	{
		return tacticalField;
	}
}
