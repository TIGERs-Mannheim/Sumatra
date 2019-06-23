/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.05.2011
 * Author(s): Oliver Steinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;


/**
 * This state of the {@link AthenaGuiAdapter} sets athena to perform an emergency stop.
 * 
 * @see AthenaGuiAdapter
 * @see AthenaControl
 * @see IGuiAdapterState
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class EmergencyModeAdapterState extends AGuiAdapterState
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param adapter
	 */
	public EmergencyModeAdapterState(AthenaGuiAdapter adapter)
	{
		super(adapter);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void beforePlayFinding(AIInfoFrame current, AIInfoFrame previous)
	{
		// do nothing
	}
	
	
	@Override
	public void choosePlays(AIInfoFrame current, AIInfoFrame previous)
	{
		// do nothing
	}
	
	
	@Override
	public void betweenPlayRole(AIInfoFrame current, AIInfoFrame previous)
	{
		// do nothing
	}
	
	
	@Override
	public void assignRoles(AIInfoFrame current, AIInfoFrame previous)
	{
		// do nothing
	}
	
	
	@Override
	public void afterRoleAssignment(AIInfoFrame current, AIInfoFrame previous)
	{
		// do nothing
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public boolean overridePlayFinding()
	{
		return true;
	}
	
	
	@Override
	public boolean overrideRoleAssignment()
	{
		return true;
	}
}
