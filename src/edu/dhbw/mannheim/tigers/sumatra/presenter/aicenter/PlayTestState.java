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

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;


/**
 * This {@link IAICenterState} handles every input which is allowed if the AI-developer wants to test plays
 * 
 * @author Gero
 */
public class PlayTestState extends AICenterState
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param panel
	 */
	public PlayTestState(AICenterPresenter presenter)
	{
		super(presenter);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void addPlay(EPlay play)
	{
		getControl().addPlay(play);
		sendControl();
	}
	

	@Override
	public void removePlay(List<EPlay> oddPlays)
	{
		getControl().removePlay(oddPlays);
		sendControl();
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
