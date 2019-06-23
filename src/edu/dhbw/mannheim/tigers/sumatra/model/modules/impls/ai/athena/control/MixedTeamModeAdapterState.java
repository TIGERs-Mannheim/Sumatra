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

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;


/**
 * This is the match-state of the {@link AthenaGuiAdapter}. It does not replace play-finding or role-assignment but lets
 * the user force new play-decision or view the play-scores
 * 
 * @see AthenaGuiAdapter
 * @see AthenaControl
 * @see IGuiAdapterState
 * 
 * @author Gero
 */
public class MixedTeamModeAdapterState extends AGuiAdapterState
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
	public MixedTeamModeAdapterState(AthenaGuiAdapter adapter)
	{
		super(adapter);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void beforePlayFinding(AIInfoFrame current, AIInfoFrame previous)
	{
		if (adapterHasChanged())
		{
			if (getControl().isForceNewDecision())
			{
				current.playStrategy.setForceNewDecision();
				
				// Event, disable
				getControl().forceNewDecision(false);
			} else
			{
				current.playStrategy.setStateChanged(true);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public boolean overridePlayFinding()
	{
		return false;
	}
	
	
	@Override
	public boolean overrideRoleAssignment()
	{
		return false;
	}
}
