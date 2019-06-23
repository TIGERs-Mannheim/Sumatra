/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.data;

import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * This is a Dataholder class to allow communication between, roles
 * and plays and even back to Metis.
 * 
 * @author MarkG <Mark.Geiger@dlr.de>
 */
public class AICom
{
	private IPassTarget passTarget = null;
	
	private int specialMoveCounter = 0;
	
	private int unassignedStateCounter = 0;
	
	// this is used to send responses from SpecialMoveState to OffensiveStrategy
	private boolean responded = false;
	
	private double protectionPenalty = 0;
	
	private long protectionInitTime = 0;
	
	private IVector2 primaryOffensiveMovePos = null;
	
	
	public IPassTarget getPassTarget()
	{
		return passTarget;
	}
	
	
	public void setPassTarget(final IPassTarget passTarget)
	{
		this.passTarget = passTarget;
	}
	
	
	/**
	 * @return the offensiveRoleCounter
	 */
	public int getSpecialMoveCounter()
	{
		return specialMoveCounter;
	}
	
	
	/**
	 * @param offensiveRoleCounter the offensiveRoleCounter to set
	 */
	public void setSpecialMoveCounter(final int offensiveRoleCounter)
	{
		specialMoveCounter = offensiveRoleCounter;
	}
	
	
	/**
	 * @return the specialMoveResponse
	 */
	public boolean hasResponded()
	{
		return responded;
	}
	
	
	/**
	 * @param specialMoveResponse the specialMoveResponse to set
	 */
	public void setResponded(final boolean specialMoveResponse)
	{
		responded = specialMoveResponse;
	}

	/**
	 * @return the unassignedStateCounter
	 */
	public int getUnassignedStateCounter()
	{
		return unassignedStateCounter;
	}
	
	
	/**
	 * @param unassignedStateCounter the unassignedStateCounter to set
	 */
	public void setUnassignedStateCounter(final int unassignedStateCounter)
	{
		this.unassignedStateCounter = unassignedStateCounter;
	}
	
	
	public double getProtectionPenalty()
	{
		return protectionPenalty;
	}
	
	
	public void setProtectionPenalty(final double protectionPenalty)
	{
		this.protectionPenalty = protectionPenalty;
	}
	
	
	public long getProtectionInitTime()
	{
		return protectionInitTime;
	}
	
	
	public void setProtectionInitTime(final long protectionInitTime)
	{
		this.protectionInitTime = protectionInitTime;
	}
	
	
	public IVector2 getPrimaryOffensiveMovePos()
	{
		return primaryOffensiveMovePos;
	}
	
	
	public void setPrimaryOffensiveMovePos(final IVector2 primaryOffensiveMovePos)
	{
		this.primaryOffensiveMovePos = primaryOffensiveMovePos;
	}
}
