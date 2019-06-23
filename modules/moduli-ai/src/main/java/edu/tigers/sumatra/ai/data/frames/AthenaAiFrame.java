/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 11, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.frames;

import edu.tigers.sumatra.ai.data.IPlayStrategy;


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
