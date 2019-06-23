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

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.PlayAndRoleCount;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.stats.ESelectionReason;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
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
	 * @param presenter
	 */
	public PlayTestState(AICenterPresenter presenter)
	{
		super(presenter);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void addNewPlay(EPlay play, int numRolesToAssign)
	{
		getControl().addNewPlay(new PlayAndRoleCount(play, numRolesToAssign, ESelectionReason.MANUEL));
		sendControl();
	}
	
	
	@Override
	public void removePlay(APlay play)
	{
		play.changeToCanceled();
		getControl().removePlay(play);
		sendControl();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
