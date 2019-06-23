/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.04.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview;

import edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.EAIControlState;


/**
 * Observes the AIModuleControl Panel!
 * 
 * @author Malte, Gero
 */
public interface IModuleControlPanelObserver
{
	/**
	 * Change to {@link EAIControlState#PLAY_TEST_MODE}
	 */
	public void onPlayTestMode();
	

	/**
	 * Change to {@link EAIControlState#ROLE_TEST_MODE}
	 */
	public void onRoleTestMode();
	

	/**
	 * Change to {@link EAIControlState#MATCH_MODE}
	 */
	public void onMatchMode();
	

	/**
	 * Change to {@link EAIControlState#EMERGENCY_MODE}
	 */
	public void onEmergencyMode();
	
}
