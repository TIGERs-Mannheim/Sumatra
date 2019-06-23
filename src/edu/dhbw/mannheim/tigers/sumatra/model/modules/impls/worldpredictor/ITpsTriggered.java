/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.04.2014
 * Author(s): KaiE
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor;

/**
 * This Interface is required for the TpsTrigger class to add a certain object to the Hashmap.
 * 
 * @author KaiE
 */
public interface ITpsTriggered
{
	/**
	 * This method is called by an FpsTrigger-Object.
	 */
	void onElementTriggered();
}
