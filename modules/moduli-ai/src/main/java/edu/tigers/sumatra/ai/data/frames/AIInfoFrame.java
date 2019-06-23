/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.data.frames;

import edu.tigers.sumatra.ai.data.AresData;
import edu.tigers.sumatra.ai.data.IAresData;
import edu.tigers.sumatra.ai.data.PlayStrategy;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * This class is a simple container for all information the AI gathers during its processes for one {@link WorldFrame}
 * 
 * @author Oliver Steinbrecher, Daniel Waigand, Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AIInfoFrame extends AthenaAiFrame
{
	private final IAresData aresData;
	
	
	/**
	 * @param athenaAiFrame
	 * @param aresData data from Ares
	 */
	public AIInfoFrame(final AthenaAiFrame athenaAiFrame, final IAresData aresData)
	{
		super(athenaAiFrame, athenaAiFrame.getPlayStrategy());
		this.aresData = aresData;
	}
	
	
	/**
	 * Providing a <strong>shallow</strong> copy of original (Thus collections are created, but filled with the same
	 * values (instead of copying these values, too))
	 * 
	 * @param original
	 */
	public AIInfoFrame(final AIInfoFrame original)
	{
		super(original, original.getPlayStrategy());
		aresData = original.aresData;
	}
	
	
	/**
	 * Create a frame based on the given baseAiFrame, filling the rest with dummy data
	 * 
	 * @param baseAiFrame
	 * @return
	 */
	public static AIInfoFrame fromBaseAiFrame(BaseAiFrame baseAiFrame)
	{
		return new AIInfoFrame(new AthenaAiFrame(new MetisAiFrame(baseAiFrame, new TacticalField()), new PlayStrategy(
				new PlayStrategy.Builder())), new AresData());
	}
	
	
	public IAresData getAresData()
	{
		return aresData;
	}
}
