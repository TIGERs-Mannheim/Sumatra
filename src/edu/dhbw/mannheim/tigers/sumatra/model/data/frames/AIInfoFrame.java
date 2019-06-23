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
package edu.dhbw.mannheim.tigers.sumatra.model.data.frames;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.AresData;


/**
 * This class is a simple container for all information the AI gathers during its processes for one {@link WorldFrame}
 * 
 * @author Oliver Steinbrecher, Daniel Waigand, Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AIInfoFrame extends AthenaAiFrame
{
	/** frames per second (based on last frames) */
	private final float		fps;
	
	/** Paths for bots */
	private final AresData	aresData;
	
	
	/**
	 * @param athenaAiFrame
	 * @param aresData
	 * @param fps
	 */
	public AIInfoFrame(final AthenaAiFrame athenaAiFrame, final AresData aresData, final float fps)
	{
		super(athenaAiFrame, athenaAiFrame.getPlayStrategy());
		this.aresData = aresData;
		this.fps = fps;
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
		fps = original.fps;
		aresData = original.aresData;
	}
	
	
	@Override
	public void cleanUp()
	{
		super.cleanUp();
	}
	
	
	/**
	 * @return the fps
	 */
	@Override
	public float getFps()
	{
		return fps;
	}
	
	
	/**
	 * @return the aresData
	 */
	@Override
	public AresData getAresData()
	{
		return aresData;
	}
	
	
	@Override
	public boolean isPersistable()
	{
		return false;
	}
}
