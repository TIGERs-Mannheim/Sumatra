/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 24, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.tecchallenges.navigation;

/**
 * data holder for the combination of role number the time this role needs to drive to the center point
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
public class DrivingTimeContainer implements Comparable<DrivingTimeContainer>
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private int		roleNum;
	private long	drivingTime;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param roleNum
	 * @param drivingTime
	 */
	public DrivingTimeContainer(int roleNum, long drivingTime)
	{
		super();
		this.roleNum = roleNum;
		this.drivingTime = drivingTime;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public int compareTo(DrivingTimeContainer o)
	{
		if (o.getDrivingTime() > drivingTime)
		{
			return -1;
		} else if (o.getDrivingTime() < drivingTime)
		{
			return 1;
		}
		return 0;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the roleNum
	 */
	public int getRoleNum()
	{
		return roleNum;
	}
	
	
	/**
	 * @param roleNum the roleNum to set
	 */
	public void setRoleNum(int roleNum)
	{
		this.roleNum = roleNum;
	}
	
	
	/**
	 * @return the drivingTime
	 */
	public long getDrivingTime()
	{
		return drivingTime;
	}
	
	
	/**
	 * @param drivingTime the drivingTime to set
	 */
	public void setDrivingTime(long drivingTime)
	{
		this.drivingTime = drivingTime;
	}
	
	
}
