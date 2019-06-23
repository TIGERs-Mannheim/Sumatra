/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2014
 * Author(s): MarkG <Mark,Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.IVector2;


/**
 * This is a Dataholder class to allow communication between, roles
 * and plays and even back to Metis.
 * 
 * @author MarkG <Mark.Geiger@dlr.de>
 */
public class AICom
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private IVector2			offensiveRolePassTarget		= null;
	
	private BotID				offensiveRolePassTargetID	= null;
	
	private int					specialMoveCounter			= 0;
	
	private int					unassignedStateCounter		= 0;
	
	// this is used to send responses from SpecialMoveState to OffensiveStrategy
	private boolean			responded						= false;
	
	private List<IVector2>	delayMoves						= new ArrayList<IVector2>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param passTarget
	 */
	public void setOffensiveRolePassTargetID(final BotID passTarget)
	{
		offensiveRolePassTargetID = passTarget;
	}
	
	
	/**
	 * @return botID of the bot to wich the offensiveRole will pass
	 */
	public BotID getOffensiveRolePassTargetID()
	{
		return offensiveRolePassTargetID;
	}
	
	
	/**
	 * @param passTarget
	 */
	public void setOffensiveRolePassTarget(final IVector2 passTarget)
	{
		offensiveRolePassTarget = passTarget;
	}
	
	
	/**
	 * @return BotID of the passTarget if offensiveRole is about to Pass, else NULL
	 */
	public IVector2 getOffensiveRolePassTarget()
	{
		return offensiveRolePassTarget;
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
	 * @return the delayMoves
	 */
	public List<IVector2> getDelayMoves()
	{
		return delayMoves;
	}
	
	
	/**
	 * @param delayMoves the delayMoves to set
	 */
	public void setDelayMoves(final List<IVector2> delayMoves)
	{
		this.delayMoves = delayMoves;
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
}
