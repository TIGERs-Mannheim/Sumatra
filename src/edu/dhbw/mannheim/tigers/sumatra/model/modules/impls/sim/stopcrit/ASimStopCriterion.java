/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 1, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.stopcrit;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class ASimStopCriterion
{
	private AIInfoFrame	firstFrame;
	private AIInfoFrame	latestFrame;
	private long			starttime;
	
	
	/**
	 * Stop the simulation?
	 * 
	 * @param aiFrame
	 * @return
	 */
	public boolean checkStopSimulation(final AIInfoFrame aiFrame)
	{
		if (firstFrame == null)
		{
			firstFrame = aiFrame;
			starttime = SumatraClock.nanoTime();
			return false;
		}
		latestFrame = aiFrame;
		return checkStopSimulation();
	}
	
	
	protected abstract boolean checkStopSimulation();
	
	
	/**
	 * Full runtime
	 * 
	 * @return
	 */
	protected long getRuntime()
	{
		return SumatraClock.nanoTime() - starttime;
	}
	
	
	/**
	 * @return the firstFrame
	 */
	protected AIInfoFrame getFirstFrame()
	{
		return firstFrame;
	}
	
	
	/**
	 * @return the latestFrame
	 */
	protected AIInfoFrame getLatestFrame()
	{
		return latestFrame;
	}
	
	
	/**
	 * @return the starttime
	 */
	protected long getStarttime()
	{
		return starttime;
	}
}
