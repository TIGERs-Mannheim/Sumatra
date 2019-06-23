/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.04.2011
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.aicenter.view;


/**
 * Observes the AIModuleControl Panel!
 * 
 * @author Malte, Gero
 */
public interface IModuleControlPanelObserver
{
	/**
	 * Change to {@link edu.tigers.sumatra.ai.data.EAIControlState#TEST_MODE}
	 */
	void onTestMode();
	
	
	/**
	 * Change to {@link edu.tigers.sumatra.ai.data.EAIControlState#MATCH_MODE}
	 */
	void onMatchMode();
	
	
	/**
	 * Change to {@link edu.tigers.sumatra.ai.data.EAIControlState#MIXED_TEAM_MODE}
	 */
	void onMixedTeamMode();
	
	
	/**
	 * Change to {@link edu.tigers.sumatra.ai.data.EAIControlState#EMERGENCY_MODE}
	 */
	void onEmergencyMode();
	
}
