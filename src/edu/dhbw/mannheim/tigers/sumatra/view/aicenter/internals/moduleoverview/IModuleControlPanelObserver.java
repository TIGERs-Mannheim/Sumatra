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


/**
 * Observes the AIModuleControl Panel!
 * 
 * @author Malte, Gero
 */
public interface IModuleControlPanelObserver
{
	/**
	 * Change to {@link edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.EAIControlState#TEST_MODE}
	 */
	void onTestMode();
	
	
	/**
	 * Change to {@link edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.EAIControlState#MATCH_MODE}
	 */
	void onMatchMode();
	
	
	/**
	 * Change to {@link edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.EAIControlState#MIXED_TEAM_MODE}
	 */
	void onMixedTeamMode();
	
	
	/**
	 * Change to {@link edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.EAIControlState#EMERGENCY_MODE}
	 */
	void onEmergencyMode();
	
}
