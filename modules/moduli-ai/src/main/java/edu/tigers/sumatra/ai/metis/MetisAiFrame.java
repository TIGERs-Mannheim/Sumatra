/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis;

import edu.tigers.sumatra.ai.BaseAiFrame;


/**
 * This frame extends the {@link BaseAiFrame} with information gathered from Metis module.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MetisAiFrame extends BaseAiFrame
{
	/** stores all tactical information added by metis' calculators. */
	private final ITacticalField	tacticalField;
	
	
	/**
	 * @param baseAiFrame
	 * @param tacticalInfo
	 */
	public MetisAiFrame(final BaseAiFrame baseAiFrame, final ITacticalField tacticalInfo)
	{
		super(baseAiFrame);
		tacticalField = tacticalInfo;
	}
	
	
	/**
	 * @return the tacticalInfo
	 */
	public ITacticalField getTacticalField()
	{
		return tacticalField;
	}
}
