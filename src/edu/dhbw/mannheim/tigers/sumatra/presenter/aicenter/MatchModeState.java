/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.04.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter;


/**
 * This {@link IAICenterState} handles every input which is allowed if the AI-developer wants to run a normal match
 * 
 * @author Gero
 */
public class MatchModeState extends AICenterState
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param presenter
	 */
	public MatchModeState(AICenterPresenter presenter)
	{
		super(presenter);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void forceNewDecision()
	{
		getControl().forceNewDecision(true);
		sendControl();
		// getControl().forceNewDecision(false); // Event: Should only happen once.
	}
	
	
}
