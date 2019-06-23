/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.04.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;


/**
 * The basic implementation of {@link IGuiAdapterState}, for easy usage.
 * 
 * @author Gero
 */
public abstract class AGuiAdapterState implements IGuiAdapterState
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	protected final AthenaGuiAdapter	adapter;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param adapter
	 */
	public AGuiAdapterState(AthenaGuiAdapter adapter)
	{
		this.adapter = adapter;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void beforePlayFinding(AIInfoFrame current, AIInfoFrame previous)
	{
		
	}
	

	@Override
	public void choosePlays(AIInfoFrame current, AIInfoFrame previous)
	{
		
	}
	

	@Override
	public void betweenPlayRole(AIInfoFrame current, AIInfoFrame previous)
	{
		
	}
	

	@Override
	public void assignRoles(AIInfoFrame current, AIInfoFrame previous)
	{
		
	}
	

	@Override
	public void afterRoleAssignment(AIInfoFrame current, AIInfoFrame previous)
	{
		
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	protected AthenaControl getControl()
	{
		return adapter.getControl();
	}
	
	
	protected boolean hasChanged()
	{
		return adapter.hasChanged();
	}
}