/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.04.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview;

/**
 * Interface for all ai-module panels.
 * 
 * @author Malte
 * 
 */
public interface IChangeGUIMode
{
	/**
	 *
	 */
	void setPlayTestMode();
	
	
	/**
	 *
	 */
	void setRoleTestMode();
	
	
	/**
	 *
	 */
	void setMatchMode();
	
	
	/**
	 *
	 */
	void setEmergencyMode();
	
	
	/**
	 *
	 */
	void onStart();
	
	
	/**
	 *
	 */
	void onStop();
}
