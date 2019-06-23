/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ai.metis.support.IPassTarget;


/**
 *
 */
@Persistent()
public class OffensiveMoveAndTargetInformation
{
	private IPassTarget	passTarget			= null;
	
	private boolean		isReceiveActive	= false;
	
	
	private OffensiveMoveAndTargetInformation()
	{
		// needed for berkley
	}
	
	
	public OffensiveMoveAndTargetInformation(IPassTarget passTarget, boolean isReceiveActive)
	{
		this.passTarget = passTarget;
		this.isReceiveActive = isReceiveActive;
	}
	
	
	public IPassTarget getPassTarget()
	{
		return passTarget;
	}
	
	
	public boolean isReceiveActive()
	{
		return isReceiveActive;
	}
}
