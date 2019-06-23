/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai;

import edu.tigers.sumatra.ai.ares.AresData;
import edu.tigers.sumatra.ai.ares.IAresData;
import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.athena.PlayStrategy;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * This class is a simple container for all information the AI gathers during its processes for one {@link WorldFrame}
 * 
 * @author Oliver Steinbrecher, Daniel Waigand, Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AIInfoFrame extends AthenaAiFrame
{
	private final IAresData aresData;
	private final long tAssembly = System.nanoTime();
	
	
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
	
	
	/**
	 * @return the assembly timestamp in [ns]
	 */
	public long gettAssembly()
	{
		return tAssembly;
	}
}
