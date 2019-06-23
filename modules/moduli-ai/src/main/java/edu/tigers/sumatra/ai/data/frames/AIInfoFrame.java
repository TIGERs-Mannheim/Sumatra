/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.08.2010
 * Author(s):
 * Oliver Steinbrecher
 * Daniel Waigand
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.frames;

import edu.tigers.sumatra.wp.data.WorldFrame;

/**
 * This class is a simple container for all information the AI gathers during its processes for one {@link WorldFrame}
 * 
 * @author Oliver Steinbrecher, Daniel Waigand, Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AIInfoFrame extends AthenaAiFrame
{
	/**
	 * @param athenaAiFrame
	 */
	public AIInfoFrame(final AthenaAiFrame athenaAiFrame)
	{
		super(athenaAiFrame, athenaAiFrame.getPlayStrategy());
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
	}
}
