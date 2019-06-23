/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.07.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;


/**
 * Play, which shall be choosen when a 'Halt'-referee command was sent.
 * It works, because Ares will sent stop-skills to all bots without a role.
 * 
 * @author Malte
 * 
 */
public class HaltPlay extends AStandardPlay
{
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public HaltPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		// nothing todo
	}
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		// nothing todo
	}
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
