/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.athena;

import edu.tigers.sumatra.ai.metis.MetisAiFrame;


/**
 * Ai frame for athena data, based on {@link MetisAiFrame}
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AthenaAiFrame extends MetisAiFrame
{
	private final IPlayStrategy	playStrategy;
	
	
	/**
	 * @param metisAiFrame
	 * @param playStrategy
	 */
	public AthenaAiFrame(final MetisAiFrame metisAiFrame, final IPlayStrategy playStrategy)
	{
		super(metisAiFrame, metisAiFrame.getTacticalField());
		this.playStrategy = playStrategy;
	}
	
	
	/**
	 * @return the playStrategy
	 */
	public IPlayStrategy getPlayStrategy()
	{
		return playStrategy;
	}
}
