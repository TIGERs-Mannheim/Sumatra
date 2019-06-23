/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.ai.metis.support.IPassTarget;


/**
 * This is a data holder class to allow communication from Athena (roles, plays) back to Metis across two frames
 */
public class AICom implements IAiInfoForNextFrame, IAiInfoFromPrevFrame
{
	private final List<IPassTarget> activePassTargets = new ArrayList<>();
	
	
	/**
	 * Hide this constructor. This class will be created by Metis.
	 */
	protected AICom()
	{
	}
	
	
	@Override
	public List<IPassTarget> getActivePassTargets()
	{
		return activePassTargets;
	}
	
	
	@Override
	public void announcePassingTo(final IPassTarget passTarget)
	{
		if (passTarget != null)
		{
			this.activePassTargets.add(passTarget);
		}
	}
}
