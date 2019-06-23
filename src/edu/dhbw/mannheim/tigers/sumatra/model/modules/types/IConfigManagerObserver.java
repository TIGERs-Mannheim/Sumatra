/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.11.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;


/**
 * Used by the presenter (and others! O.o) to connect and keep up-to-date with the config-module
 * 
 * @author Gero
 */
public interface IConfigManagerObserver
{
	/**
	 * @param newConfig
	 */
	void onConfigAdded(ManagedConfig newConfig);
	
	
	/**
	 * @param config
	 */
	void onConfigReloaded(ManagedConfig config);
}
