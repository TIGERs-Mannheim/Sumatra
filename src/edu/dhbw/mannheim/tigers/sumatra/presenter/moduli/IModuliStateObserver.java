/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.moduli;

import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;


/**
 * Moduli state observer interface.
 * 
 * @author AndreR
 * 
 */
public interface IModuliStateObserver
{
	/**
	 * 
	 * @param state
	 */
	void onModuliStateChanged(ModulesState state);
}
